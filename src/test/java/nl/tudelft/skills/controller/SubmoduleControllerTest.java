/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.SubmoduleCreateDTO;
import nl.tudelft.skills.dto.id.SCModuleIdDTO;
import nl.tudelft.skills.dto.patch.SubmodulePatchDTO;
import nl.tudelft.skills.dto.patch.SubmodulePositionPatchDTO;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import nl.tudelft.skills.service.EditionService;
import nl.tudelft.skills.service.SkillService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SubmoduleControllerTest extends ControllerTest {

	private final SubmoduleController submoduleController;
	private final SubmoduleRepository submoduleRepository;
	private final ModuleRepository moduleRepository;
	private final EditionControllerApi editionControllerApi;
	private final HttpSession session;

	@Autowired
	public SubmoduleControllerTest(SubmoduleRepository submoduleRepository, SkillService skillService,
			ModuleRepository moduleRepository, EditionControllerApi editionControllerApi,
			EditionService editionService) {
		this.moduleRepository = moduleRepository;
		this.editionControllerApi = editionControllerApi;
		this.session = mock(HttpSession.class);
		this.submoduleController = new SubmoduleController(submoduleRepository, moduleRepository,
				skillService, editionService, session);
		this.submoduleRepository = submoduleRepository;
	}

	@Test
	@WithUserDetails("admin")
	void createSubmodule() {
		var edition = mock(EditionDetailsDTO.class);
		var course = mock(CourseSummaryDTO.class);
		Mockito.when(course.getId()).thenReturn(1L);
		Mockito.when(edition.getName()).thenReturn("Mocked Edition");
		Mockito.when(edition.getCourse()).thenReturn(course);
		Mockito.when(edition.getStartDate()).thenReturn(
				LocalDateTime.of(2023, 1, 10, 10, 10, 0));
		Mockito.when(editionControllerApi.getEditionById(any())).thenReturn(Mono.just(edition));

		when(editionControllerApi.getAllEditionsByCourse(anyLong()))
				.thenReturn(Flux.fromIterable(List.of(edition)));

		var dto = SubmoduleCreateDTO.builder()
				.name("New Submodule")
				.module(new SCModuleIdDTO(db.getModuleProofTechniques().getId()))
				.row(1).column(1).build();

		submoduleController.createSubmodule(dto, mock(Model.class));

		assertTrue(submoduleRepository.findAll().stream().anyMatch(s -> s.getName().equals("New Submodule")));
	}

	@Test
	void patchSubmodule() {
		submoduleController.patchSubmodule(SubmodulePatchDTO.builder()
				.id(db.getSubmoduleCases().getId())
				.name("Updated")
				.module(new SCModuleIdDTO(db.getModuleProofTechniques().getId()))
				.build(), model);

		Submodule submodule = submoduleRepository.findByIdOrThrow(db.getSubmoduleCases().getId());
		assertThat(submodule.getName()).isEqualTo("Updated");
		assertThat(moduleRepository.findByIdOrThrow(submodule.getModule().getId())).isEqualTo(
				moduleRepository.findByIdOrThrow(db.getModuleProofTechniques().getId()));
	}

	@Test
	void updateSubmodulePosition() {
		submoduleController.updateSubmodulePosition(db.getSubmoduleCases().getId(),
				SubmodulePositionPatchDTO.builder()
						.column(10)
						.row(11)
						.build());

		Submodule submodule = submoduleRepository.findByIdOrThrow(db.getSubmoduleCases().getId());
		assertThat(submodule.getColumn()).isEqualTo(10);
		assertThat(submodule.getRow()).isEqualTo(11);
	}

	@Test
	void deleteSkill() {
		Long submoduleCasesId = db.getSubmoduleCases().getId();

		submoduleController.deleteSubmodule(submoduleCasesId, "block");
		assertThat(submoduleRepository.existsById(submoduleCasesId)).isFalse();
	}

	@Test
	void endpointsAreProtected() throws Exception {
		mvc.perform(patch("/submodule/{id}", db.getSubmoduleCases().getId()))
				.andExpect(status().isForbidden());
		mvc.perform(post("/submodule"))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/submodule?id=1"))
				.andExpect(status().isForbidden());
	}

}
