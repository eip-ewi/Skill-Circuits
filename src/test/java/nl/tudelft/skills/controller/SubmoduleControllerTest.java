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
import nl.tudelft.skills.dto.id.SCModuleIdDTO;
import nl.tudelft.skills.dto.patch.SubmodulePatchDTO;
import nl.tudelft.skills.dto.patch.SubmodulePositionPatchDTO;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import nl.tudelft.skills.service.SkillService;

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
public class SubmoduleControllerTest extends ControllerTest {

	private final SubmoduleController submoduleController;
	private final SubmoduleRepository submoduleRepository;
	private final ModuleRepository moduleRepository;

	@Autowired
	public SubmoduleControllerTest(SubmoduleRepository submoduleRepository, SkillService skillService,
			ModuleRepository moduleRepository) {
		this.moduleRepository = moduleRepository;
		this.submoduleController = new SubmoduleController(submoduleRepository, skillService);
		this.submoduleRepository = submoduleRepository;
	}

	@Test
	@WithUserDetails("admin")
	void createSubmodule() throws Exception {
		String element = mvc.perform(post("/submodule").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("name", "Submodule"),
						new BasicNameValuePair("module.id", Long.toString(db.moduleProofTechniques.getId())),
						new BasicNameValuePair("row", "10"),
						new BasicNameValuePair("column", "11")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher idMatcher = Pattern.compile("id=\"block-(\\d+)\"").matcher(element);
		assertThat(idMatcher.find()).isTrue();

		Long id = Long.parseLong(idMatcher.group(1));
		assertThat(submoduleRepository.existsById(id)).isTrue();

		assertThat(element)
				.contains("<span id=\"block-" + id + "-name\">Submodule</span>")
				.contains("style=\"grid-row: 11; grid-column: 12");
	}

	@Test
	void patchSubmodule() {
		submoduleController.patchSubmodule(SubmodulePatchDTO.builder()
				.id(db.submoduleCases.getId())
				.name("Updated")
				.module(new SCModuleIdDTO(db.moduleProofTechniques.getId()))
				.build());

		Submodule submodule = submoduleRepository.findByIdOrThrow(db.submoduleCases.getId());
		assertThat(submodule.getName()).isEqualTo("Updated");
		assertThat(moduleRepository.findByIdOrThrow(submodule.getModule().getId())).isEqualTo(
				moduleRepository.findByIdOrThrow(db.moduleProofTechniques.getId()));
	}

	@Test
	void updateSubmodulePosition() {
		submoduleController.updateSubmodulePosition(db.submoduleCases.getId(),
				SubmodulePositionPatchDTO.builder()
						.column(10)
						.row(11)
						.build());

		Submodule submodule = submoduleRepository.findByIdOrThrow(db.submoduleCases.getId());
		assertThat(submodule.getColumn()).isEqualTo(10);
		assertThat(submodule.getRow()).isEqualTo(11);
	}

	@Test
	void deleteSkill() {
		submoduleController.deleteSubmodule(db.submoduleCases.getId(), "block");
		assertThat(submoduleRepository.existsById(db.submoduleCases.getId())).isFalse();
	}

	@Test
	void endpointsAreProtected() throws Exception {
		mvc.perform(patch("/submodule/{id}", db.submoduleCases.getId()))
				.andExpect(status().isForbidden());
		mvc.perform(post("/submodule"))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/submodule?id=1"))
				.andExpect(status().isForbidden());
	}

}
