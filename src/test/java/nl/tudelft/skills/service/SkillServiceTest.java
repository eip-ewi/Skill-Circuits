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

import java.time.LocalDateTime;
import java.util.List;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

	private final CourseControllerApi courseApi;
	private final EditionControllerApi editionApi;

	private TestDatabaseLoader db;
	private final LocalDateTime localDateTime;

	@Autowired
	public SkillServiceTest(AbstractSkillRepository abstractSkillRepository, TaskRepository taskRepository,
			TestDatabaseLoader db, TaskCompletionRepository taskCompletionRepository,
			EditionControllerApi editionApi, CourseControllerApi courseApi,
			SkillRepository skillRepository, ModuleRepository moduleRepository,
			ExternalSkillRepository externalSkillRepository, EditionRepository editionRepository) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.externalSkillRepository = externalSkillRepository;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.taskRepository = taskRepository;
		this.skillRepository = skillRepository;

		this.db = db;
		this.localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);

		this.courseApi = courseApi;
		this.editionApi = editionApi;

		this.skillService = new SkillService(abstractSkillRepository, taskCompletionRepository, editionApi,
				courseApi, skillRepository, editionRepository);
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
		newerEditionSkill.setPreviousEditionSkill(previousEditionSkill);
		newerEditionSkill = abstractSkillRepository.save(newerEditionSkill);

		assertThat(skillRepository.findByPreviousEditionSkill(previousEditionSkill))
				.containsExactly(newerEditionSkill);

		// Delete the newer editions skill, which references the previous editions skill
		Long id = newerEditionSkill.getId();
		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// Assert that the "future" edition skill is null after it was deleted.
		assertThat(skillRepository.findByPreviousEditionSkill(previousEditionSkill)).isEmpty();

		// All TaskCompletions are in other Skills, so they should not have been deleted
		assertThat(taskCompletionRepository.findAll()).hasSize(4);
	}

	/**
	 * Creates an external skill for testing purposes.
	 *
	 * @return The created external skill.
	 */
	private ExternalSkill createExternalSkill() {
		// Create a new module for the external skill, in the same edition
		SCModule module = moduleRepository.save(SCModule.builder().edition(db.getEditionRL())
				.name("New module").build());
		// Create an external skill referencing SkillAssumption
		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(module)
				.row(0).column(0).build();
		externalSkill = externalSkillRepository.save(externalSkill);

		return externalSkill;
	}

	/**
	 * Mocks the responses of the courseApi and editionApi for three active courses. Sets visibility of
	 * editionRL to visible.
	 */
	private void mockActiveEditionsAndSetVisible() {
		Long idInUse = db.getEditionRL().getId();

		// Mock the response of the courseApi to return the course details
		// Edition C should be the most recent edition
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(
						new EditionSummaryDTO().id(idInUse).startDate(localDateTime),
						new EditionSummaryDTO().id(idInUse + 1).startDate(localDateTime.plusDays(2)),
						new EditionSummaryDTO().id(idInUse + 2).startDate(localDateTime.plusDays(4))));
		Mockito.when(courseApi.getCourseByEdition(db.getEditionRL().getId())).thenReturn(Mono.just(course));

		// Mock response so that the editions are all also active
		Mockito.when(editionApi.getAllEditionsActiveOrTaughtBy(db.getPerson().getId()))
				.thenReturn(Flux.fromIterable(List.of(
						new EditionDetailsDTO().id(idInUse),
						new EditionDetailsDTO().id(idInUse + 1),
						new EditionDetailsDTO().id(idInUse + 2))));

		// Set edition to be visible
		db.getEditionRL().setVisible(true);
		editionRepository.save(db.getEditionRL());
	}

	@Test
	public void testRecentActiveEditionOneEdition() {
		// Test scenario in which there is only one edition
		// The method should return that editions skill

		ExternalSkill externalSkill = createExternalSkill();

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

		// Assert that the recent active edition method returns the correct skill
		// Since there is only one edition, this is the skill in this edition
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(db.getSkillAssumption());
	}

	@Test
	public void testMultipleEditionsNoTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, but no task was completed. Edition structure
		 * is: editionA --> (editionB and editionC). With editionC being the latest edition. The skill which
		 * the external skill refers to should be "copied" from edition A to editions B and C. The method
		 * should return the latest editions skill (edition C).
		 */

		ExternalSkill externalSkill = createExternalSkill();

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

		mockActiveEditionsAndSetVisible();

		// Assert that the recent active edition method returns the correct skill
		// The person has not completed any tasks in any edition yet, so it should return the most recent
		// editions skill (skillEditionC)
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionC);
	}

	@Test
	public void testMultipleEditionsWithTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, and a task was completed in one of the less
		 * recent editions. Edition structure is: editionA --> (editionB and editionC). With editionC being
		 * the latest edition. A task was completed in editionB. The skill which the external skill refers to
		 * should be "copied" from edition A to editions B and C. The method should return the editions skill
		 * in which a task was completed.
		 */

		ExternalSkill externalSkill = createExternalSkill();

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

		// Set a task in skillEditionB to be completed by the person
		Task task = Task.builder().skill(skillEditionB).name("Task").build();
		task = taskRepository.save(task);
		TaskCompletion taskCompletion = TaskCompletion.builder().task(task).person(db.getPerson()).build();
		taskCompletion = taskCompletionRepository.save(taskCompletion);
		task.getCompletedBy().add(taskCompletion);
		db.getPerson().getTaskCompletions().add(taskCompletion);

		mockActiveEditionsAndSetVisible();

		// Assert that the recent active edition method returns the correct skill
		// The person has completed a task in editionB, so it should return that editions skill
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);
	}

	@Test
	public void testMultipleEditionsChainWithTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, and a task was completed in one of the less
		 * recent editions. Edition structure is: editionA --> editionB --> editionC. With editionC being the
		 * latest edition. A task was completed in editionB. The skill which the external skill refers to
		 * should be "copied" from edition A to editions B and C. The method should return the editions skill
		 * in which a task was completed.
		 */

		ExternalSkill externalSkill = createExternalSkill();

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

		// Set a task in skillEditionB to be completed by the person
		Task task = Task.builder().skill(skillEditionB).name("Task").build();
		task = taskRepository.save(task);
		TaskCompletion taskCompletion = TaskCompletion.builder().task(task).person(db.getPerson()).build();
		taskCompletion = taskCompletionRepository.save(taskCompletion);
		task.getCompletedBy().add(taskCompletion);
		db.getPerson().getTaskCompletions().add(taskCompletion);

		mockActiveEditionsAndSetVisible();

		// Assert that the recent active edition method returns the correct skill
		// The person has completed a task in editionB, so it should return that editions skill
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);
	}

	@Test
	public void testMultipleEditionsMostRecentDoesNotHaveSkill() {
		/*
		 * Test scenario in which there are multiple editions, and the most recent edition does not contain
		 * the parent skill. Edition structure is: editionA --> editionB, editionC (independent). With
		 * editionC being the latest edition, and editionB the one before that. The skill which the external
		 * skill refers to should be "copied" from edition A to editions B, but not C. The method should
		 * return the skill in edition B.
		 */

		ExternalSkill externalSkill = createExternalSkill();

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1, true);

		// Skill in edition B should be a copy of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());

		// Create empty edition C
		editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		mockActiveEditionsAndSetVisible();

		// Assert that the recent active edition method returns the correct skill
		// The most recent edition which contains the copied skill is editionB
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);
	}

}
