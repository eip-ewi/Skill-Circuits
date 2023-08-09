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
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.Id;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.EditLinkDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TaskControllerTest extends ControllerTest {

	private final TaskRepository taskRepository;
	private final RoleControllerApi roleApi;

	@Autowired
	public TaskControllerTest(TaskRepository taskRepository, RoleControllerApi roleApi) {
		this.taskRepository = taskRepository;
		this.roleApi = roleApi;
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
		mockRole("TEACHER");

		mvc.perform(patch("/task/change-link").with(csrf())
				.content(createBody())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Task task = taskRepository.getById(db.getTaskDo10a().getId());
		assertThat(task.getLink()).isEqualTo("www.test.com");
	}

	@Test
	@WithUserDetails("teacher")
	public void updateLinkTaskDoesNotExist() throws Exception {
		mockRole("TEACHER");

		ObjectMapper objectMapper = new ObjectMapper();
		mvc.perform(patch("/task/change-link").with(csrf())
				.content(objectMapper.writeValueAsString(new EditLinkDTO(-1L, "www.test.com")))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("student")
	public void updateLinkUnauthorized() throws Exception {
		mockRole("STUDENT");

		mvc.perform(patch("/task/change-link").with(csrf())
				.content(createBody())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	private void mockRole(String role) {
		if (role == null || role.isBlank()) {
			when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.empty());
		} else {
			when(roleApi.getRolesById(anySet(), anySet()))
					.thenReturn(Flux.just(new RoleDetailsDTO()
							.id(new Id().editionId(db.getEditionRL().getId())
									.personId(TestUserDetailsService.id))
							.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
							.type(RoleDetailsDTO.TypeEnum.valueOf(role))));
		}
	}

}
