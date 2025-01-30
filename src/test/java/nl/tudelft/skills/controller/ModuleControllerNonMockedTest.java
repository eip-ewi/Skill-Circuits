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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.ProgramSummaryDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.ClickedLinkRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class ModuleControllerNonMockedTest extends ControllerTest {
	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final ClickedLinkRepository clickedLinkRepository;
	private final RoleControllerApi roleApi;
	private final EditionControllerApi editionApi;
	private final CourseControllerApi courseApi;

	@Autowired
	public ModuleControllerNonMockedTest(RoleControllerApi roleApi, ModuleRepository moduleRepository,
			EditionRepository editionRepository, TaskCompletionRepository taskCompletionRepository,
			ClickedLinkRepository clickedLinkRepository, EditionControllerApi editionApi,
			CourseControllerApi courseApi) {
		this.roleApi = roleApi;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.clickedLinkRepository = clickedLinkRepository;
		this.editionApi = editionApi;
		this.courseApi = courseApi;
	}

	@Test
	@WithUserDetails("teacher")
	public void testGetModuleTeacherVisible() throws Exception {
		mockRole(roleApi, "TEACHER");

		testGetModuleAllowed();
	}

	@Test
	@WithUserDetails("username")
	public void testGetModuleStudentVisible() throws Exception {
		mockRole(roleApi, "STUDENT");

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		testGetModuleAllowed();
	}

	/**
	 * Provides shared functionality for the tests in which the module can be accessed.
	 *
	 * @throws Exception If MVC throws an exception.
	 */
	private void testGetModuleAllowed() throws Exception {
		CourseSummaryDTO course = new CourseSummaryDTO().id(randomId());
		ProgramSummaryDTO program = new ProgramSummaryDTO().id(randomId());
		CourseDetailsDTO courseDetails = new CourseDetailsDTO().id(course.getId()).program(program);
		when(courseApi.getCourseById(any())).thenReturn(Mono.just(courseDetails));
		when(courseApi.getAllCoursesByProgram(any())).thenReturn(Flux.just(course));
		when(editionApi.getEditionById(any()))
				.thenReturn(Mono.just(new EditionDetailsDTO().id(db.getEditionRL().getId()).course(course)));

		String element = mvc.perform(get("/module/{id}", db.getModuleProofTechniques().getId()).with(csrf()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher nameMatcher = Pattern.compile("<title>(.+)</title>").matcher(element);
		assertThat(nameMatcher.find()).isTrue();

		assertThat(nameMatcher.group(1)).isEqualTo(db.getModuleProofTechniques().getName());
	}

	@Test
	@WithAnonymousUser
	public void testGetModuleTeacherNotAuthenticatedNotVisible() throws Exception {
		mvc.perform(get("/module/{id}", db.getModuleProofTechniques().getId()).with(csrf()))
				.andExpect(redirectedUrlPattern("**/auth/login"));
	}

	@Test
	@WithUserDetails("username")
	public void testGetModuleTeacherStudentNotVisible() throws Exception {
		mockRole(roleApi, "STUDENT");

		mvc.perform(get("/module/{id}", db.getModuleProofTechniques().getId()).with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("teacher")
	void deleteModule() throws Exception {
		// Test that deleting a module results in a redirection

		Long moduleId = db.getModuleProofTechniques().getId();
		assertThat(moduleRepository.existsById(moduleId)).isTrue();
		Long editionId = db.getModuleProofTechniques().getEdition().getId();

		mockRole(roleApi, "TEACHER");
		mvc.perform(delete("/module").param("id", moduleId.toString()).with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/edition/" + editionId));

		assertThat(moduleRepository.existsById(moduleId)).isFalse();
		assertThat(taskCompletionRepository.findAll()).hasSize(0);
		assertThat(clickedLinkRepository.findAll()).hasSize(0);
	}
}
