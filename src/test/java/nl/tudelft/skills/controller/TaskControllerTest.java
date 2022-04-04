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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskType;
import nl.tudelft.skills.repository.TaskRepository;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TaskControllerTest extends ControllerTest {

	private final TaskController taskController;
	private final TaskRepository taskRepository;

	@Autowired
	public TaskControllerTest(TaskRepository taskRepository) {
		this.taskController = new TaskController(taskRepository);
		this.taskRepository = taskRepository;
	}

	@Test
	@WithUserDetails("admin")
	void createTask() throws Exception {
		String element = mvc.perform(post("/task").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("name", "Task"),
						new BasicNameValuePair("skill.id", Long.toString(db.skillVariables.getId()))))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher idMatcher = Pattern.compile("id=\"item-(\\d+)\"").matcher(element);
		assertThat(idMatcher.find()).isTrue();

		Long id = Long.parseLong(idMatcher.group(1));
		assertThat(taskRepository.existsById(id)).isTrue();

		assertThat(element)
				.contains("<span id=\"item-" + id + "-name\" class=\"item__name\">Task</span>");
	}

	@Test
	void patchTask() {
		taskController.patchTask(TaskPatchDTO.builder()
				.id(db.taskDo10a.getId())
				.name("Updated")
				.type(TaskType.EXERCISE)
				.build());

		Task task = taskRepository.findByIdOrThrow(db.taskDo10a.getId());
		assertThat(task.getName()).isEqualTo("Updated");
	}

	@Test
	void deleteTask() {
		taskController.deleteTask(db.taskDo10a.getId());
		assertThat(taskRepository.existsById(db.taskDo10a.getId())).isFalse();
	}

	@Test
	void endpointsAreProtected() throws Exception {
		mvc.perform(patch("/task/{id}", db.taskDo10a.getId()))
				.andExpect(status().isForbidden());
		mvc.perform(post("/task"))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/task?id=1"))
				.andExpect(status().isForbidden());
	}

}
