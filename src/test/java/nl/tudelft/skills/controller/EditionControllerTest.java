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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.service.EditionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class EditionControllerTest extends ControllerTest {

	private final EditionController editionController;
	private final EditionService editionService;

	@Autowired
	public EditionControllerTest(EditionRepository editionRepository) {
		this.editionService = mock(EditionService.class);
		this.editionController = new EditionController(editionRepository, editionService);
	}

	@Test
	void getEditionPage() {
		EditionLevelEditionViewDTO mockEditionView = View.convert(db.getEditionRL(),
				EditionLevelEditionViewDTO.class);
		mockEditionView.setCourse(new EditionLevelCourseViewDTO(10L, "RL"));
		mockEditionView.setName("RL");

		when(editionService.getEditionView(anyLong())).thenReturn(mockEditionView);

		Long editionId = db.getEditionRL().getId();

		editionController.getEditionPage(editionId, model);

		assertThat(model.getAttribute("edition")).isEqualTo(mockEditionView);

		verify(editionService).getEditionView(editionId);
	}

}
