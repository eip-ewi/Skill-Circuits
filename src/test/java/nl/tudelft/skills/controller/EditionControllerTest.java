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

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.repository.EditionRepository;

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

	@Autowired
	public EditionControllerTest(EditionRepository editionRepository) {
		this.editionController = new EditionController(editionRepository);
	}

	@Test
	void getEditionPage() {
		String page = editionController.getEditionPage(db.getEditionRL().getId(), model);
		assertThat(page).isEqualTo("edition/view");
		assertThat(model.getAttribute("edition"))
				.isEqualTo(View.convert(db.getEditionRL(), EditionLevelEditionViewDTO.class));
	}

}
