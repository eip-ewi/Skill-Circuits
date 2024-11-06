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
package nl.tudelft.skills.dto.old.id;

import org.springframework.data.repository.CrudRepository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nl.tudelft.librador.dto.id.IdDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SCModuleIdDTO extends IdDTO<SCModule, Long> {

	public SCModuleIdDTO(Long id) {
		super(id);
	}

	@Override
	public Class<? extends CrudRepository<SCModule, Long>> repositoryClass() {
		return ModuleRepository.class;
	}

	@Override
	public Class<? extends SCModule> targetClass() {
		return SCModule.class;
	}

}
