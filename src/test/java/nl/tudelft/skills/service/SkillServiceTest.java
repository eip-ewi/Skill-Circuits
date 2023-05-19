/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.tudelft.skills.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SkillServiceTest {

	private final AbstractSkillRepository abstractSkillRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final EditionRepository editionRepository;
	private final TaskRepository taskRepository;
	private final ModuleRepository moduleRepository;
	private final SkillService skillService;
	private final SkillRepository skillRepository;
	private AuthorisationService authorisationService;

	private final RoleControllerApi roleApi;
	private final CourseControllerApi courseApi;
	private final EditionControllerApi editionApi;

	private final PersonRepository personRepository;

	private TestDatabaseLoader db;
	private final LocalDateTime localDateTime;

	@Autowired
	public SkillServiceTest(AbstractSkillRepository abstractSkillRepository, TaskRepository taskRepository,
			TestDatabaseLoader db, TaskCompletionRepository taskCompletionRepository,
			EditionControllerApi editionApi, CourseControllerApi courseApi,
			SkillRepository skillRepository, ModuleRepository moduleRepository,
			ExternalSkillRepository externalSkillRepository, EditionRepository editionRepository,
			AuthorisationService authorisationService, RoleControllerApi roleApi,
			PersonRepository personRepository) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.externalSkillRepository = externalSkillRepository;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.taskRepository = taskRepository;
		this.skillRepository = skillRepository;
		this.personRepository = personRepository;

		// The service is not mocked to test the specifics of whether an edition is shown because it
		// is visible, or because the person is at least a teacher in the edition
		this.authorisationService = authorisationService;

		this.db = db;
		this.localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);

		this.roleApi = roleApi;
		this.courseApi = courseApi;
		this.editionApi = editionApi;

		this.skillService = new SkillService(abstractSkillRepository, taskCompletionRepository, courseApi,
				authorisationService, personRepository);
	}

	@Test
	public void deleteSkill() {
		Long id = db.getSkillVariables().getId();

		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// All TaskCompletions are in other Skills, so they should not have been deleted
		assertThat(taskCompletionRepository.findAll()).hasSize(4);
	}

	@Test
	public void deleteSkillTaskCompletionsImpacted() {
		Long id = db.getSkillNegation().getId();

		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// Two of the previously saved TaskCompletions were in the deleted Skill, so
		// the size should be 2 now
		assertThat(taskCompletionRepository.findAll()).hasSize(2);
	}

	@Test
	public void deleteSkillPreviousEditionSkillImpacted() {
		// For testing purposes, set another skill to be the previous editions
		// skill of skill variables. This scenario would not occur in the real setup, since
		// they are in the same edition, this test only checks if the deletion
		// is propagated.

		Skill previousEditionSkill = db.getSkillAssumption();
		Skill newerEditionSkill = db.getSkillVariables();
		newerEditionSkill.setPreviousEditionSkill(previousEditionSkill);
		newerEditionSkill = abstractSkillRepository.save(newerEditionSkill);
		abstractSkillRepository.flush();

		// Delete the previous editions skill, which is referenced by the newer editions skill
		Long id = previousEditionSkill.getId();
		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// Assert that the previous edition skill is null after it was deleted.
		assertThat(newerEditionSkill.getPreviousEditionSkill()).isNull();

		// All TaskCompletions are in other Skills, so they should
		// not have been deleted
		assertThat(taskCompletionRepository.findAll()).hasSize(4);
	}

	@Test
	public void deleteSkillFutureEditionSkillImpacted() {
		// For testing purposes, set another skill to be the previous editions
		// skill of skill variables. This scenario would not occur in the real setup, since
		// they are in the same edition, this test only checks if the deletion
		// is propagated.

		Skill previousEditionSkill = db.getSkillAssumption();
		Skill newerEditionSkill = db.getSkillVariables();
		previousEditionSkill.getFutureEditionSkills().add(newerEditionSkill);
		newerEditionSkill.setPreviousEditionSkill(previousEditionSkill);
		newerEditionSkill = abstractSkillRepository.save(newerEditionSkill);

		// Delete the newer editions skill, which references the previous editions skill
		Long id = newerEditionSkill.getId();
		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// Assert that the "future" edition skill is null after it was deleted.
		assertThat(skillRepository.findByIdOrThrow(previousEditionSkill.getId()).getFutureEditionSkills())
				.isEmpty();

		// All TaskCompletions are in other Skills, so they should not have been deleted
		assertThat(taskCompletionRepository.findAll()).hasSize(4);
	}

	/**
	 * Mocks the responses of the courseApi and editionApi for three courses. Sets visibility of editionRL to
	 * the given value. Also mocks the role of the person in all editions.
	 *
	 * @param visible Whether the current edition should be visible.
	 * @param role    Role of the person.
	 */
	private void mockEditionsAndSetVisible(boolean visible, String role) {
		Long idInUse = db.getEditionRL().getId();

		// Mock the response of the courseApi to return the course details
		// Edition C should be the most recent edition
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(
						new EditionSummaryDTO().id(idInUse).startDate(localDateTime),
						new EditionSummaryDTO().id(idInUse + 1).startDate(localDateTime.plusDays(2)),
						new EditionSummaryDTO().id(idInUse + 2).startDate(localDateTime.plusDays(4))));
		Mockito.when(courseApi.getCourseByEdition(db.getEditionRL().getId())).thenReturn(Mono.just(course));

		// Set edition to be visible
		db.getEditionRL().setVisible(visible);
		editionRepository.save(db.getEditionRL());

		// Mock the role of the person for all of the editions
		List<RoleDetailsDTO> roleDetails = List.of(idInUse, idInUse + 1, idInUse + 2).stream()
				.map(id -> new RoleDetailsDTO()
						.id(new Id().editionId(id)
								.personId(TestUserDetailsService.id))
						.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
						.type(RoleDetailsDTO.TypeEnum.valueOf(role)))
				.collect(Collectors.toList());
		when(roleApi.getRolesById(anySet(), anySet()))
				.thenReturn(Flux.fromIterable(roleDetails));
	}

	@Test
	@WithUserDetails("username")
	public void testRecentActiveEditionOneEdition() {
		// Test scenario in which there is only one edition
		// The method should return that editions skill

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Mock the response of the courseApi to return the course details
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(new EditionSummaryDTO().id(db.getEditionRL().getId())));
		Mockito.when(courseApi.getCourseByEdition(db.getEditionRL().getId())).thenReturn(Mono.just(course));

		// Mock response so that the edition is also active
		Mockito.when(editionApi.getAllEditionsActiveOrTaughtBy(db.getPerson().getId()))
				.thenReturn(Flux.just(new EditionDetailsDTO().id(db.getEditionRL().getId())));

		// Set edition to be visible
		db.getEditionRL().setVisible(true);
		editionRepository.save(db.getEditionRL());
		// Mock the role of the person
		when(roleApi.getRolesById(anySet(), anySet()))
				.thenReturn(Flux.just(new RoleDetailsDTO()
						.id(new Id().editionId(db.getEditionRL().getId())
								.personId(TestUserDetailsService.id))
						.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
						.type(RoleDetailsDTO.TypeEnum.valueOf("STUDENT"))));

		// Assert that the recent active edition method returns the correct skill
		// Since there is only one edition, this is the skill in this edition
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(db.getSkillAssumption());

		// Assert on the traversal list
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactly(db.getSkillAssumption());
	}

	@Test
	@WithUserDetails("username")
	public void testRecentActiveEditionReturnNull() {
		// Test scenario in which there is only a skill in an invisible edition
		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Mock the response of the courseApi to return the course details
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(new EditionSummaryDTO().id(db.getEditionRL().getId())));
		Mockito.when(courseApi.getCourseByEdition(db.getEditionRL().getId())).thenReturn(Mono.just(course));

		// Mock response so that the edition is also active
		Mockito.when(editionApi.getAllEditionsActiveOrTaughtBy(db.getPerson().getId()))
				.thenReturn(Flux.just(new EditionDetailsDTO().id(db.getEditionRL().getId())));

		// Mock the role of the person
		when(roleApi.getRolesById(anySet(), anySet()))
				.thenReturn(Flux.just(new RoleDetailsDTO()
						.id(new Id().editionId(db.getEditionRL().getId())
								.personId(TestUserDetailsService.id))
						.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
						.type(RoleDetailsDTO.TypeEnum.valueOf("STUDENT"))));

		// Assert that the recent active edition method returns null (edition is invisible)
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(null);

		// Assert on the traversal list
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactly(db.getSkillAssumption());
	}

	@Test
	@WithUserDetails("username")
	public void testMultipleEditionsNoTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, but no task was completed. Edition structure
		 * is: editionA --> (editionB and editionC). With editionC being the latest edition. The skill which
		 * the external skill refers to should be "copied" from edition A to editions B and C. The method
		 * should return the latest editions skill (edition C).
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2, true);

		// Skills in edition B/C should be copies of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		skillEditionC.setPreviousEditionSkill(db.getSkillAssumption());
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionC);

		mockEditionsAndSetVisible(true, "STUDENT");

		// Assert that the recent active edition method returns the correct skill
		// The person has not completed any tasks in any edition yet, so it should return the most recent
		// editions skill (skillEditionC)
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionC);

		// Assert on the traversal lists
		// The order cannot be asserted on, since the futureEditionSkills is a set
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionC))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
	}

	@Test
	@WithUserDetails("username")
	public void testMultipleEditionsWithTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, and a task was completed in one of the less
		 * recent editions. Edition structure is: editionA --> (editionB and editionC). With editionC being
		 * the latest edition. A task was completed in editionB. The skill which the external skill refers to
		 * should be "copied" from edition A to editions B and C. The method should return the editions skill
		 * in which a task was completed.
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2, true);

		// Skills in edition B/C should be copies of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		skillEditionC.setPreviousEditionSkill(db.getSkillAssumption());
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionC);

		// Set a task in skillEditionB to be completed by the person
		Task task = Task.builder().skill(skillEditionB).name("Task").build();
		task = taskRepository.save(task);
		TaskCompletion taskCompletion = TaskCompletion.builder().task(task).person(db.getPerson()).build();
		taskCompletion = taskCompletionRepository.save(taskCompletion);
		task.getCompletedBy().add(taskCompletion);
		db.getPerson().getTaskCompletions().add(taskCompletion);

		mockEditionsAndSetVisible(true, "STUDENT");

		// Assert that the recent active edition method returns the correct skill
		// The person has completed a task in editionB, so it should return that editions skill
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);

		// Assert on the traversal lists
		// The order cannot be asserted on, since the futureEditionSkills is a set
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionC))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
	}

	@Test
	@WithUserDetails("username")
	public void testMultipleEditionsChainWithTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, and a task was completed in one of the less
		 * recent editions. Edition structure is: editionA --> editionB --> editionC. With editionC being the
		 * latest edition. A task was completed in editionB. The skill which the external skill refers to
		 * should be "copied" from edition A to editions B and C. The method should return the editions skill
		 * in which a task was completed.
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2, true);

		// Skill in edition B should be a copy of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		// Skill in edition C should be a copy of skill in edition C
		skillEditionC.setPreviousEditionSkill(skillEditionB);
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);
		skillEditionB.getFutureEditionSkills().add(skillEditionC);

		// Set a task in skillEditionB to be completed by the person
		Task task = Task.builder().skill(skillEditionB).name("Task").build();
		task = taskRepository.save(task);
		TaskCompletion taskCompletion = TaskCompletion.builder().task(task).person(db.getPerson()).build();
		taskCompletion = taskCompletionRepository.save(taskCompletion);
		task.getCompletedBy().add(taskCompletion);
		db.getPerson().getTaskCompletions().add(taskCompletion);

		mockEditionsAndSetVisible(true, "STUDENT");

		// Assert that the recent active edition method returns the correct skill
		// The person has completed a task in editionB, so it should return that editions skill
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);

		// Assert on the traversal lists
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactly(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactly(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionC))
				.containsExactly(db.getSkillAssumption(), skillEditionB, skillEditionC);
	}

	@Test
	@WithUserDetails("username")
	public void testMultipleEditionsMostRecentDoesNotHaveSkill() {
		/*
		 * Test scenario in which there are multiple editions, and the most recent edition does not contain
		 * the parent skill. Edition structure is: editionA --> editionB, editionC (independent). With
		 * editionC being the latest edition, and editionB the one before that. The skill which the external
		 * skill refers to should be "copied" from edition A to editions B, but not C. The method should
		 * return the skill in edition B.
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);

		// Skill in edition B should be a copy of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);

		// Create empty edition C
		editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		mockEditionsAndSetVisible(true, "STUDENT");

		// Assert that the recent active edition method returns the correct skill
		// The most recent edition which contains the copied skill is editionB
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);

		// Assert on the traversal lists
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactly(db.getSkillAssumption(), skillEditionB);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactly(db.getSkillAssumption(), skillEditionB);
	}

	@Test
	@WithUserDetails("username")
	public void testMultipleEditionsTaskCompletedInvisibleEdition() {
		/*
		 * Test scenario in which there are multiple editions, and a task was completed in the oldest edition,
		 * however it now being not visible. Edition structure is: editionA --> (editionB and editionC). With
		 * editionC being the latest edition. A task was completed in editionA. The skill which the external
		 * skill refers to should be "copied" from edition A to editions B and C. The method should return the
		 * most skill of recent edition, which is also visible (editionC).
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2, true);

		// Skills in edition B/C should be copies of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		skillEditionC.setPreviousEditionSkill(db.getSkillAssumption());
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionC);

		// Set a task in skillEditionA to be completed by the person
		Task task = Task.builder().skill(db.getSkillAssumption()).name("Task").build();
		task = taskRepository.save(task);
		TaskCompletion taskCompletion = TaskCompletion.builder().task(task).person(db.getPerson()).build();
		taskCompletion = taskCompletionRepository.save(taskCompletion);
		task.getCompletedBy().add(taskCompletion);
		db.getPerson().getTaskCompletions().add(taskCompletion);

		mockEditionsAndSetVisible(false, "STUDENT");

		// Assert that the recent active edition method returns the correct skill
		// The latest visible edition is edition C, so it should return the skill in that edition
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionC);

		// Assert on the traversal lists
		// The order cannot be asserted on, since the futureEditionSkills is a set
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionC))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
	}

	@Test
	@WithUserDetails("username")
	public void testMultipleEditionsWithoutTaskCompletedInvisibleEdition() {
		/*
		 * Test scenario in which there are multiple editions, and the most recent edition is not visible.
		 * Edition structure is: editionA --> (editionB and editionC). With editionC being the latest edition.
		 * The skill which the external skill refers to should be "copied" from edition A to editions B and C.
		 * The method should return the most skill of recent edition, which is also visible (editionB).
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2, false);

		// Skills in edition B/C should be copies of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		skillEditionC.setPreviousEditionSkill(db.getSkillAssumption());
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionC);

		mockEditionsAndSetVisible(true, "STUDENT");

		// Assert that the recent active edition method returns the correct skill
		// The latest visible edition is edition B, so it should return the skill in that edition
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);

		// Assert on the traversal lists
		// The order cannot be asserted on, since the futureEditionSkills is a set
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionC))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
	}

	@Test
	@WithUserDetails("teacher")
	public void testMultipleEditionsWithoutTaskCompletedTeacherEdition() {
		/*
		 * Test scenario in which there are multiple editions, and the most recent edition is not visible, the
		 * logged-in user is however a teacher in these editions, so it should be visible to them regardless.
		 * Edition structure is: editionA --> (editionB and editionC). With editionC being the latest edition.
		 * The skill which the external skill refers to should be "copied" from edition A to editions B and C.
		 * The method should return the most skill of recent edition, which is also visible to the user
		 * (editionC).
		 */

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2, false);

		// Skills in edition B/C should be copies of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		skillEditionC.setPreviousEditionSkill(db.getSkillAssumption());
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionB);
		db.getSkillAssumption().getFutureEditionSkills().add(skillEditionC);

		// Sets the role to "teacher" for all editions
		mockEditionsAndSetVisible(true, "TEACHER");

		// Assert that the recent active edition method returns the correct skill
		// The latest visible edition is edition B, so it should return the skill in that edition
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionC);

		// Assert on the traversal lists
		// The order cannot be asserted on, since the futureEditionSkills is a set
		assertThat(skillService.traverseSkillTree(db.getSkillAssumption()))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionB))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
		assertThat(skillService.traverseSkillTree(skillEditionC))
				.containsExactlyInAnyOrder(db.getSkillAssumption(), skillEditionB, skillEditionC);
	}

}
