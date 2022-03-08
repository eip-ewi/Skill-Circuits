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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.service.ModuleService;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;

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
	@WithUserDetails("username")
	void getModulePageCallsTasksCompleted() throws Exception {
		when(roleControllerApi.getRolesById(any(), any())).thenReturn(Flux.empty());

		mvc.perform(get("/module/{id}", db.getModuleProofTechniques().getId()));
		verify(moduleService).setCompletedTasksForPerson(
				View.convert(db.getModuleProofTechniques(), ModuleLevelModuleViewDTO.class),
				TestUserDetailsService.id);
	}

	@Test
	void deleteModule() {
		Long moduleId = db.getModuleProofTechniques().getId();

		assertThat(moduleRepository.existsById(moduleId)).isTrue();

		new ModuleController(moduleRepository, moduleService).deleteModule(moduleId);

		assertThat(moduleRepository.existsById(moduleId)).isFalse();
	}

}
