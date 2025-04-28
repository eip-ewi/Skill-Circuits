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
package nl.tudelft.skills.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.CheckpointViewDTO;
import nl.tudelft.skills.dto.view.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.ModuleViewService;

@RestController
@AllArgsConstructor
@RequestMapping("api/module")
public class ModuleController {

	private final ModuleViewService moduleViewService;

	@GetMapping("{module}")
	public ModuleLevelModuleViewDTO getCircuit(@AuthenticatedSCPerson SCPerson person,
			@PathEntity SCModule module) {
		return moduleViewService.getCircuitView(module, person);
	}

	@GetMapping("{module}/checkpoints")
	public List<CheckpointViewDTO> getCheckpoints(@PathEntity SCModule module) {
		return moduleViewService.getCheckpointViews(module);
	}

}
