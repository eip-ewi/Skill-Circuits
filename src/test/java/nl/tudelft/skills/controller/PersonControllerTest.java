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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.TaskCompletedDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.repository.ClickedLinkRepository;
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
	private final ClickedLinkRepository clickedLinkRepository;

	@Autowired
	public PersonControllerTest(PersonRepository personRepository, TaskRepository taskRepository,
			TaskCompletionService taskCompletionService, ClickedLinkRepository clickedLinkRepository) {
		this.personRepository = personRepository;
		this.personController = new PersonController(taskRepository, personRepository, taskCompletionService,
				clickedLinkRepository);
		this.taskRepository = taskRepository;
		this.taskCompletionService = taskCompletionService;
		this.clickedLinkRepository = clickedLinkRepository;
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
	void logClickedLinkByPerson() {
		Person person = new Person();
		person.setId(db.getPerson().getId());
		Long taskId = db.getTaskRead11().getId();
		String link = db.getTaskRead11().getLink();
		personController.logClickedLinkByPerson(person, taskId, link);
		assertTrue(clickedLinkRepository.findAll().stream().anyMatch(s -> s.getLink().equals(link)));
	}

}
