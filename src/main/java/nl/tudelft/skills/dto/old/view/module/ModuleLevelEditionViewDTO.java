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

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.old.view.edition.PathViewDTO;
import nl.tudelft.skills.model.SCEdition;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleLevelEditionViewDTO extends View<SCEdition> {
	@NotNull
	private Long id;

	@NotNull
	private List<PathViewDTO> pathViews;

	@Override
	public void postApply() {
		super.postApply();
		// get all paths in this edition
//		this.pathViews = data.getPaths().stream().map(p -> View.convert(p, PathViewDTO.class)).toList();
	}

	public List<PathViewDTO> getPaths() {
		return pathViews;
	}

}
