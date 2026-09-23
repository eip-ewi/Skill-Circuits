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
package nl.tudelft.skills.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSkillView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSubmoduleView;
import nl.tudelft.skills.dto.view.circuit.track.TrackLevelTrackView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.EditionRepository;
import reactor.core.publisher.Mono;

public class TrackServiceTest {

	private TrackService trackService;

	private EditionControllerApi editionApi;
	private EditionRepository editionRepository;
	private EditionCircuitService editionCircuitService;
	private SubmoduleDependencyService submoduleDependencyService;
	private SCPerson person;

	@BeforeEach
	public void setUp() {
		editionApi = mock(EditionControllerApi.class);
		editionRepository = mock(EditionRepository.class);
		editionCircuitService = mock(EditionCircuitService.class);
		submoduleDependencyService = mock(SubmoduleDependencyService.class);
		person = mock(SCPerson.class);

		trackService = new TrackService(editionApi, editionRepository, editionCircuitService,
				submoduleDependencyService);
	}

	@Test
	public void getTrackCircuitSuccess() {
		SCEdition edition = edition(1L);
		SCModule moduleTwo = module(edition, 12L, "Mat notation", 22L, "Second");
		SCModule moduleOne = module(edition, 11L, "Prop logic", 21L, "First");
		SCModule moduleThree = module(edition, 10L, "Foundation", 20L, "Basics");
		Submodule submoduleThree = moduleThree.getSubmodules().iterator().next();

		when(editionApi.getEditionById(1L)).thenReturn(Mono.just(details(1L)));
		when(editionRepository.getOrCreate(1L)).thenReturn(edition);
		when(editionCircuitService.convertToSubmoduleViews(edition, person)).thenReturn(Map.of(
				20L, submoduleView(20L, "Basics", skillView(30L, false, false)),
				21L, submoduleView(21L, "First", skillView(31L, false, true)),
				22L, submoduleView(22L, "Second", skillView(32L, true, false))));
		when(submoduleDependencyService.getSubmoduleParents(any())).thenReturn(Set.of());
		when(submoduleDependencyService.getSubmoduleChildren(any())).thenReturn(Set.of());
		when(submoduleDependencyService.getSubmoduleChildren(submoduleThree))
				.thenReturn(Set.of(moduleTwo.getSubmodules().iterator().next(),
						moduleOne.getSubmodules().iterator().next()));

		TrackLevelTrackView track = trackService.getTrackCircuit(1L, person);
		List<TrackLevelTrackView.Module> modules = track.groups().stream()
				.flatMap(group -> group.blocks().stream()).toList();

		assertEquals("Track", track.name());
		assertEquals(List.of(1L), track.groups().stream().map(TrackLevelTrackView.Edition::id).toList());
		assertEquals(List.of("Course"),
				track.groups().stream().map(TrackLevelTrackView.Edition::name).toList());
		assertEquals(List.of("Foundation", "Mat notation", "Prop logic"),
				modules.stream().map(TrackLevelTrackView.Module::name).toList());
		assertEquals(List.of(0, 1, 2), modules.stream().map(TrackLevelTrackView.Module::column).toList());
		assertEquals(List.of(11L, 12L), modules.getFirst().children());
		assertFalse(modules.getFirst().items().getFirst().completed());
		assertFalse(modules.getFirst().items().getFirst().locked());
		assertTrue(modules.get(1).items().getFirst().completed());
		assertFalse(modules.get(1).items().getFirst().locked());
		assertFalse(modules.getLast().items().getFirst().completed());
		assertTrue(modules.getLast().items().getFirst().locked());
	}

	@Test
	public void getTrackCircuitWithoutModules() {
		when(editionApi.getEditionById(1L)).thenReturn(Mono.just(details(1L)));
		SCEdition edition = edition(1L);
		when(editionRepository.getOrCreate(1L)).thenReturn(edition);
		when(editionCircuitService.convertToSubmoduleViews(edition, person)).thenReturn(Map.of());

		TrackLevelTrackView track = trackService.getTrackCircuit(1L, person);

		assertEquals("Track", track.name());
		assertEquals(1, track.groups().size());
		assertEquals(List.of(), track.groups().getFirst().blocks());
	}

	private static SCEdition edition(long editionId) {
		return SCEdition.builder().id(editionId).build();
	}

	private static SCModule module(SCEdition edition, long moduleId, String moduleName,
			long submoduleId, String submoduleName) {
		SCModule module = SCModule.builder()
				.id(moduleId)
				.name(moduleName)
				.edition(edition)
				.build();
		Submodule submodule = Submodule.builder()
				.id(submoduleId)
				.name(submoduleName)
				.module(module)
				.build();
		edition.getModules().add(module);
		module.getSubmodules().add(submodule);
		return module;
	}

	private static EditionDetailsDTO details(long id) {
		return new EditionDetailsDTO().id(id).name("Edition " + id)
				.course(new CourseSummaryDTO().id(10L).name("Course").code("CSE"));
	}

	private static EditionLevelSubmoduleView submoduleView(long id, String name,
			EditionLevelSkillView skill) {
		return new EditionLevelSubmoduleView(id, name, 0, List.of(), List.of(), List.of(skill));
	}

	private static EditionLevelSkillView skillView(long id, boolean completed, boolean locked) {
		return new EditionLevelSkillView(id, "Skill " + id, 0, true, false, completed, locked);
	}

}
