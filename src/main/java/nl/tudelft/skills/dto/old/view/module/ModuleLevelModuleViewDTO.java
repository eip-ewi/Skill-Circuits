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
package nl.tudelft.skills.dto.old.view.module;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.util.Pair;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.old.view.CircuitView;
import nl.tudelft.skills.dto.old.view.GroupView;
import nl.tudelft.skills.dto.old.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.dto.old.view.edition.PathViewDTO;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.SCModule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleLevelModuleViewDTO extends View<SCModule> implements CircuitView {

	@NotNull
	private Long id;
	@NotNull
	private ModuleLevelEditionViewDTO edition;
	@NotBlank
	private String name;
	@NotNull
	@PostApply
	private List<ModuleLevelSubmoduleViewDTO> submodules;
	@NotNull
	@PostApply
	private List<CheckpointViewDTO> checkpoints;

	@NotNull
	@PostApply
	private List<CheckpointViewDTO> checkpointsInEdition;

	@NotNull
	@PostApply
	private List<CheckpointViewDTO> remainingCheckpointsInEdition;

	@NotNull
	@PostApply
	private List<CheckpointViewDTO> usedCheckpointsInEdition;

	@NotNull
	private List<PathViewDTO> paths;

	@NotNull
	@PostApply
	private List<ModuleLevelExternalSkillViewDTO> externalSkillList;

	/**
	 * Gets the submodules of a module sorted in alphabetic order.
	 *
	 * @return The list of submodules
	 */
	@Override
	public List<? extends GroupView> getGroups() {
		return submodules.stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
	}

	/**
	 * Orders the checkpoints in increasing deadline order.
	 */
	@Override
	public void postApply() {
		super.postApply();
		// get all checkpoints in this edition
		this.checkpointsInEdition = data.getEdition().getCheckpoints().stream()
				.map(cp -> View.convert(cp, CheckpointViewDTO.class))
				.sorted(Comparator.comparing(CheckpointViewDTO::getDeadline, Comparator.nullsLast(
						Comparator.naturalOrder())))
				.toList();
		// get all checkpoints that contain a skill that is in this module
		Set<Long> skillIdsInModule = data.getSubmodules().stream()
				.flatMap(sub -> sub.getSkills().stream().map(AbstractSkill::getId))
				.collect(Collectors.toSet());
		this.checkpoints = data.getEdition().getCheckpoints().stream()
				.filter(checkpoint -> checkpoint.getSkills().stream()
						.anyMatch(skill -> skillIdsInModule.contains(skill.getId())))
				.map(checkpoint -> View.convert(checkpoint, CheckpointViewDTO.class)).toList();
		this.remainingCheckpointsInEdition = checkpointsInEdition.stream()
				.filter(checkpoint -> !checkpoints.contains(checkpoint)).toList();
		// get all used checkpoints in edition
		this.usedCheckpointsInEdition = checkpointsInEdition.stream()
				.filter(c -> !remainingCheckpointsInEdition.contains(c)).collect(Collectors.toList());
		// get all paths in this edition
		this.paths = data.getEdition().getPaths().stream().map(p -> View.convert(p, PathViewDTO.class))
				.toList();
		// get all external skills in edition
		this.externalSkillList = data.getExternalSkills().stream()
				.map(s -> View.convert(s, ModuleLevelExternalSkillViewDTO.class)).toList();

	}

	@Override
	public Set<Pair<Integer, Integer>> getFilledPositions() {
		var externalPositions = externalSkillList.stream()
				.map(s -> Pair.of(s.getColumn(), s.getRow())).collect(Collectors.toSet());
		var skillPositions = submodules.stream().flatMap(m -> m.getSkills().stream())
				.map(s -> Pair.of(s.getColumn(), s.getRow())).collect(Collectors.toSet());
		externalPositions.addAll(skillPositions);
		return externalPositions;
	}
}
