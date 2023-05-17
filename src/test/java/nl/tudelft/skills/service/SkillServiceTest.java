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
import static org.mockito.ArgumentMatchers.anyLong;

import java.time.LocalDateTime;
import java.util.List;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.ExternalSkillRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SkillServiceTest {

	private final AbstractSkillRepository abstractSkillRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final TaskRepository taskRepository;
	private final ModuleRepository moduleRepository;
	private final SkillService skillService;
	private final CourseControllerApi courseApi;

	private TestDatabaseLoader db;
	private final LocalDateTime localDateTime;

	@Autowired
	public SkillServiceTest(AbstractSkillRepository abstractSkillRepository, TaskRepository taskRepository,
			TestDatabaseLoader db, TaskCompletionRepository taskCompletionRepository,
			EditionControllerApi editionApi, CourseControllerApi courseApi,
			SkillRepository skillRepository, ModuleRepository moduleRepository,
			ExternalSkillRepository externalSkillRepository) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.db = db;
		this.taskCompletionRepository = taskCompletionRepository;
		this.moduleRepository = moduleRepository;
		this.externalSkillRepository = externalSkillRepository;
		this.courseApi = courseApi;
		this.taskRepository = taskRepository;
		this.localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);
		this.skillService = new SkillService(abstractSkillRepository, taskCompletionRepository, editionApi,
				courseApi, skillRepository);
	}

	@Test
	public void deleteSkill() {
		Long id = db.getSkillVariables().getId();

		skillService.deleteSkill(id);
		assertThat(abstractSkillRepository.existsById(id)).isFalse();

		// All TaskCompletions are in other Skills, so they should
		// not have been deleted
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
	public void testRecentActiveEditionOneEdition() {
		// Test scenario in which there is only one edition
		// The method should return that editions skill

		// Create a new module for the external skill, in the same edition
		SCModule module = moduleRepository.save(SCModule.builder().edition(db.getEditionRL())
				.name("New module").build());
		// Create an external skill referencing SkillAssumption
		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(module)
				.row(0).column(0).build();
		externalSkill = externalSkillRepository.save(externalSkill);

		// Mock the response of the courseApi to return the course details
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(new EditionSummaryDTO().id(db.getEditionRL().getId())));
		Mockito.when(courseApi.getCourseByEdition(anyLong())).thenReturn(Mono.just(course));

		// Assert that the recent active edition method returns the correct skill
		// Since there is only one edition, this is the skill in this edition
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(db.getSkillAssumption());
	}

	@Test
	public void testMultipleEditionsNoTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, but no task was completed. Edition structure
		 * is: editionA / \ editionB editionC With editionC being the latest edition. The skill which the
		 * external skill refers to should be "copied" from edition A to editions B and C. The method should
		 * return the latest editions skill (edition C).
		 */

		// Create a new module for the external skill, in the same edition
		SCModule module = moduleRepository.save(SCModule.builder().edition(db.getEditionRL())
				.name("New module").build());
		// Create an external skill referencing SkillAssumption
		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(module)
				.row(0).column(0).build();
		externalSkill = externalSkillRepository.save(externalSkill);

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2);

		// Skills in edition B/C should be copies of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());
		skillEditionC.setPreviousEditionSkill(db.getSkillAssumption());

		// Mock the response of the courseApi to return the course details
		// Edition C should be the most recent edition
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(
						new EditionSummaryDTO().id(idInUse).startDate(localDateTime),
						new EditionSummaryDTO().id(idInUse + 1).startDate(localDateTime.plusDays(2)),
						new EditionSummaryDTO().id(idInUse + 2).startDate(localDateTime.plusDays(4))));
		Mockito.when(courseApi.getCourseByEdition(anyLong())).thenReturn(Mono.just(course));

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
		 * recent editions. Edition structure is: editionA / \ editionB editionC With editionC being the
		 * latest edition. A task was completed in editionB. The skill which the external skill refers to
		 * should be "copied" from edition A to editions B and C. The method should return the editions skill
		 * in which a task was completed.
		 */

		// Create a new module for the external skill, in the same edition
		SCModule module = moduleRepository.save(SCModule.builder().edition(db.getEditionRL())
				.name("New module").build());
		// Create an external skill referencing SkillAssumption
		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(module)
				.row(0).column(0).build();
		externalSkill = externalSkillRepository.save(externalSkill);

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2);

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

		// Mock the response of the courseApi to return the course details
		// Edition C should be the most recent edition
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(
						new EditionSummaryDTO().id(idInUse).startDate(localDateTime),
						new EditionSummaryDTO().id(idInUse + 1).startDate(localDateTime.plusDays(2)),
						new EditionSummaryDTO().id(idInUse + 2).startDate(localDateTime.plusDays(4))));
		Mockito.when(courseApi.getCourseByEdition(anyLong())).thenReturn(Mono.just(course));

		// Assert that the recent active edition method returns the correct skill
		// The person has completed a task in editionB, so it should return that editions skill
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);
	}

	@Test
	public void testMultipleEditionsChainWithTaskCompleted() {
		/*
		 * Test scenario in which there are multiple editions, and a task was completed in one of the less
		 * recent editions. Edition structure is: editionA | editionB | editionC With editionC being the
		 * latest edition. A task was completed in editionB. The skill which the external skill refers to
		 * should be "copied" from edition A to editions B and C. The method should return the editions skill
		 * in which a task was completed.
		 */

		// Create a new module for the external skill, in the same edition
		SCModule module = moduleRepository.save(SCModule.builder().edition(db.getEditionRL())
				.name("New module").build());
		// Create an external skill referencing SkillAssumption
		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(module)
				.row(0).column(0).build();
		externalSkill = externalSkillRepository.save(externalSkill);

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1);
		Skill skillEditionC = db.createSkillInEditionHelper(idInUse + 2);

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

		// Mock the response of the courseApi to return the course details
		// Edition C should be the most recent edition
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(
						new EditionSummaryDTO().id(idInUse).startDate(localDateTime),
						new EditionSummaryDTO().id(idInUse + 1).startDate(localDateTime.plusDays(2)),
						new EditionSummaryDTO().id(idInUse + 2).startDate(localDateTime.plusDays(4))));
		Mockito.when(courseApi.getCourseByEdition(anyLong())).thenReturn(Mono.just(course));

		// Assert that the recent active edition method returns the correct skill
		// The person has completed a task in editionB, so it should return that editions skill
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);
	}

	@Test
	public void testMultipleEditionsMostRecentDoesNotHaveSkill() {
		/*
		 * Test scenario in which there are multiple editions, and the most recent edition does not contain
		 * the parent skill. Edition structure is: editionA | editionB editionC With editionC being the latest
		 * edition, and editionB the one before that. The skill which the external skill refers to should be
		 * "copied" from edition A to editions B, but not C. The method should return the skill in edition B.
		 */

		// Create a new module for the external skill, in the same edition
		SCModule module = moduleRepository.save(SCModule.builder().edition(db.getEditionRL())
				.name("New module").build());
		// Create an external skill referencing SkillAssumption
		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(module)
				.row(0).column(0).build();
		externalSkill = externalSkillRepository.save(externalSkill);

		// Reset the task completions, so that the person has not completed any tasks yet
		db.resetTaskCompletions();

		// Create two new skills, submodules, modules and editions
		// Edition A is the current edition (editionRL)
		Long idInUse = db.getEditionRL().getId();
		Skill skillEditionB = db.createSkillInEditionHelper(idInUse + 1);

		// Skill in edition B should be a copy of skill in edition A
		skillEditionB.setPreviousEditionSkill(db.getSkillAssumption());

		// Mock the response of the courseApi to return the course details
		// Edition C should be the most recent edition
		CourseDetailsDTO course = new CourseDetailsDTO().id(db.getCourseRL().getId())
				.editions(List.of(
						new EditionSummaryDTO().id(idInUse).startDate(localDateTime),
						new EditionSummaryDTO().id(idInUse + 1).startDate(localDateTime.plusDays(2)),
						new EditionSummaryDTO().id(idInUse + 2).startDate(localDateTime.plusDays(4))));
		Mockito.when(courseApi.getCourseByEdition(anyLong())).thenReturn(Mono.just(course));

		// Assert that the recent active edition method returns the correct skill
		// The most recent edition which contains the copied skill is editionB
		assertThat(skillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(), externalSkill))
				.isEqualTo(skillEditionB);
	}

}
