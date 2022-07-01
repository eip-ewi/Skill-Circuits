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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.BlockView;
import nl.tudelft.skills.dto.view.ItemView;
import nl.tudelft.skills.dto.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.model.Skill;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleLevelSkillViewDTO extends View<Skill> implements BlockView {

	@NotNull
	private Long id;
	@NotNull
	private String name;
	@NotNull
	private Boolean essential;
	@NotNull
	private Boolean hidden;
	@NotNull
	private Integer row;
	@NotNull
	private Integer column;
	@NotNull
	private CheckpointViewDTO checkpoint;
	@NotNull
	private List<TaskViewDTO> tasks;
	@NotNull
	private List<Long> parentIds;
	@NotNull
	private List<Long> childIds;

	@Override
	public void postApply() {
		super.postApply();
		this.parentIds = data.getParents().stream().map(Skill::getId).toList();
		this.childIds = data.getChildren().stream().map(Skill::getId).toList();
	}

	@Override
	public List<? extends ItemView> getItems() {
		return tasks;
	}

	public List<Long> getChildIds() {
		return childIds;
	}

	public static ModuleLevelSkillViewDTO empty() {
		return ModuleLevelSkillViewDTO.builder().id(-1L).name("").essential(true).row(-1).column(-1)
				.checkpoint(CheckpointViewDTO.empty()).tasks(new ArrayList<>())
				.parentIds(new ArrayList<>()).childIds(new ArrayList<>()).build();
	}
}
