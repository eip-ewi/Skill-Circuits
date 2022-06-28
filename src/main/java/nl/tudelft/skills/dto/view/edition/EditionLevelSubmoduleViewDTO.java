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

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.BlockView;
import nl.tudelft.skills.dto.view.ItemView;
import nl.tudelft.skills.model.Submodule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EditionLevelSubmoduleViewDTO extends View<Submodule> implements BlockView {

	@NotNull
	private Long id;
	@NotNull
	private String name;
	@NotNull
	private Integer row;
	@NotNull
	private Integer column;
	@NotNull
	private List<EditionLevelSkillViewDTO> skills;
	@NotNull
	private List<Long> parentIds;
	@NotNull
	private List<Long> childIds;

	@Override
	public void postApply() {
		super.postApply();
		this.childIds = data.getSkills().stream()
				.flatMap(s -> s.getChildren().stream())
				.map(s -> s.getSubmodule().getId())
				.filter(id -> !this.id.equals(id))
				.distinct().toList();
		this.parentIds = data.getSkills().stream()
				.flatMap(s -> s.getParents().stream())
				.map(s -> s.getSubmodule().getId())
				.filter(id -> !this.id.equals(id))
				.distinct().toList();
	}

	@Override
	public List<? extends ItemView> getItems() {
		return skills;
	}

	@Override
	public List<Long> getChildIds() {
		return childIds;
	}

	public static EditionLevelSubmoduleViewDTO empty() {
		return EditionLevelSubmoduleViewDTO.builder()
				.id(-1L).name("").row(-1).column(-1).skills(new ArrayList<>())
				.parentIds(new ArrayList<>()).childIds(new ArrayList<>()).build();
	}

}
