// /*
//  * Skill Circuits
//  * Copyright (C) 2025 - Delft University of Technology
//  *
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License as
//  * published by the Free Software Foundation, either version 3 of the
//  * License, or (at your option) any later version.
//  *
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
//  */
// package nl.tudelft.skills.controller;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.util.Set;
//
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.CsvSource;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithUserDetails;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
//
// import nl.tudelft.labracore.api.RoleControllerApi;
// import nl.tudelft.skills.TestSkillCircuitsApplication;
// import nl.tudelft.skills.dto.view.EditLinkDTO;
// import nl.tudelft.skills.dto.view.module.ChoiceTaskViewDTO;
// import nl.tudelft.skills.dto.view.module.RegularTaskViewDTO;
// import nl.tudelft.skills.dto.view.module.TaskViewDTO;
// import nl.tudelft.skills.model.RegularTask;
// import nl.tudelft.skills.model.Task;
// import nl.tudelft.skills.repository.RegularTaskRepository;
// import nl.tudelft.skills.repository.TaskRepository;
// import nl.tudelft.skills.security.AuthorisationService;
//
// @Transactional
// @AutoConfigureMockMvc
// @SpringBootTest(classes = TestSkillCircuitsApplication.class)
// public class TaskControllerTest extends ControllerTest {
//
// 	private final TaskController taskController;
// 	private final RegularTaskRepository regularTaskRepository;
// 	private final RoleControllerApi roleApi;
//
// 	@Autowired
// 	public TaskControllerTest(RegularTaskRepository regularTaskRepository, RoleControllerApi roleApi,
// 			AuthorisationService authorisationService, TaskRepository taskRepository) {
// 		this.regularTaskRepository = regularTaskRepository;
// 		this.roleApi = roleApi;
// 		this.taskController = new TaskController(regularTaskRepository, authorisationService, taskRepository);
// 	}
//
// 	private String createBody() throws JsonProcessingException {
// 		ObjectMapper objectMapper = new ObjectMapper();
// 		Long taskId = db.getTaskDo10a().getId();
//
// 		return objectMapper.writeValueAsString(new EditLinkDTO(taskId, "www.test.com"));
// 	}
//
// 	@Test
// 	void changeLinkUnauthenticatedForbidden() throws Exception {
// 		mvc.perform(patch("/task/change-link").with(csrf())
// 				.content(createBody())
// 				.contentType(MediaType.APPLICATION_JSON))
// 				.andExpect(redirectedUrlPattern("**/auth/login"));
// 	}
//
// 	@Test
// 	@WithUserDetails("teacher")
// 	public void updateLinkSuccessful() throws Exception {
// 		mockRole(roleApi, "TEACHER");
//
// 		mvc.perform(patch("/task/change-link").with(csrf())
// 				.content(createBody())
// 				.contentType(MediaType.APPLICATION_JSON))
// 				.andExpect(status().isOk());
//
// 		RegularTask task = regularTaskRepository.getById(db.getTaskDo10a().getId());
// 		assertThat(task.getLink()).isEqualTo("www.test.com");
// 	}
//
// 	@Test
// 	@WithUserDetails("teacher")
// 	public void updateLinkTaskDoesNotExist() throws Exception {
// 		mockRole(roleApi, "TEACHER");
//
// 		ObjectMapper objectMapper = new ObjectMapper();
// 		mvc.perform(patch("/task/change-link").with(csrf())
// 				.content(objectMapper.writeValueAsString(new EditLinkDTO(-1L, "www.test.com")))
// 				.contentType(MediaType.APPLICATION_JSON))
// 				.andExpect(status().isNotFound());
// 	}
//
// 	@Test
// 	@WithUserDetails("student")
// 	public void updateLinkUnauthorized() throws Exception {
// 		mockRole(roleApi, "STUDENT");
//
// 		mvc.perform(patch("/task/change-link").with(csrf())
// 				.content(createBody())
// 				.contentType(MediaType.APPLICATION_JSON))
// 				.andExpect(status().isForbidden());
// 	}
//
// 	@ParameterizedTest
// 	@WithUserDetails("username")
// 	@CsvSource({ "TEACHER", "HEAD_TA" })
// 	void getTaskAtLeastHeadTA(String role) throws Exception {
// 		mockRole(roleApi, role);
// 		Long taskId = db.getTaskRead12().getId();
//
// 		// Check that authentication passes as intended
// 		String content = mvc.perform(get("/task/" + taskId).with(csrf()))
// 				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
// 		// Check that it is a regular task: Contains regular task but not contain choice task
// 		// (a choice task would contain regular tasks)
// 		assertThat(content).contains("name=\"taskType\" value=\"RegularTask\"");
// 		assertThat(content).doesNotContain("name=\"taskType\" value=\"ChoiceTask\"");
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTask(db.getTaskRead12().getId(), model);
// 		assertModelAttributes(RegularTaskViewDTO.class, Set.of(db.getPathFinderPath().getId()));
// 	}
//
// 	@Test
// 	@WithUserDetails("teacher")
// 	void getChoiceTaskTest() throws Exception {
// 		mockRole(roleApi, "TEACHER");
// 		Long taskId = db.getChoiceTaskBookOrVideo().getId();
//
// 		// Check that authentication passes as intended
// 		String content = mvc.perform(get("/task/" + taskId).with(csrf()))
// 				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
// 		// Check that it is a choice task
// 		assertThat(content).contains("name=\"taskType\" value=\"ChoiceTask\"");
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTask(db.getChoiceTaskBookOrVideo().getId(), model);
// 		assertModelAttributes(ChoiceTaskViewDTO.class, Set.of());
// 	}
//
// 	@Test
// 	@WithUserDetails("student")
// 	void getTaskStudentPublished() throws Exception {
// 		mockRole(roleApi, "STUDENT");
// 		Task task = db.getTaskRead12();
// 		task.getSkill().getSubmodule().getModule().getEdition().setVisible(true);
//
// 		// Check that authentication passes as intended
// 		mvc.perform(get("/task/" + task.getId()).with(csrf()))
// 				.andExpect(status().isOk());
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTask(task.getId(), model);
// 		assertModelAttributes(RegularTaskViewDTO.class, Set.of(db.getPathFinderPath().getId()));
// 	}
//
// 	@Test
// 	@WithUserDetails("student")
// 	void getTaskStudent() throws Exception {
// 		mockRole(roleApi, "STUDENT");
// 		Long taskId = db.getTaskRead12().getId();
//
// 		// Check that access is forbidden (edition is hidden)
// 		mvc.perform(get("/task/" + taskId).with(csrf()))
// 				.andExpect(status().isForbidden());
//
// 	}
//
// 	@Test
// 	@WithUserDetails("admin")
// 	void getTaskForCustomPathAdmin() throws Exception {
// 		mockRole(roleApi, "ADMIN");
// 		Long taskId = db.getTaskRead12().getId();
//
// 		// Check that authentication passes as intended
// 		mvc.perform(get("/task/" + taskId + "/preview").with(csrf()))
// 				.andExpect(status().isOk());
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTaskForCustomPath(db.getTaskRead12().getId(), model);
// 		assertModelAttributes(RegularTaskViewDTO.class, Set.of(db.getPathFinderPath().getId()));
// 	}
//
// 	@ParameterizedTest
// 	@WithUserDetails("username")
// 	@CsvSource({ "TEACHER", "HEAD_TA" })
// 	void getTaskForCustomPathAtLeastHeadTA(String role) throws Exception {
// 		mockRole(roleApi, role);
// 		Long taskId = db.getTaskRead12().getId();
//
// 		// Check that authentication passes as intended
// 		String content = mvc.perform(get("/task/" + taskId + "/preview").with(csrf()))
// 				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
// 		// Check that it is not a choice task
// 		assertThat(content).doesNotContain("choice_task");
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTaskForCustomPath(db.getTaskRead12().getId(), model);
// 		assertModelAttributes(RegularTaskViewDTO.class, Set.of(db.getPathFinderPath().getId()));
// 	}
//
// 	@Test
// 	@WithUserDetails("teacher")
// 	void getChoiceTaskCustomPathTest() throws Exception {
// 		mockRole(roleApi, "TEACHER");
// 		Long taskId = db.getChoiceTaskBookOrVideo().getId();
//
// 		// Check that authentication passes as intended
// 		String content = mvc.perform(get("/task/" + taskId + "/preview").with(csrf()))
// 				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
// 		// Check that it is a choice task
// 		assertThat(content).contains("choice_task");
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTask(db.getChoiceTaskBookOrVideo().getId(), model);
// 		assertModelAttributes(ChoiceTaskViewDTO.class, Set.of());
// 	}
//
// 	@Test
// 	@WithUserDetails("student")
// 	void getTaskForCustomPathStudent() throws Exception {
// 		mockRole(roleApi, "STUDENT");
// 		Long taskId = db.getTaskRead12().getId();
//
// 		// Check that access is forbidden (edition is hidden)
// 		mvc.perform(get("/task/" + taskId + "/preview").with(csrf()))
// 				.andExpect(status().isForbidden());
// 	}
//
// 	@Test
// 	@WithUserDetails("student")
// 	void getTaskForCustomPathStudentPublished() throws Exception {
// 		mockRole(roleApi, "STUDENT");
// 		Task task = db.getTaskRead12();
// 		task.getSkill().getSubmodule().getModule().getEdition().setVisible(true);
//
// 		// Check that authentication passes as intended
// 		mvc.perform(get("/task/" + task.getId() + "/preview").with(csrf()))
// 				.andExpect(status().isOk());
//
// 		// Check that model attributes get changed as intended
// 		taskController.getTaskForCustomPath(task.getId(), model);
// 		assertModelAttributes(RegularTaskViewDTO.class, Set.of(db.getPathFinderPath().getId()));
// 	}
//
// 	/**
// 	 * Helper method for tests. Asserts that "canEdit" is false, the view DTO is of a specific subclass and
// 	 * that the paths match the given pathIds.
// 	 *
// 	 * @param taskViewDTOClass The subclass the viewDTO should be.
// 	 * @param pathIds          The ids of the paths the task should be in.
// 	 */
// 	private void assertModelAttributes(Class<?> taskViewDTOClass, Set<Long> pathIds) {
// 		assertThat(model.getAttribute("canEdit")).isEqualTo(false);
// 		TaskViewDTO<?> viewDTO = (TaskViewDTO<?>) model.getAttribute("item");
// 		assertThat(viewDTO).isInstanceOf(taskViewDTOClass);
// 		assertThat(viewDTO.getPathIds()).containsExactlyElementsOf(pathIds);
// 	}
// }
