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
package nl.tudelft.skills.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.TaskCompletedDTO;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.PathPreferenceRepository;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PersonService;
import nl.tudelft.skills.service.TaskCompletionService;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PersonControllerTest extends ControllerTest {

	private final PersonController personController;
	private final PersonRepository personRepository;
	private final TaskRepository taskRepository;
	private final TaskCompletionService taskCompletionService;
	private final PathPreferenceRepository pathPreferenceRepository;
	private final PersonService personService;

	@Autowired
	public PersonControllerTest(PersonRepository personRepository, TaskRepository taskRepository,
			TaskCompletionService taskCompletionService,
			PathPreferenceRepository pathPreferenceRepository,
			SkillRepository skillRepository,
			PathRepository pathRepository,
			PersonService personService) {
		this.personRepository = personRepository;
		this.personController = new PersonController(taskRepository, personRepository, taskCompletionService,
				skillRepository, pathRepository, personService);
		this.taskRepository = taskRepository;
		this.taskCompletionService = taskCompletionService;
		this.pathPreferenceRepository = pathPreferenceRepository;
		this.personService = personService;
	}

	@Test
	void setTasksCompletedForPerson() {
		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
				.map(TaskCompletion::getTask).toList();
		assertThat(tasksCompleted).doesNotContain(db.getTaskDo10a(), db.getTaskRead10());

		Person person = new Person();
		person.setId(db.getPerson().getId());
		personController.setTasksCompletedForPerson(person,
				List.of(db.getTaskDo10a().getId(), db.getTaskRead10().getId()));

		List<Task> tasksCompletedAfter = db.getPerson().getTaskCompletions().stream()
				.map(TaskCompletion::getTask).toList();
		assertThat(tasksCompletedAfter).contains(db.getTaskDo10a(), db.getTaskRead10());
	}

	@Test
	void updateTaskCompletedForPersonTrue() {
		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
				.map(TaskCompletion::getTask).toList();
		assertThat(tasksCompleted).doesNotContain(db.getTaskDo10a());

		Person person = new Person();
		person.setId(db.getPerson().getId());
		TaskCompletedDTO taskCompletedDTO = personController.updateTaskCompletedForPerson(person,
				db.getTaskDo10a().getId(), true);

		List<Task> tasksCompletedAfter = db.getPerson().getTaskCompletions().stream()
				.map(TaskCompletion::getTask).toList();
		assertThat(tasksCompletedAfter).contains(db.getTaskDo10a());
		assertThat(taskCompletedDTO.getShowSkills()).hasSize(0);
	}

	@Test
	void updateTaskCompletedForPersonFalse() {
		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
				.map(TaskCompletion::getTask).toList();
		assertThat(tasksCompleted).contains(db.getTaskDo11ad());

		Person person = new Person();
		person.setId(db.getPerson().getId());
		TaskCompletedDTO taskCompletedDTO = personController.updateTaskCompletedForPerson(person,
				db.getTaskDo11ad().getId(), false);

		List<Task> tasksCompletedAfter = db.getPerson().getTaskCompletions().stream()
				.map(TaskCompletion::getTask).toList();
		assertThat(tasksCompletedAfter).doesNotContain(db.getTaskDo11ad());
		assertThat(taskCompletedDTO.getShowSkills()).hasSize(0);
	}

	@Test
	void addAllTaskFromCurrentPath() {
		SCPerson person = db.getPerson();

		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
				.edition(db.getEditionRL()).person(db.getPerson()).build();
		pathPreferenceRepository.save(pathPreference);

		Task task = db.getTaskDo12ae();
		personController.addAllTaskFromCurrentPath(person, task);

		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskRead12());
	}

	@Test
	void resetSkill() throws IOException {
		SCPerson person = db.getPerson();
		person.getSkillsModified().add(db.getSkillImplication());
		person.getTasksAdded().add(db.getTaskDo12ae());
		personRepository.save(person);

		Person authPerson = Person.builder().id(person.getId()).build();

		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
				.edition(db.getEditionRL()).person(db.getPerson()).build();
		pathPreferenceRepository.save(pathPreference);

		personController.resetSkill(authPerson, db.getSkillImplication().getId(),
				mock(HttpServletResponse.class));

		//		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskRead12());
		assertThat(db.getPerson().getTasksAdded()).doesNotContain(db.getTaskDo12ae());
		assertThat(db.getPerson().getSkillsModified()).doesNotContain(db.getSkillImplication());
	}

	@Test
	void addTaskToOwnPath() throws IOException {
		SCPerson person = db.getPerson();

		Person authPerson = Person.builder().id(person.getId()).build();

		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
				.edition(db.getEditionRL()).person(db.getPerson()).build();
		pathPreferenceRepository.save(pathPreference);

		personController.addTaskToOwnPath(authPerson, db.getTaskDo12ae().getId());

		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskRead12());
		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskDo12ae());
		assertThat(db.getPerson().getSkillsModified()).contains(db.getSkillImplication());
	}

	@Test
	void removeTaskFromOwnPath() throws IOException {
		SCPerson person = db.getPerson();

		Person authPerson = Person.builder().id(person.getId()).build();

		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
				.edition(db.getEditionRL()).person(db.getPerson()).build();
		pathPreferenceRepository.save(pathPreference);

		personController.removeTaskFromOwnPath(authPerson, db.getTaskRead12().getId());

		assertThat(db.getPerson().getTasksAdded()).doesNotContain(db.getTaskRead12());
		assertThat(db.getPerson().getSkillsModified()).contains(db.getSkillImplication());
	}
}
