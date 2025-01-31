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
package nl.tudelft.skills.dto.view.edition;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.util.Pair;

import lombok.*;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.CircuitView;
import nl.tudelft.skills.dto.view.GroupView;
import nl.tudelft.skills.dto.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.CheckpointRepository;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EditionLevelEditionViewDTO extends View<SCEdition> implements CircuitView {

	@NotNull
	private Long id;
	@NotNull
	private EditionLevelCourseViewDTO course;
	@NotBlank
	private String name;
	@NotNull
	@PostApply
	private List<EditionLevelModuleViewDTO> modules;
	@NotNull
	@PostApply
	private List<CheckpointViewDTO> checkpointsInEdition;
	@NotNull
	private Set<PathViewDTO> paths;

	/**
	 * Gets the modules of an edition sorted in alphabetic order.
	 *
	 * @return The list of modules
	 */
	public List<EditionLevelModuleViewDTO> getModules() {
		return modules.stream()
				.sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
	}

	/**
	 * Gets the modules of an edition sorted in alphabetic order.
	 *
	 * @return The list of modules
	 */
	@Override
	public List<? extends GroupView> getGroups() {
		return modules.stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
	}

	/**
	 * Orders the checkpoints in increasing deadline order.
	 */
	@Override
	public void postApply() {
		super.postApply();
		this.checkpointsInEdition = View.convert(
				SpringContext.getBean(CheckpointRepository.class)
						.findAllById(this.data.getCheckpoints().stream().map(Checkpoint::getId).toList()),
				CheckpointViewDTO.class);
		this.checkpointsInEdition = this.checkpointsInEdition.stream()
				.sorted(Comparator.comparing(CheckpointViewDTO::getDeadline, Comparator.nullsLast(
						Comparator.naturalOrder())))
				.toList();
	}

	@Override
	public Set<Pair<Integer, Integer>> getFilledPositions() {
		return modules.stream().flatMap(m -> m.getSubmodules().stream())
				.map(s -> Pair.of(s.getColumn(), s.getRow())).collect(Collectors.toSet());
	}
}
