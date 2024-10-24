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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.EditLinkDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.repository.RegularTaskRepository;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TaskControllerTest extends ControllerTest {

	private final TaskController taskController;
	private final RegularTaskRepository regularTaskRepository;
	private final RoleControllerApi roleApi;

	@Autowired
	public TaskControllerTest(RegularTaskRepository regularTaskRepository, RoleControllerApi roleApi) {
		this.regularTaskRepository = regularTaskRepository;
		this.roleApi = roleApi;
		this.taskController = new TaskController(regularTaskRepository);
	}

	private String createBody() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Long taskId = db.getTaskDo10a().getId();

		return objectMapper.writeValueAsString(new EditLinkDTO(taskId, "www.test.com"));
	}

	@Test
	void changeLinkUnauthenticatedForbidden() throws Exception {
		mvc.perform(patch("/task/change-link").with(csrf())
				.content(createBody())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(redirectedUrlPattern("**/auth/login"));
	}

	@Test
	@WithUserDetails("teacher")
	public void updateLinkSuccessful() throws Exception {
		mockRole(roleApi, "TEACHER");

		mvc.perform(patch("/task/change-link").with(csrf())
				.content(createBody())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		RegularTask task = regularTaskRepository.getById(db.getTaskDo10a().getId());
		assertThat(task.getLink()).isEqualTo("www.test.com");
	}

	@Test
	@WithUserDetails("teacher")
	public void updateLinkTaskDoesNotExist() throws Exception {
		mockRole(roleApi, "TEACHER");

		ObjectMapper objectMapper = new ObjectMapper();
		mvc.perform(patch("/task/change-link").with(csrf())
				.content(objectMapper.writeValueAsString(new EditLinkDTO(-1L, "www.test.com")))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("student")
	public void updateLinkUnauthorized() throws Exception {
		mockRole(roleApi, "STUDENT");

		mvc.perform(patch("/task/change-link").with(csrf())
				.content(createBody())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	void getTask() {
		taskController.getTask(db.getTaskRead12().getId(), model);
		assertThat(model.getAttribute("canEdit")).isEqualTo(false);

		assertThat(((TaskViewDTO) model.getAttribute("item")).getPathIds())
				.containsExactly(db.getPathFinderPath().getId());
	}

	@Test
	void getTaskForCustomPath() {
		taskController.getTaskForCustomPath(db.getTaskRead12().getId(), model);
		assertThat(model.getAttribute("canEdit")).isEqualTo(false);

		assertThat(((TaskViewDTO) model.getAttribute("item")).getPathIds())
				.containsExactly(db.getPathFinderPath().getId());
	}
}
