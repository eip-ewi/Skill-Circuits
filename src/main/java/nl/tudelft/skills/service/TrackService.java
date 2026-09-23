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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSkillView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSubmoduleView;
import nl.tudelft.skills.dto.view.circuit.track.TrackLevelTrackView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.EditionRepository;

@Service
@AllArgsConstructor
public class TrackService {

	private final EditionControllerApi editionApi;

	private final EditionRepository editionRepository;
	private final EditionCircuitService editionCircuitService;
	private final SubmoduleDependencyService submoduleDependencyService;

	@Transactional
	public TrackLevelTrackView getTrackCircuit(Long editionId, SCPerson person) {
		EditionDetailsDTO editionDetails = requireNonNull(editionApi.getEditionById(editionId).block());
		SCEdition edition = editionRepository.getOrCreate(editionId);
		Map<Long, EditionLevelSubmoduleView> submoduleViews = editionCircuitService
				.convertToSubmoduleViews(edition, person);
		List<TrackLevelTrackView.Module> modules = new ArrayList<>();
		int column = 0;
		for (SCModule module : sortedModules(edition)) {
			modules.add(convertToModuleView(module, column++, submoduleViews));
		}
		TrackLevelTrackView.Edition group = new TrackLevelTrackView.Edition(editionId,
				editionDetails.getCourse().getName(), modules);
		return new TrackLevelTrackView(editionId, "Track", List.of(group));
	}

	private List<SCModule> sortedModules(SCEdition edition) {
		return edition.getModules().stream()
				.sorted(Comparator.comparing(SCModule::getName, String.CASE_INSENSITIVE_ORDER)
						.thenComparing(SCModule::getId))
				.toList();
	}

	private TrackLevelTrackView.Module convertToModuleView(SCModule module, int column,
			Map<Long, EditionLevelSubmoduleView> submoduleViews) {
		return new TrackLevelTrackView.Module(
				module.getId(),
				module.getName(),
				column,
				getModuleParents(module).stream().map(SCModule::getId).sorted().toList(),
				getModuleChildren(module).stream().map(SCModule::getId).sorted().toList(),
				module.getSubmodules().stream()
						.sorted(Comparator.comparing(Submodule::getName, String.CASE_INSENSITIVE_ORDER)
								.thenComparing(Submodule::getId))
						.map(submodule -> convertToSubmoduleView(submodule,
								submoduleViews.get(submodule.getId())))
						.toList());
	}

	private TrackLevelTrackView.Submodule convertToSubmoduleView(Submodule submodule,
			EditionLevelSubmoduleView submoduleView) {
		List<EditionLevelSkillView> essentialSkills = submoduleView.items().stream()
				.filter(EditionLevelSkillView::essential)
				.toList();
		boolean unlocked = submoduleView.items().stream().anyMatch(skill -> !skill.locked());
		boolean completed = essentialSkills.isEmpty()
				? unlocked
				: essentialSkills.stream().allMatch(skill -> skill.completed() && !skill.locked());
		return new TrackLevelTrackView.Submodule(submodule.getId(), submodule.getName(), completed,
				!unlocked);
	}

	private Set<SCModule> getModuleParents(SCModule module) {
		return module.getSubmodules().stream()
				.flatMap(submodule -> submoduleDependencyService.getSubmoduleParents(submodule).stream())
				.map(Submodule::getModule)
				.filter(parent -> !Objects.equals(module, parent))
				.collect(Collectors.toSet());
	}

	private Set<SCModule> getModuleChildren(SCModule module) {
		return module.getSubmodules().stream()
				.flatMap(submodule -> submoduleDependencyService.getSubmoduleChildren(submodule).stream())
				.map(Submodule::getModule)
				.filter(child -> !Objects.equals(module, child))
				.collect(Collectors.toSet());
	}

}
