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
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.SCCourse;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TaskCompletionServiceTest {

	private final EditionControllerApi editionApi;
	private final TaskCompletionRepository taskCompletionRepository;
	private final TaskCompletionService taskCompletionService;

	private final TestDatabaseLoader db;

	@Autowired
	public TaskCompletionServiceTest(EditionControllerApi editionApi, PersonRepository personRepository,
			TaskCompletionRepository taskCompletionRepository, TestDatabaseLoader db) {
		this.editionApi = editionApi;
		this.taskCompletionRepository = taskCompletionRepository;
		this.db = db;
		this.taskCompletionService = new TaskCompletionService(taskCompletionRepository, personRepository,
				editionApi);
	}

	@Test
	public void testLatestTaskCompletionDoesNotExist() {
		// Reset the person to not have done any tasks yet
		db.resetTaskCompletions();
		SCPerson scPerson = db.getPerson();

		// The person did not complete any tasks yet
		Person person = Person.builder().id(scPerson.getId()).build();
		assertThat(taskCompletionService.latestTaskCompletion(person)).isNull();
		// If the person is null, should also return null
		assertThat(taskCompletionService.latestTaskCompletion(null)).isNull();
	}

	@Test
	public void testLatestTaskCompletionNullTimestamp() {
		// Reset the person to not have done any tasks yet
		db.resetTaskCompletions();
		SCPerson scPerson = db.getPerson();

		// Add a task completion which has null as the timestamp
		TaskCompletion timestampNull = TaskCompletion.builder().task(db.getTaskDo10a()).person(scPerson)
				.timestamp(null).build();
		taskCompletionRepository.save(timestampNull);
		scPerson.getTaskCompletions().add(timestampNull);
		db.getTaskDo10a().getCompletedBy().add(timestampNull);

		// The person has only completed a task with null as the timestamp,
		// this should not be considered the latest task completion
		// Therefore, null is expected
		Person person = Person.builder().id(scPerson.getId()).build();
		assertThat(taskCompletionService.latestTaskCompletion(person)).isNull();
	}

	@Test
	public void testLatestTaskCompletionReturnsCompletion() {
		// In the test database, the most recently completed task by the
		// saved person is taskRead11
		Task expectedLastTask = db.getTaskRead11();

		// Assert that the latest task completion is the most recent one
		Person person = Person.builder().id(db.getPerson().getId()).build();
		assertThat(taskCompletionService.latestTaskCompletion(person)).isEqualTo(expectedLastTask);
	}

	@Test
	public void testGetLocationString() {
		Task task = db.getTaskDo10a();
		SCEdition dbEdition = db.getEditionRL();
		SCCourse dbCourse = db.getCourseRL();
		EditionDetailsDTO editionDetailsDTO = new EditionDetailsDTO().id(dbEdition.getId()).name("edition")
				.course(new CourseSummaryDTO().id(dbCourse.getId()).name("Reasoning and Logic"));

		when(editionApi.getEditionById(db.getEditionRL().getId())).thenReturn(Mono.just(editionDetailsDTO));

		String expected = "Reasoning and Logic - Proof Techniques - Logic Basics - Variables";
		assertThat(taskCompletionService.getLocationString(task)).isEqualTo(expected);
	}

	@Test
	public void testAddTaskCompletion() {
		TaskCompletion completion = taskCompletionService.addTaskCompletion(db.getPerson(),
				db.getTaskDo10a());
		assertThat(taskCompletionRepository.getById(completion.getId())).isEqualTo(completion);
		assertThat(db.getPerson().getTaskCompletions()).contains(completion);
		assertThat(db.getTaskDo10a().getCompletedBy()).contains(completion);
	}

	@Test
	public void testDeleteTaskCompletionExists() {
		TaskCompletion taskCompletion = db.getCompleteRead12();
		assertThat(db.getTaskRead12().getCompletedBy()).containsExactly(taskCompletion);
		assertThat(db.getPerson().getTaskCompletions()).contains(taskCompletion);
		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isPresent();

		TaskCompletion deleted = taskCompletionService.deleteTaskCompletion(db.getPerson(),
				db.getTaskRead12());
		assertThat(deleted).isEqualTo(taskCompletion);

		// The TaskCompletion should be removed from the sets
		assertThat(db.getPerson().getTaskCompletions()).doesNotContain(taskCompletion);
		assertThat(db.getTaskRead10().getCompletedBy()).doesNotContain(taskCompletion);

		// The TaskCompletion should be removed from the repository
		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isEmpty();
	}

	@Test
	public void testDeleteTaskCompletionDoesNotExist() {
		TaskCompletion completion = taskCompletionService.deleteTaskCompletion(db.getPerson(),
				db.getTaskRead10());
		// Should return null since this completion was not saved
		assertThat(completion).isNull();
	}

	@Test
	public void testDeleteTaskCompletionsOfTask() {
		TaskCompletion taskCompletion = db.getCompleteRead12();
		assertThat(db.getTaskRead12().getCompletedBy()).containsExactly(taskCompletion);
		assertThat(db.getPerson().getTaskCompletions()).contains(taskCompletion);
		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isPresent();

		taskCompletionService.deleteTaskCompletionsOfTask(db.getTaskRead12());

		assertThat(db.getTaskRead12().getCompletedBy()).isEmpty();
		assertThat(db.getPerson().getTaskCompletions()).doesNotContain(taskCompletion);
		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isEmpty();
	}
}
