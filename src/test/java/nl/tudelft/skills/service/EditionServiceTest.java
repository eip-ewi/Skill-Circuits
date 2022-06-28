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
package nl.tudelft.skills.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.EditionRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class EditionServiceTest {

	private EditionControllerApi editionApi;

	private EditionService editionService;

	private EditionRepository editionRepository;

	@Autowired
	public EditionServiceTest(EditionControllerApi editionApi, EditionRepository editionRepository,
			CircuitService circuitService) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		editionService = new EditionService(editionApi, editionRepository, circuitService);
	}

	@Test
	public void getCourseView() {

		EditionDetailsDTO editionDetailsDTO = new EditionDetailsDTO().id(1L).name("edition")
				.course(new CourseSummaryDTO().id(2L).name("course"));

		when(editionApi.getEditionById(anyLong())).thenReturn(Mono.just(editionDetailsDTO));

		assertThat(editionService.getEditionView(1L).getName()).isEqualTo("edition");
		assertThat(editionService.getEditionView(1L).getCourse())
				.isEqualTo(new EditionLevelCourseViewDTO(2L, "course"));
	}

	@Test
	public void getOrCreateSCEditionCreatesNew() {
		Long editionId = 1L;
		assertThat(editionRepository.findById(editionId)).isNotPresent();

		SCEdition edition = editionService.getOrCreateSCEdition(editionId);

		assertThat(editionRepository.findById(editionId)).isPresent().contains(edition);
	}
}
