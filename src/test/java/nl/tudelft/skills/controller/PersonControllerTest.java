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

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestUserDetailsService;

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

	@Autowired
	public PersonControllerTest(TaskRepository taskRepository, PersonRepository personRepository) {
		this.personRepository = personRepository;
		this.personController = new PersonController(taskRepository, personRepository);
	}

	@Test
	void setTasksCompletedForPerson() {
		assertThat(db.getPerson().getTasksCompleted()).doesNotContain(db.taskDo10a, db.taskDo10a);

		personController.setTasksCompletedForPerson(TestUserDetailsService.id,
				List.of(db.taskDo10a.getId(), db.taskRead10.getId()));

		assertThat(db.getPerson().getTasksCompleted()).contains(db.taskDo10a, db.taskRead10);
	}

	@Test
	void updateTaskCompletedForPersonTrue() {
		assertThat(db.getPerson().getTasksCompleted()).doesNotContain(db.taskDo10a);

		personController.updateTaskCompletedForPerson(TestUserDetailsService.id,
				db.taskDo10a.getId(), true);

		assertThat(db.getPerson().getTasksCompleted()).contains(db.taskDo10a);
	}

	@Test
	void updateTaskCompletedForPersonFalse() {
		assertThat(db.getPerson().getTasksCompleted()).contains(db.taskDo11ad);

		personController.updateTaskCompletedForPerson(TestUserDetailsService.id,
				db.taskDo11ad.getId(), false);

		assertThat(db.getPerson().getTasksCompleted()).doesNotContain(db.taskDo11ad);
	}
}
