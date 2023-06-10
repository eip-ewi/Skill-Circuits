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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionSummaryDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class EditionServiceTest {

	private final EditionControllerApi editionApi;

	private final EditionService editionService;

	private final EditionRepository editionRepository;

	@Autowired
	public EditionServiceTest(EditionControllerApi editionApi, EditionRepository editionRepository,
			CircuitService circuitService, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, ModuleRepository moduleRepository,
			SubmoduleRepository submoduleRepository, AbstractSkillRepository abstractSkillRepository,
			AchievementRepository achievementRepository, SkillRepository skillRepository,
			TaskRepository taskRepository) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		editionService = new EditionService(editionApi, editionRepository, circuitService,
				checkpointRepository, pathRepository, moduleRepository, submoduleRepository,
				abstractSkillRepository, achievementRepository, skillRepository, taskRepository);
	}

	@Test
	public void getCourseView() {
		EditionDetailsDTO editionDetailsDTO = new EditionDetailsDTO().id(1L).name("edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(LocalDateTime.of(2023, 1, 10, 10, 10, 0));

		when(editionApi.getEditionById(anyLong())).thenReturn(Mono.just(editionDetailsDTO));
		when(editionApi.getAllEditionsByCourse(anyLong()))
				.thenReturn(Flux.fromIterable(List.of(editionDetailsDTO)));

		EditionLevelEditionViewDTO editionView = editionService.getEditionView(1L);
		assertThat(editionView.getName()).isEqualTo("edition");

		// Should contain no edition in the previous editions of the course view
		assertThat(editionView.getCourse())
				.isEqualTo(new EditionLevelCourseViewDTO(2L, "course", List.of()));
	}

	@Test
	public void getCourseViewPreviousEditionExists() {
		// Return and mock three editions, one older and one newer one than the one for which
		// the method is called
		EditionDetailsDTO checkEditionDetails = new EditionDetailsDTO().id(1L).name("edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(LocalDateTime.of(2023, 1, 10, 10, 10, 0));
		EditionDetailsDTO olderEdition = new EditionDetailsDTO().id(2L).name("older edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(LocalDateTime.of(2022, 1, 10, 10, 10, 0));
		EditionDetailsDTO newerEdition = new EditionDetailsDTO().id(3L).name("newer edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(LocalDateTime.of(2023, 2, 10, 10, 10, 0));

		when(editionApi.getEditionById(anyLong())).thenReturn(Mono.just(checkEditionDetails));
		when(editionApi.getAllEditionsByCourse(anyLong()))
				.thenReturn(Flux.fromIterable(List.of(checkEditionDetails, olderEdition, newerEdition)));

		EditionLevelEditionViewDTO editionView = editionService.getEditionView(1L);
		assertThat(editionView.getName()).isEqualTo("edition");

		// Should only contain the older edition in the previous editions of the course view
		EditionLevelEditionSummaryDTO olderEditionSummary = new EditionLevelEditionSummaryDTO(2L,
				"older edition");
		assertThat(editionView.getCourse())
				.isEqualTo(new EditionLevelCourseViewDTO(2L, "course", List.of(olderEditionSummary)));
	}

	@Test
	public void getOrCreateSCEditionCreatesNew() {
		Long editionId = 1L;
		assertThat(editionRepository.findById(editionId)).isNotPresent();

		SCEdition edition = editionService.getOrCreateSCEdition(editionId);

		assertThat(editionRepository.findById(editionId)).isPresent().contains(edition);
	}

	@Test
	public void getDefaultPath() {
		Long editionId = 1L;
		editionService.getOrCreateSCEdition(editionId);

		assertNull(editionService.getDefaultPath(editionId));
	}

	@Test
	public void getPaths() {
		Long editionId = 1L;
		editionService.getOrCreateSCEdition(editionId);

		assertTrue(editionService.getPaths(editionId).isEmpty());
	}
}
