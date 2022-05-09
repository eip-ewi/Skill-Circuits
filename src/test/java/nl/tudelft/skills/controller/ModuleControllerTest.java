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

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.id.SCEditionIdDTO;
import nl.tudelft.skills.dto.patch.SCModulePatchDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.service.ModuleService;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class ModuleControllerTest extends ControllerTest {

	@MockBean
	private ModuleService moduleService;
	private final ModuleController moduleController;
	private final RoleControllerApi roleControllerApi;
	private ModuleRepository moduleRepository;

	@Autowired
	public ModuleControllerTest(ModuleController moduleController, RoleControllerApi roleControllerApi,
			ModuleRepository moduleRepository) {
		this.moduleController = moduleController;
		this.roleControllerApi = roleControllerApi;
		this.moduleRepository = moduleRepository;
	}

	@Test
	void getModulePage() {
		String page = moduleController.getModulePage(null, db.getModuleProofTechniques().getId(), model);
		assertThat(page).isEqualTo("module/view");
		assertThat(model.getAttribute("module"))
				.isEqualTo(View.convert(db.getModuleProofTechniques(), ModuleLevelModuleViewDTO.class));
	}

	@Test
	void getModulePageCallsTasksCompleted() {
		moduleController.getModulePage(TestUserDetailsService.assemblePerson("username"),
				db.getModuleProofTechniques().getId(), model);

		verify(moduleService).setCompletedTasksForPerson(
				View.convert(db.getModuleProofTechniques(), ModuleLevelModuleViewDTO.class),
				TestUserDetailsService.id);
	}

	@Test
	@WithUserDetails("admin")
	void createModule() throws Exception {
		String element = mvc.perform(post("/module").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("name", "Module"),
						new BasicNameValuePair("edition.id", Long.toString(db.edition.getId()))))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher idMatcher = Pattern.compile("id=\"module-(\\d+)\"").matcher(element);
		assertThat(idMatcher.find()).isTrue();

		Long id = Long.parseLong(idMatcher.group(1));
		assertThat(moduleRepository.existsById(id)).isTrue();

		assertThat(element)
				.contains("<h2 id=\"module-" + id + "-name\" class=\"module-title\">Module</h2>");
	}

	@Test
	void deleteModule() {
		Long moduleId = db.getModuleProofTechniques().getId();

		assertThat(moduleRepository.existsById(moduleId)).isTrue();

		new ModuleController(moduleRepository, moduleService).deleteModule(moduleId);

		assertThat(moduleRepository.existsById(moduleId)).isFalse();
	}

	@Test
	void patchModule() {
		new ModuleController(moduleRepository, moduleService).patchModule(SCModulePatchDTO.builder()
				.id(db.getModuleProofTechniques().getId())
				.name("Module 2.0")
				.edition(new SCEditionIdDTO(db.getModuleProofTechniques().getEdition().getId()))
				.build());

		SCModule module = moduleRepository.findByIdOrThrow(db.getModuleProofTechniques().getId());

		assertThat(module.getName()).isEqualTo("Module 2.0");
	}
}
