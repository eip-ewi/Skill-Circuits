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
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.RegularTaskCreateDTO;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.create.TaskInfoCreateDTO;
import nl.tudelft.skills.dto.id.CheckpointIdDTO;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.dto.id.SubmoduleIdDTO;
import nl.tudelft.skills.dto.patch.RegularTaskPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.TaskInfoPatchDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.RegularTaskRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SkillServiceTest {

	private final AbstractSkillRepository abstractSkillRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final EditionRepository editionRepository;
	private final RegularTaskRepository regularTaskRepository;
	private final TaskRepository taskRepository;
	private final ChoiceTaskRepository choiceTaskRepository;
	private final ModuleRepository moduleRepository;
	private final PathRepository pathRepository;
	private final SkillRepository skillRepository;
	private final SkillService skillService;
	private AuthorisationService authorisationService;
	private final TaskInfoRepository taskInfoRepository;

	private final RoleControllerApi roleApi;
	private final CourseControllerApi courseApi;
	private final EditionControllerApi editionApi;

	private final PersonRepository personRepository;

	private TestDatabaseLoader db;
	private final LocalDateTime localDateTime;

	private final ClickedLinkService clickedLinkService;
	private final ClickedLinkRepository clickedLinkRepository;

	@Autowired
	public SkillServiceTest(AbstractSkillRepository abstractSkillRepository, PathRepository pathRepository,
			RegularTaskRepository regularTaskRepository,
			TaskRepository taskRepository, ChoiceTaskRepository choiceTaskRepository,
			TestDatabaseLoader db, TaskCompletionRepository taskCompletionRepository,
			EditionControllerApi editionApi, CourseControllerApi courseApi,
			SkillRepository skillRepository, ModuleRepository moduleRepository,
			ExternalSkillRepository externalSkillRepository, EditionRepository editionRepository,
			AuthorisationService authorisationService, TaskCompletionService taskCompletionService,
			RoleControllerApi roleApi, PersonRepository personRepository,
			ClickedLinkService clickedLinkService,
			ClickedLinkRepository clickedLinkRepository, TaskInfoRepository taskInfoRepository) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.externalSkillRepository = externalSkillRepository;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.regularTaskRepository = regularTaskRepository;
		this.taskRepository = taskRepository;
		this.choiceTaskRepository = choiceTaskRepository;
		this.skillRepository = skillRepository;
		this.clickedLinkService = clickedLinkService;
		this.clickedLinkRepository = clickedLinkRepository;
		this.personRepository = personRepository;
		this.pathRepository = pathRepository;
		this.taskInfoRepository = taskInfoRepository;

		// The service is not mocked to test the specifics of whether an edition is shown because it
		// is visible, or because the person is at least a teacher in the edition
		this.authorisationService = authorisationService;

		this.db = db;
		this.localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);

		this.roleApi = roleApi;
		this.courseApi = courseApi;
		this.editionApi = editionApi;

		this.skillService = new SkillService(abstractSkillRepository, taskCompletionRepository,
				taskCompletionService,
				courseApi, authorisationService, clickedLinkService, personRepository, regularTaskRepository,
				taskRepository, choiceTaskRepository,
				pathRepository, taskInfoRepository, skillRepository);
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
	public void deleteSkillClickedLinksImpacted() {
		Long id = db.getSkillNegation().getId();

		assertThat(clickedLinkRepository.findAll()).hasSize(3);
		skillService.deleteSkill(id);
		assertThat(clickedLinkRepository.findAll()).hasSize(1);
	}

	@Test
	public void deleteSkillRequiredForImpacted() {
		// Make skill a hidden skill
		Skill skill = db.getSkillNegation();
		RegularTask taskRequired = db.getTaskRead12();
		skill.setHidden(true);
		skill.getRequiredTasks().add(taskRequired);
		taskRequired.getRequiredFor().add(skill);
		abstractSkillRepository.save(skill);
		regularTaskRepository.save(taskRequired);

		// Delete skill
		Long id = skill.getId();
		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// The connection to the task that was required to make the skill appear, should have been removed
		assertThat(db.getTaskRead12().getRequiredFor()).doesNotContain(skill);
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
		RegularTask task = db.createTaskBySkillAndName(skillEditionB, "Task");
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
		RegularTask task = db.createTaskBySkillAndName(skillEditionB, "Task");
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
		RegularTask task = db.createTaskBySkillAndName(db.getSkillAssumption(), "Task");
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

	@Test
	void createSkillTest() {
		// TODO when implemented, add a ChoiceTask test
		// Create a skill with two tasks
		RegularTaskCreateDTO taskDtoA = RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("Test Task A").type(TaskType.VIDEO).link("link")
						.time(10).build())
				.skill(new SkillIdDTO()).index(0).build();
		RegularTaskCreateDTO taskDtoB = RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("Test Task B").type(TaskType.VIDEO).link("link")
						.time(10).build())
				.skill(new SkillIdDTO()).index(1).build();
		SkillCreateDTO skillCreateDTO = SkillCreateDTO.builder()
				.name("Test Skill")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.checkpoint(new CheckpointIdDTO(db.getCheckpointLectureOne().getId()))
				.requiredTaskIds(Collections.emptyList())
				.column(10).row(11).newItems(List.of(taskDtoA, taskDtoB)).build();

		skillService.createSkill(skillCreateDTO);

		Optional<Skill> skill = skillRepository.findAll().stream()
				.filter(s -> s.getName().equals("Test Skill"))
				.findFirst();
		Optional<RegularTask> taskA = regularTaskRepository.findAll().stream()
				.filter(t -> t.getName().equals("Test Task A")).findFirst();
		Optional<RegularTask> taskB = regularTaskRepository.findAll().stream()
				.filter(t -> t.getName().equals("Test Task B")).findFirst();
		assertThat(skill).isNotEmpty();
		assertThat(taskA).isNotEmpty();
		assertThat(taskB).isNotEmpty();

		// Assert on tasks (ordering should be reversed)
		assertOnRegularTaskAttributes(taskA.get(), "Test Task A", 1, skill.get(),
				Set.of(db.getPathFinderPath()));
		assertOnRegularTaskAttributes(taskB.get(), "Test Task B", 0, skill.get(),
				Set.of(db.getPathFinderPath()));

		// Assert on skill
		assertThat(skill.get().getTasks()).containsExactly(taskB.get(), taskA.get());
		assertThat(skill.get().getCheckpoint()).isEqualTo(db.getCheckpointLectureOne());
		assertThat(db.getCheckpointLectureOne().getSkills()).contains(skill.get());
		assertThat(skill.get().getSubmodule()).isEqualTo(db.getSubmoduleCases());
		assertThat(db.getSubmoduleCases().getSkills()).contains(skill.get());
		assertThat(skill.get().getRequiredTasks()).isEmpty();
		assertThat(skill.get().getName()).isEqualTo("Test Skill");
		assertThat(skill.get().getColumn()).isEqualTo(10);
		assertThat(skill.get().getRow()).isEqualTo(11);
	}

	@Test
	void patchSkillTest() {
		// TODO when implemented, add a ChoiceTask test
		// Changes made: Task removal, task addition, task patch, change task ordering,
		// hide and rename skill, add required task

		Skill oldSkill = db.getSkillVariables();
		RegularTask oldTask = db.getTaskRead10();
		RegularTask removedTask = db.getTaskDo10a();

		// Set old task index to 0
		oldTask.setIdx(0);
		oldTask = taskRepository.save(oldTask);

		// Create DTOs
		RegularTaskCreateDTO newTaskDto = RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("Test Task").type(TaskType.VIDEO).link("link")
						.time(10).build())
				.skill(new SkillIdDTO(oldSkill.getId())).index(1).build();
		RegularTaskPatchDTO oldTaskPatchDto = RegularTaskPatchDTO.builder()
				.taskInfo(TaskInfoPatchDTO.builder().name("Patched Task")
						.time(10).type(TaskType.VIDEO).link("link").build())
				.id(oldTask.getId()).skill(new SkillIdDTO(oldSkill.getId())).index(0).build();

		// Patch skill
		skillService.patchSkill(SkillPatchDTO.builder()
				.id(oldSkill.getId())
				.name("Patched Skill")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.items(List.of(oldTaskPatchDto))
				.newItems(List.of(newTaskDto))
				.removedItems(Set.of(removedTask.getId()))
				.requiredTaskIds(List.of(db.getTaskRead12().getId()))
				.hidden(true)
				.build());

		Skill newSkill = db.getSkillVariables();
		Optional<RegularTask> addedTask = regularTaskRepository.findAll().stream()
				.filter(t -> t.getName().equals("Test Task"))
				.findFirst();
		Optional<RegularTask> patchedTask = newSkill.getTasks().stream()
				.filter(t -> t instanceof RegularTask r && r.getName().equals("Patched Task"))
				.map(t -> (RegularTask) t)
				.findFirst();

		// Check task related properties (task ordering should be reversed)
		assertThat(addedTask).isNotEmpty();
		assertThat(patchedTask).isNotEmpty();
		assertThat(taskRepository.findById(removedTask.getId())).isEmpty();
		assertThat(taskInfoRepository.findById(removedTask.getTaskInfo().getId())).isEmpty();
		assertOnRegularTaskAttributes(addedTask.get(), "Test Task", 0, newSkill,
				Set.of(db.getPathFinderPath()));
		assertOnRegularTaskAttributes(patchedTask.get(), "Patched Task", 1, newSkill,
				Set.of());
		assertThat(db.getSkillVariablesHidden().getRequiredTasks()).doesNotContain(removedTask);

		// Assert on skill attributes
		assertThat(newSkill.getTasks()).containsExactly(addedTask.get(), patchedTask.get());
		assertThat(newSkill.isHidden()).isTrue();
		assertThat(newSkill.getRequiredTasks()).containsExactly(db.getTaskRead12());
		assertThat(newSkill.getName()).isEqualTo("Patched Skill");
		assertThat(newSkill.getColumn()).isEqualTo(oldSkill.getColumn());
		assertThat(newSkill.getRow()).isEqualTo(oldSkill.getRow());

		// Check that all TaskCompletion related information remains the same
		assertThat(taskCompletionRepository.findAll()).hasSize(4);
		assertThat(db.getPerson().getTaskCompletions()).hasSize(4);
		assertThat(db.getTaskRead12().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskDo12ae().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskRead11().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskDo11ad().getCompletedBy()).hasSize(1);
	}

	@Test
	void saveNewTasksTestRegularTask() {
		// TODO when implemented, add a ChoiceTask test
		Skill skill = db.getSkillImplication();
		RegularTaskCreateDTO regularTaskDTO = RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("Test Task").time(10).type(TaskType.VIDEO)
						.link("link").build())
				.skill(SkillIdDTO.builder().id(skill.getId()).build()).index(4).build();

		// Save task
		List<Task> tasks = skillService.saveNewTasks(skill, List.of(regularTaskDTO));

		// Assert on the task attributes
		assertThat(tasks).hasSize(1);
		Optional<RegularTask> createdRegularTask = tasks.stream()
				.filter(t -> t instanceof RegularTask regularTask
						&& regularTask.getName().equals("Test Task"))
				.map(t -> (RegularTask) t)
				.findFirst();
		assertThat(createdRegularTask).isNotEmpty();
		assertOnRegularTaskAttributes(createdRegularTask.get(), "Test Task", 4, skill,
				Set.of(db.getPathFinderPath()));
	}

	@Test
	void saveTasksFromRegularTaskDtoTest() {
		Skill skill = db.getSkillImplication();
		RegularTaskCreateDTO regularTaskDTO = RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("Test Task").time(10).type(TaskType.VIDEO)
						.link("link").build())
				.skill(SkillIdDTO.builder().id(skill.getId()).build()).index(4).build();

		// Save task
		RegularTask task = skillService.saveTaskFromRegularTaskDto(regularTaskDTO, skill,
				Set.of(db.getPathFinderPath()));

		// Assert on the task attributes
		assertOnRegularTaskAttributes(task, "Test Task", 4, skill, Set.of(db.getPathFinderPath()));
	}

	@Test
	void patchTasksTestRegularTask() {
		// TODO when implemented, add a ChoiceTask test
		Skill skill = db.getSkillImplication();
		Long taskId = db.getTaskDo12ae().getId();
		RegularTaskPatchDTO regularTaskDTO = RegularTaskPatchDTO.builder()
				.id(taskId)
				.taskInfo(TaskInfoPatchDTO.builder().name("Test Task").time(10).type(TaskType.VIDEO)
						.link("link").build())
				.skill(SkillIdDTO.builder().id(skill.getId()).build()).index(4).build();

		// Patch task
		List<Task> tasks = skillService.patchTasks(skill, Set.of(db.getPathFinderPath()),
				List.of(regularTaskDTO));

		// Assert on the task attributes
		assertThat(tasks).hasSize(1);
		Optional<RegularTask> createdRegularTask = tasks.stream()
				.filter(t -> t instanceof RegularTask regularTask
						&& regularTask.getName().equals("Test Task"))
				.map(t -> (RegularTask) t)
				.findFirst();
		assertThat(createdRegularTask).isNotEmpty();
		assertThat(regularTaskRepository.findByIdOrThrow(taskId)).isEqualTo(createdRegularTask.get());
		// Paths should not be updated since they should only be used for new tasks
		assertOnRegularTaskAttributes(createdRegularTask.get(), "Test Task", 4, skill, Set.of());
	}

	@Test
	void removeTasksTestModified() {
		// TODO when implemented, add ChoiceTask tests
		// Add a skill modification
		Skill skill = db.getSkillImplication();
		RegularTask task = db.getTaskRead12();
		SCPerson person = db.getPerson();
		person.getTasksAdded().add(task);
		person.getSkillsModified().add(skill);
		task.getPersonsThatAddedTask().add(person);
		skill.getPersonModifiedSkill().add(person);
		personRepository.save(person);
		skill = skillRepository.save(skill);

		// Assert on modification
		RegularTask taskModified = taskRepository.save(task);
		assertThat(taskModified.getPersonsThatAddedTask()).hasSize(1);
		assertThat(skill.getPersonModifiedSkill()).hasSize(1);

		// Remove task
		skillService.removeTasks(skill, Set.of(task.getId()));

		// Assert that modification was removed
		assertThat(taskRepository.findById(task.getId())).isEmpty();
		assertThat(taskInfoRepository.findById(task.getTaskInfo().getId())).isEmpty();
		Set<SCPerson> addedTask = personRepository.findAll().stream()
				.filter(p -> p.getTasksAdded().contains(taskModified)).collect(Collectors.toSet());
		assertThat(addedTask).isEmpty();
		assertThat(db.getSkillImplication().getTasks()).doesNotContain(task);
	}

	@Test
	void removeTasksTestLinkAndCompletion() {
		Skill skill = db.getSkillImplication();
		RegularTask task = db.getTaskRead12();
		assertThat(task.getCompletedBy()).hasSize(1);
		Set<ClickedLink> clickedLinksBefore = clickedLinkRepository.findAll().stream()
				.filter(c -> c.getTask().equals(task)).collect(Collectors.toSet());
		assertThat(clickedLinksBefore).hasSize(1);

		skillService.removeTasks(skill, Set.of(task.getId()));

		assertThat(taskRepository.findById(task.getId())).isEmpty();
		assertThat(taskInfoRepository.findById(task.getTaskInfo().getId())).isEmpty();
		Set<TaskCompletion> completions = taskCompletionRepository.findAll().stream()
				.filter(c -> c.getTask().equals(task)).collect(Collectors.toSet());
		assertThat(completions).isEmpty();
		Set<ClickedLink> clickedLinksAfter = clickedLinkRepository.findAll().stream()
				.filter(c -> c.getTask().equals(task)).collect(Collectors.toSet());
		assertThat(clickedLinksAfter).isEmpty();
		assertThat(db.getSkillImplication().getTasks()).doesNotContain(task);
	}

	@Test
	void removeTasksTestRequiredForHidden() {
		Skill skill = db.getSkillVariables();
		RegularTask task = db.getTaskRead10();
		assertThat(task.getRequiredFor()).containsExactly(db.getSkillVariablesHidden());

		skillService.removeTasks(skill, Set.of(task.getId()));

		assertThat(taskRepository.findById(task.getId())).isEmpty();
		assertThat(taskInfoRepository.findById(task.getTaskInfo().getId())).isEmpty();
		Set<Skill> requiredFor = skillRepository.findAll().stream()
				.filter(s -> s.getRequiredTasks().contains(task)).collect(Collectors.toSet());
		assertThat(requiredFor).isEmpty();
		assertThat(db.getSkillVariables().getTasks()).doesNotContain(task);
	}

	@Test
	void removeTasksTestSimpleTasks() {
		Skill skill = db.getSkillImplication();
		RegularTask taskA = db.createTaskBySkillAndName(skill, "Task A");
		RegularTask taskB = db.createTaskBySkillAndName(skill, "Task B");

		skillService.removeTasks(skill, Set.of(taskA.getId(), taskB.getId()));

		assertThat(taskRepository.findById(taskA.getId())).isEmpty();
		assertThat(taskInfoRepository.findById(taskA.getTaskInfo().getId())).isEmpty();
		assertThat(taskRepository.findById(taskB.getId())).isEmpty();
		assertThat(taskInfoRepository.findById(taskB.getTaskInfo().getId())).isEmpty();
		assertThat(db.getSkillImplication().getTasks()).doesNotContain(taskA, taskB);
	}

	@Test
	void patchRequiredTasksTestNotHidden() {
		// Pass one task as required task
		skillService.patchRequiredTasks(db.getSkillVariables(), Set.of(), Set.of(db.getTaskRead12()));

		// A non-hidden skill should not have any required tasks
		assertThat(db.getSkillVariables().getRequiredTasks()).isEmpty();
		assertThat(db.getTaskRead12().getRequiredFor()).isEmpty();
	}

	@Test
	void patchRequiredTasksTestHiddenNoPreviousRequirement() {
		// Hide skill
		Skill skill = db.getSkillVariables();
		skill.setHidden(true);
		skill = skillRepository.save(skill);

		// Pass one task as required task
		skillService.patchRequiredTasks(skill, Set.of(), Set.of(db.getTaskRead12()));

		// Assert that the task was added as requirement
		Skill skillAfter = db.getSkillVariables();
		Task taskAfter = db.getTaskRead12();
		assertThat(skillAfter.getRequiredTasks()).containsExactly(taskAfter);
		assertThat(taskAfter.getRequiredFor()).containsExactly(skillAfter);
	}

	@Test
	void patchRequiredTasksTestHiddenSetToNoRequirement() {
		Skill skill = db.getSkillVariablesHidden();
		Task taskA = db.getTaskRead10();
		Task taskB = db.getTaskDo10a();
		assertThat(skill.getRequiredTasks()).containsExactlyInAnyOrder(taskA, taskB);

		// Pass no required tasks
		skillService.patchRequiredTasks(skill, Set.of(taskA, taskB), Set.of());

		// Assert that there are no required tasks anymore
		Skill skillAfter = db.getSkillVariablesHidden();
		assertThat(skillAfter.getRequiredTasks()).isEmpty();
		assertThat(db.getTaskRead10().getRequiredFor()).isEmpty();
		assertThat(db.getTaskDo10a().getRequiredFor()).isEmpty();
	}

	@Test
	void patchRequiredTasksTestHiddenOverlappingRequirement() {
		Skill skill = db.getSkillVariablesHidden();
		Task taskA = db.getTaskRead10();
		Task taskB = db.getTaskDo10a();
		assertThat(skill.getRequiredTasks()).containsExactlyInAnyOrder(taskA, taskB);

		// Remove one task and add one task
		skillService.patchRequiredTasks(skill, Set.of(taskA, taskB), Set.of(taskA, db.getTaskRead12()));

		// Assert that the requirements are correct
		Skill skillAfter = db.getSkillVariablesHidden();
		assertThat(skillAfter.getRequiredTasks()).containsExactlyInAnyOrder(db.getTaskRead10(),
				db.getTaskRead12());
		assertThat(db.getTaskRead10().getRequiredFor()).containsExactly(skillAfter);
		assertThat(db.getTaskRead12().getRequiredFor()).containsExactly(skillAfter);
		assertThat(db.getTaskDo10a().getRequiredFor()).isEmpty();
	}

	/**
	 * Helper method to assert on attributes of a created or patched regular task with pre-defined values.
	 *
	 * @param task  The task to assert on.
	 * @param idx   The index the task should have.
	 * @param name  The name the task should have.
	 * @param skill The skill in which the task should be.
	 * @param paths The paths in which the task should be.
	 */
	void assertOnRegularTaskAttributes(RegularTask task, String name, int idx, Skill skill, Set<Path> paths) {
		assertThat(task.getName()).isEqualTo(name);
		assertThat(task.getTime()).isEqualTo(10);
		assertThat(task.getType()).isEqualTo(TaskType.VIDEO);
		assertThat(task.getLink()).isEqualTo("link");
		assertThat(task.getIdx()).isEqualTo(idx);
		assertThat(task.getSkill()).isEqualTo(skill);
		assertThat(task.getPaths()).containsExactlyInAnyOrderElementsOf(paths);
	}
}
