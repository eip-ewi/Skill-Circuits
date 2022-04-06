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
package nl.tudelft.skills.dto.view.module;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.CircuitView;
import nl.tudelft.skills.dto.view.GroupView;
import nl.tudelft.skills.dto.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.EditionRepository;

import org.springframework.data.util.Pair;

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

	@Override
	public List<? extends GroupView> getGroups() {
		return submodules;
	}

	@Override
	public void postApply() {
		super.postApply();
		// get all checkpoints that contain a skill that is in this module.
		Set<Long> skillIdsInModule = data.getSubmodules().stream()
				.flatMap(sub -> sub.getSkills().stream().map(Skill::getId)).collect(Collectors.toSet());
		this.checkpoints = SpringContext.getBean(EditionRepository.class)
				.findByIdOrThrow(data.getEdition().getId())
				.getCheckpoints().stream()
				.filter(checkpoint -> checkpoint.getSkills().stream()
						.anyMatch(skill -> skillIdsInModule.contains(skill.getId())))
				.map(checkpoint -> View.convert(checkpoint, CheckpointViewDTO.class)).toList();
	}

	@Override
	public Set<Pair<Integer, Integer>> getFilledPositions() {
		return submodules.stream().flatMap(m -> m.getSkills().stream())
				.map(s -> Pair.of(s.getColumn(), s.getRow())).collect(Collectors.toSet());
	}
}
