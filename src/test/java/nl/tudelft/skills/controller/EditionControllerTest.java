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

import javax.servlet.http.HttpSession;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.EditionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class EditionControllerTest extends ControllerTest {

	private final EditionController editionController;
	private final EditionService editionService;
	private final EditionRepository editionRepository;
	private final HttpSession session;

	@Autowired
	public EditionControllerTest(EditionRepository editionRepository,
			AuthorisationService authorisationService) {
		this.editionRepository = editionRepository;
		this.editionService = mock(EditionService.class);
		this.session = mock(HttpSession.class);
		this.editionController = new EditionController(editionRepository, editionService,
				authorisationService, session);
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

}
