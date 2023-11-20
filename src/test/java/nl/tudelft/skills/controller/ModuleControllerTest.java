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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.SCModuleCreateDTO;
import nl.tudelft.skills.dto.id.SCEditionIdDTO;
import nl.tudelft.skills.dto.patch.SCModulePatchDTO;
import nl.tudelft.skills.dto.view.SkillSummaryDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ClickedLinkRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.service.ClickedLinkService;
import nl.tudelft.skills.service.ModuleService;
import nl.tudelft.skills.service.TaskCompletionService;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class ModuleControllerTest extends ControllerTest {

	@MockBean
	private ModuleService moduleService;
	private final TaskCompletionService taskCompletionService;
	private final TaskCompletionRepository taskCompletionRepository;
	private final ModuleController moduleController;
	private final RoleControllerApi roleControllerApi;
	private final ModuleRepository moduleRepository;
	private final HttpSession session;
	private final ClickedLinkService clickedLinkService;
	private final ClickedLinkRepository clickedLinkRepository;

	@Autowired
	public ModuleControllerTest(ModuleController moduleController, RoleControllerApi roleControllerApi,
			ModuleRepository moduleRepository, TaskCompletionService taskCompletionService,
			TaskCompletionRepository taskCompletionRepository, ClickedLinkService clickedLinkService,
			ClickedLinkRepository clickedLinkRepository) {
		this.moduleController = moduleController;
		this.roleControllerApi = roleControllerApi;
		this.moduleRepository = moduleRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.taskCompletionService = taskCompletionService;
		this.clickedLinkService = clickedLinkService;
		this.clickedLinkRepository = clickedLinkRepository;
		this.session = mock(HttpSession.class);
	}

	@Test
	@WithUserDetails("admin")
	void createModule() throws Exception {
		String element = mvc.perform(post("/module").with(csrf())
				.content(getModuleCreateFormData())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher idMatcher = Pattern.compile("id=\"module-(\\d+)\"").matcher(element);
		assertThat(idMatcher.find()).isTrue();

		Long id = Long.parseLong(idMatcher.group(1));
		assertThat(moduleRepository.existsById(id)).isTrue();

		assertThat(element.replaceAll("\n\\s+", "\n"))
				.contains("<h2\nid=\"module-" + id + "-name\"\nclass=\"module-title\">Module</h2>");
	}

	@Test
	void createModuleSetup() throws Exception {
		new ModuleController(moduleRepository, moduleService,
				session, taskCompletionService, clickedLinkService).createModuleInEditionSetup(
						SCModuleCreateDTO.builder()
								.name("Module").edition(new SCEditionIdDTO(db.getEditionRL().getId()))
								.build(),
						Mockito.mock(Model.class));

		assertThat(moduleRepository.findAll().stream()
				.filter(m -> m.getName().equals("Module")).findFirst()).isNotEmpty();
	}

	private String getModuleCreateFormData() throws Exception {
		return EntityUtils.toString(new UrlEncodedFormEntity(List.of(
				new BasicNameValuePair("name", "Module"),
				new BasicNameValuePair("edition.id", Long.toString(db.getEditionRL().getId())))));
	}

	@Test
	void deleteModule() {
		Long moduleId = db.getModuleProofTechniques().getId();

		assertThat(moduleRepository.existsById(moduleId)).isTrue();

		new ModuleController(moduleRepository, moduleService,
				session, taskCompletionService, clickedLinkService).deleteModule(moduleId);

		assertThat(moduleRepository.existsById(moduleId)).isFalse();
		assertThat(taskCompletionRepository.findAll()).hasSize(0);
		assertThat(clickedLinkRepository.findAll()).hasSize(0);
	}

	@Test
	void deleteModuleSetup() {
		Long moduleId = db.getModuleProofTechniques().getId();

		assertThat(moduleRepository.existsById(moduleId)).isTrue();

		new ModuleController(moduleRepository, moduleService,
				session, taskCompletionService, clickedLinkService).deleteModuleSetup(moduleId);

		assertThat(moduleRepository.existsById(moduleId)).isFalse();
		assertThat(taskCompletionRepository.findAll()).hasSize(0);
		assertThat(clickedLinkRepository.findAll()).hasSize(0);
	}

	@Test
	void patchModule() {
		new ModuleController(moduleRepository, moduleService,
				session, taskCompletionService, clickedLinkService).patchModule(
						SCModulePatchDTO.builder()
								.id(db.getModuleProofTechniques().getId())
								.name("Module 2.0")
								.edition(new SCEditionIdDTO(
										db.getModuleProofTechniques().getEdition().getId()))
								.build());

		SCModule module = moduleRepository.findByIdOrThrow(db.getModuleProofTechniques().getId());

		assertThat(module.getName()).isEqualTo("Module 2.0");
	}

	@Test
	void toggleStudentMode() {
		ModuleController moduleController = new ModuleController(moduleRepository, moduleService,
				session, taskCompletionService, clickedLinkService);
		when(session.getAttribute("student-mode-" + db.getEditionRL().getId()))
				.thenReturn(null)
				.thenReturn(true)
				.thenReturn(false);
		moduleController.toggleStudentMode(db.getModuleProofTechniques().getId());
		verify(session).setAttribute("student-mode-" + db.getEditionRL().getId(), true);
		moduleController.toggleStudentMode(db.getModuleProofTechniques().getId());
		verify(session).setAttribute("student-mode-" + db.getEditionRL().getId(), false);
		moduleController.toggleStudentMode(db.getModuleProofTechniques().getId());
		verify(session, times(2)).setAttribute("student-mode-" + db.getEditionRL().getId(), true);
	}

	@Test
	void getSkillsOfModule() {
		ModuleController moduleController = new ModuleController(moduleRepository, moduleService,
				session, taskCompletionService, clickedLinkService);
		assertThat(moduleController.getSkillsOfModule(db.getModuleProofTechniques().getId()))
				.containsExactlyInAnyOrder(Stream
						.of(
								db.getSkillVariables(),
								db.getSkillNegation(),
								db.getSkillImplication(),
								db.getSkillNegateImplications(),
								db.getSkillContrapositivePractice(),
								db.getSkillDividingIntoCases(), db.getSkillCasesPractice(),
								db.getSkillContradictionPractice(),
								db.getSkillAssumption(),
								db.getSkillGeneralisationPractice(), db.getSkillProofOutline(),
								db.getSkillTransitiveProperty(), db.getSkillInductionPractice())
						.map(s -> mapper.map(s, SkillSummaryDTO.class)).toList()
						.toArray(new SkillSummaryDTO[0]));
	}
}
