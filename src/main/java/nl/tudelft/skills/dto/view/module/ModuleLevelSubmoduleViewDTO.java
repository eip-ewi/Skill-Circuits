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
package nl.tudelft.skills.dto.view.module;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.BlockView;
import nl.tudelft.skills.dto.view.GroupView;
import nl.tudelft.skills.model.Submodule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleLevelSubmoduleViewDTO extends View<Submodule> implements GroupView {

	@NotNull
	private Long id;
	@NotNull
	private String name;
	@NotNull
	@PostApply
	private List<ModuleLevelSkillViewDTO> skills;
	@NotNull
	private List<Long> childIds;

	@Override
	public List<? extends BlockView> getBlocks() {
		return skills;
	}

	public static ModuleLevelSubmoduleViewDTO empty() {
		return ModuleLevelSubmoduleViewDTO.builder()
				.id(-1L)
				.name("")
				.skills(new ArrayList<>())
				.childIds(new ArrayList<>()).build();

	}
}
