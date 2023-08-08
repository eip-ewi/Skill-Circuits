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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.servlet.http.HttpSession;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.Id;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.SCModuleSummaryDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.EditionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class EditionControllerTest extends ControllerTest {

	private final EditionController editionController;
	private final EditionService editionService;
	private final EditionRepository editionRepository;
	private final HttpSession session;
	private final RoleControllerApi roleApi;

	@Autowired
	public EditionControllerTest(EditionRepository editionRepository,
			AuthorisationService authorisationService, RoleControllerApi roleApi) {
		this.editionRepository = editionRepository;
		this.editionService = mock(EditionService.class);
		this.session = mock(HttpSession.class);
		this.editionController = new EditionController(editionRepository, editionService,
				authorisationService, session);
		this.roleApi = roleApi;
	}

	@Test
	@WithUserDetails("admin")
	void publishEdition() {
		Long editionId = db.getEditionRL().getId();

		assertThat(editionRepository.findByIdOrThrow(editionId).isVisible()).isFalse();

		editionController.publishEdition(editionId);

		assertThat(editionRepository.findByIdOrThrow(editionId).isVisible()).isTrue();

	}

	@Test
	@WithUserDetails("admin")
	void unpublishEdition() {
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		Long editionId = db.getEditionRL().getId();

		assertThat(editionRepository.findByIdOrThrow(editionId).isVisible()).isTrue();

		editionController.unpublishEdition(editionId);

		assertThat(editionRepository.findByIdOrThrow(editionId).isVisible()).isFalse();
	}

	@Test
	void toggleStudentMode() {
		when(session.getAttribute("student-mode-" + db.getEditionRL().getId()))
				.thenReturn(null)
				.thenReturn(true)
				.thenReturn(false);
		editionController.toggleStudentMode(db.getEditionRL().getId());
		verify(session).setAttribute("student-mode-" + db.getEditionRL().getId(), true);
		editionController.toggleStudentMode(db.getEditionRL().getId());
		verify(session).setAttribute("student-mode-" + db.getEditionRL().getId(), false);
		editionController.toggleStudentMode(db.getEditionRL().getId());
		verify(session, times(2)).setAttribute("student-mode-" + db.getEditionRL().getId(), true);
	}

	@Test
	void getModulesOfEdition() {
		assertThat(editionController.getModulesOfEdition(db.getEditionRL().getId()))
				.isEqualTo(List.of(mapper.map(db.getModuleProofTechniques(), SCModuleSummaryDTO.class)));
	}

	@Test
	@WithUserDetails("teacher")
	public void testCopyEditionAllowed() throws Exception {
		SCEdition edition1 = editionRepository.save(SCEdition.builder().id(1L).build());
		SCModule module = SCModule.builder().edition(edition1).name("Module").build();
		edition1.getModules().add(module);
		SCEdition edition2 = editionRepository.save(SCEdition.builder().id(2L).build());
		// Mock the user to be a teacher in both editions
		when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.fromIterable(
				List.of(getRoleDetails("TEACHER", 1L), getRoleDetails("TEACHER", 2L))));
		// This will use a non-mocked version of the editionService
		mvc.perform(post("/edition/2/copy/1").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/edition/2"));

		// Assert on simple copy behavior
		assertThat(edition2.getModules()).hasSize(1);
		assertThat(edition2.getModules().get(0).getName()).isEqualTo("Module");
		assertThat(edition2.getModules().get(0).getEdition()).isEqualTo(edition2);

		// Check behavior with mocked editionService
		editionController.copyEdition(2L, 1L);
		verify(editionService).copyEdition(2L, 1L);
	}

	@Test
	@WithUserDetails("username")
	public void testCopyEditionAllowedVisible() throws Exception {
		SCEdition edition1 = editionRepository.save(SCEdition.builder().id(1L).build());
		SCModule module = SCModule.builder().edition(edition1).name("Module").build();
		edition1.getModules().add(module);
		edition1.setVisible(true);
		SCEdition edition2 = editionRepository.save(SCEdition.builder().id(2L).build());
		// Mock the user to be a teacher one edition, but a student in the other, the edition being visible
		when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.fromIterable(
				List.of(getRoleDetails("TEACHER", 2L), getRoleDetails("STUDENT", 1L))));
		// This will use a non-mocked version of the editionService
		mvc.perform(post("/edition/2/copy/1").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/edition/2"));

		// Assert on simple copy behavior
		assertThat(edition2.getModules()).hasSize(1);
		assertThat(edition2.getModules().get(0).getName()).isEqualTo("Module");
		assertThat(edition2.getModules().get(0).getEdition()).isEqualTo(edition2);
	}

	@Test
	@WithUserDetails("teacher")
	public void testCopyEditionEditionDoesNotExist() throws Exception {
		editionRepository.save(SCEdition.builder().id(1L).build());
		// Mock the user to be a teacher in edition 1, edition with id 2 is not saved
		when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.fromIterable(
				List.of(getRoleDetails("TEACHER", 1L))));
		mvc.perform(post("/edition/1/copy/2").with(csrf()))
				.andExpect(status().is4xxClientError());
	}

	@Test
	@WithUserDetails("username")
	public void testCopyEditionForbidden() throws Exception {
		editionRepository.save(SCEdition.builder().id(1L).build());
		editionRepository.save(SCEdition.builder().id(2L).build());

		// Mock the user to be a student in one edition, and student in the other (the edition being invisible)
		when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.fromIterable(
				List.of(getRoleDetails("TEACHER", 1L), getRoleDetails("STUDENT", 2L))));
		mvc.perform(post("/edition/1/copy/2").with(csrf()))
				.andExpect(status().isForbidden());
		when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.fromIterable(
				List.of(getRoleDetails("STUDENT", 1L), getRoleDetails("TEACHER", 2L))));
		mvc.perform(post("/edition/1/copy/2").with(csrf()))
				.andExpect(status().isForbidden());

		// Mock the user to be a student both editions
		when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.fromIterable(
				List.of(getRoleDetails("STUDENT", 1L), getRoleDetails("STUDENT", 2L))));
		mvc.perform(post("/edition/1/copy/2").with(csrf()))
				.andExpect(status().isForbidden());
	}

	/**
	 * Creates a RoleDetailsDTO for a specific edition and role.
	 *
	 * @param  role      The role of the user.
	 * @param  editionId The edition id.
	 * @return           The RoleDetailsDTO with the specified attributes.
	 */
	private RoleDetailsDTO getRoleDetails(String role, long editionId) {
		return new RoleDetailsDTO()
				.id(new Id().editionId(editionId).personId(db.getPerson().getId()))
				.person(new PersonSummaryDTO().id(db.getPerson().getId()).username("username"))
				.type(RoleDetailsDTO.TypeEnum.valueOf(role));
	}
}
