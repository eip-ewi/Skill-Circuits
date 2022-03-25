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
package nl.tudelft.skills.dto.patch;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.SCModuleIdDTO;
import nl.tudelft.skills.model.Submodule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmodulePatchDTO extends Patch<Submodule> {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@NotNull
	private SCModuleIdDTO module;

	@Override
	protected void applyOneToOne() {
		updateNonNull(name, data::setName);
		updateNonNullId(module, data::setModule);
	}

	@Override
	protected void validate() {
	}
}
