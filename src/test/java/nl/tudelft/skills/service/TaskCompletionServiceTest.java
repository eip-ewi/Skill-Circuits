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

import java.util.Optional;

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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TaskCompletionServiceTest {

	private final EditionControllerApi editionApi;
	private final PersonRepository personRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final TaskCompletionService taskCompletionService;

	private final TestDatabaseLoader db;

	@Autowired
	public TaskCompletionServiceTest(EditionControllerApi editionApi, PersonRepository personRepository,
			TaskCompletionRepository taskCompletionRepository, TestDatabaseLoader db) {
		this.editionApi = editionApi;
		this.personRepository = personRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.db = db;
		this.taskCompletionService = new TaskCompletionService(taskCompletionRepository, personRepository,
				editionApi);
	}

	@Test
	public void testLatestTaskCompletionDoesNotExist() {
		personRepository.save(SCPerson.builder().id(1L).build());
		assertThat(personRepository.findById(1L)).isPresent();
		// The person did not complete any tasks yet
		Person person = Person.builder().id(1L).build();
		assertThat(taskCompletionService.latestTaskCompletion(person)).isNull();
		// If the person is null, should also return null
		assertThat(taskCompletionService.latestTaskCompletion(null)).isNull();
	}

	@Test
	public void testLatestTaskCompletionReturnsCompletion() {
		// In the test database, the most recently completed task by the
		// saved person is taskRead10
		Task expectedLastTask = db.getCompleteRead10().getTask();

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
				db.getTaskDo12ae());
		assertThat(taskCompletionRepository.getById(completion.getId())).isEqualTo(completion);
		assertThat(db.getPerson().getTaskCompletions()).contains(completion);
		assertThat(db.getTaskDo12ae().getCompletedBy()).contains(completion);
	}

	@Test
	public void testDeleteTaskCompletionExists() {
		TaskCompletion completion = taskCompletionRepository.save(
				TaskCompletion.builder().person(db.getPerson()).task(db.getTaskDo11ad()).build());
		Long completionId = completion.getId();
		assertThat(taskCompletionRepository.getById(completionId)).isEqualTo(completion);
		db.getPerson().getTaskCompletions().add(completion);
		db.getTaskDo11ad().getCompletedBy().add(completion);

		TaskCompletion deleted = taskCompletionService.deleteTaskCompletion(db.getPerson(),
				db.getTaskDo11ad());
		assertThat(deleted).isEqualTo(completion);

		// The TaskCompletion should be removed from the sets
		assertThat(db.getPerson().getTaskCompletions()).doesNotContain(completion);
		assertThat(db.getTaskDo11ad().getCompletedBy()).doesNotContain(completion);

		// The TaskCompletion should be removed from the repository
		Optional<TaskCompletion> retrieved = taskCompletionRepository.findById(completionId);
		assertThat(retrieved.isEmpty()).isTrue();
	}

	@Test
	public void testDeleteTaskCompletionDoesNotExist() {
		TaskCompletion completion = taskCompletionService.deleteTaskCompletion(db.getPerson(),
				db.getTaskRead12());
		// Should return null since this completion was not saved
		assertThat(completion).isNull();
	}

}
