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
package nl.tudelft.skills.dto.view.edition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.BlockView;
import nl.tudelft.skills.dto.view.GroupView;
import nl.tudelft.skills.dto.view.module.RegularTaskViewDTO;
import nl.tudelft.skills.model.SCModule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EditionLevelModuleViewDTO extends View<SCModule> implements GroupView {

	@NotNull
	private Long id;
	@NotNull
	private String name;
	@NotNull
	@PostApply
	private List<EditionLevelSubmoduleViewDTO> submodules;
	@NotNull
	@PostApply
	private Set<RegularTaskViewDTO> tasksWithLinks;

	@Override
	public List<? extends BlockView> getBlocks() {
		return submodules;
	}

	@Override
	public void postApply() {
		super.postApply();
		this.tasksWithLinks = this.getSubmodules().stream()
				.flatMap(submodule -> submodule.getSkills().stream())
				.flatMap(skill -> skill.getTasks().stream())
				.filter(task -> task instanceof RegularTaskViewDTO &&
						((RegularTaskViewDTO) task).getTaskInfo().getLink() != null)
				.map(task -> (RegularTaskViewDTO) task)
				.collect(Collectors.toSet());
		this.tasksWithLinks.forEach(RegularTaskViewDTO::postApply);
	}

	public int getSkillsCount() {
		return submodules.stream().mapToInt(s -> s.getSkills().size()).sum();
	}

	public static EditionLevelModuleViewDTO empty() {
		return EditionLevelModuleViewDTO.builder()
				.id(-1L).name("").submodules(new ArrayList<>()).build();
	}
}
