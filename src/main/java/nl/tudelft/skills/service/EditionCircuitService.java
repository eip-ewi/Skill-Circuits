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

import java.util.*;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.dto.view.CheckpointView;
import nl.tudelft.skills.dto.view.PathView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelEditionView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelModuleView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSkillView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSubmoduleView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;

@Service
@AllArgsConstructor
public class EditionCircuitService {

	private final EditionControllerApi editionApi;

	private final EditionRepository editionRepository;
	private final SkillStateService skillStateService;
	private final SubmoduleDependencyService submoduleDependencyService;

	private final TaskCompletionRepository taskCompletionRepository;

	public EditionLevelEditionView getEditionCircuit(Long editionId, SCPerson person) {
		SCEdition edition = editionRepository.getOrCreate(editionId);
		EditionDetailsDTO editionDetails = requireNonNull(editionApi.getEditionById(editionId).block());
		Set<Long> completedTaskIds = taskCompletionRepository.findAllCompletedTaskIdsForPerson(person);

		return new EditionLevelEditionView(
				edition.getId(),
				editionDetails.getCourse().getName() + " - " + editionDetails.getName(),
				edition.getModules().stream().map(module -> convertToModuleView(module, completedTaskIds))
						.toList(),
				edition.getCheckpoints().stream()
						.map(checkpoint -> new CheckpointView(checkpoint.getId(), checkpoint.getName(),
								checkpoint.getDeadline()))
						.toList(),
				edition.getPaths().stream()
						.map(path -> new PathView(path.getId(), path.getName(), path.getDescription()))
						.toList());
	}

	public EditionLevelModuleView convertToModuleView(SCModule module, SCPerson person) {
		Set<Long> completedTaskIds = taskCompletionRepository.findAllCompletedTaskIdsForPerson(person);
		return convertToModuleView(module, completedTaskIds);
	}

	private EditionLevelModuleView convertToModuleView(SCModule module, Set<Long> completedTaskIds) {
		return new EditionLevelModuleView(
				module.getId(),
				module.getName(),
				module.getSubmodules().stream()
						.map(submodule -> convertToSubmoduleView(submodule, completedTaskIds)).toList());
	}

	public EditionLevelSubmoduleView convertToSubmoduleView(Submodule submodule, SCPerson person) {
		Set<Long> completedTaskIds = taskCompletionRepository.findAllCompletedTaskIdsForPerson(person);
		return convertToSubmoduleView(submodule, completedTaskIds);
	}

	private EditionLevelSubmoduleView convertToSubmoduleView(Submodule submodule,
			Set<Long> completedTaskIds) {
		return new EditionLevelSubmoduleView(
				submodule.getId(),
				submodule.getName(),
				submodule.getColumn(),
				submoduleDependencyService.getSubmoduleParents(submodule).stream().map(Submodule::getId)
						.toList(),
				submoduleDependencyService.getSubmoduleChildren(submodule).stream().map(Submodule::getId)
						.toList(),
				submodule.getSkills().stream().map(skill -> convertToSkillView(skill, completedTaskIds))
						.toList());
	}

	private EditionLevelSkillView convertToSkillView(Skill skill, Set<Long> completedTaskIds) {
		return new EditionLevelSkillView(
				skill.getId(),
				skill.getName(),
				skill.isEssential(),
				skill.isHidden(),
				skillStateService.isSkillCompleted(skill, completedTaskIds),
				!skillStateService.isSkillUnlocked(skill, completedTaskIds));
	}

}
