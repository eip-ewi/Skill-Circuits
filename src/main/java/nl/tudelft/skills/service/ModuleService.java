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
package nl.tudelft.skills.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.create.ModuleCreate;
import nl.tudelft.skills.dto.patch.ModulePatch;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;

@Service
@AllArgsConstructor
public class ModuleService {

	private final ModuleRepository moduleRepository;

	private final DTOConverter dtoConverter;

	@Transactional
	public SCModule createModule(ModuleCreate create) {
		return moduleRepository.save(create.apply(dtoConverter));
	}

	@Transactional
	public void patchModule(SCModule module, ModulePatch patch) {
		patch.apply(module, dtoConverter);
		moduleRepository.save(module);
	}

	@Transactional
	public void deleteModule(SCModule module) {
		module.getEdition().getModules().remove(module);
		moduleRepository.delete(module);
	}

}
