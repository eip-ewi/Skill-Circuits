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

import java.util.List;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.TaskCompletionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PersonControllerTest extends ControllerTest {

	private final PersonController personController;
	private final PersonRepository personRepository;
	private final TaskRepository taskRepository;
	private final TaskCompletionService taskCompletionService;

	@Autowired
	public PersonControllerTest(PersonRepository personRepository, TaskRepository taskRepository,
			TaskCompletionService taskCompletionService) {
		this.personRepository = personRepository;
		this.personController = new PersonController(taskRepository, personRepository, taskCompletionService);
		this.taskRepository = taskRepository;
		this.taskCompletionService = taskCompletionService;
	}

	@Test
	void setTasksCompletedForPerson() {
		// TODO modify to (also?) use TaskCompletion
		assertThat(db.getPerson().getTasksCompleted()).doesNotContain(
				db.getTaskDo10a(),
				db.getTaskRead10());

		Person person = new Person();
		person.setId(db.getPerson().getId());
		personController.setTasksCompletedForPerson(person,
				List.of(db.getTaskDo10a().getId(), db.getTaskRead10().getId()));

		assertThat(db.getPerson().getTasksCompleted()).contains(
				db.getTaskDo10a(),
				db.getTaskRead10());
	}

	@Test
	void updateTaskCompletedForPersonTrue() {
		// TODO modify to (also?) use TaskCompletion
		assertThat(db.getPerson().getTasksCompleted())
				.doesNotContain(db.getTaskDo10a());

		Person person = new Person();
		person.setId(db.getPerson().getId());
		personController.updateTaskCompletedForPerson(person,
				db.getTaskDo10a().getId(), true);

		assertThat(db.getPerson().getTasksCompleted())
				.contains(db.getTaskDo10a());
	}

	@Test
	void updateTaskCompletedForPersonFalse() {
		// TODO modify to (also?) use TaskCompletion
		assertThat(db.getPerson().getTasksCompleted())
				.contains(db.getTaskDo11ad());

		Person person = new Person();
		person.setId(db.getPerson().getId());
		personController.updateTaskCompletedForPerson(person,
				db.getTaskDo11ad().getId(), false);

		assertThat(db.getPerson().getTasksCompleted())
				.doesNotContain(db.getTaskDo11ad());
	}

}
