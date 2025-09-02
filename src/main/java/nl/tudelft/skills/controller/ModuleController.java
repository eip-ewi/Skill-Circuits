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
package nl.tudelft.skills.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.create.ModuleCreate;
import nl.tudelft.skills.dto.patch.ModulePatch;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelModuleView;
import nl.tudelft.skills.dto.view.circuit.module.ModuleLevelModuleView;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.EditionCircuitService;
import nl.tudelft.skills.service.ModuleCircuitService;
import nl.tudelft.skills.service.ModuleService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/modules")
public class ModuleController {

	private final EditionCircuitService editionCircuitService;
	private final ModuleCircuitService moduleCircuitService;
	private final ModuleService moduleService;

	@GetMapping("{module}/circuit")
	@PreAuthorize("@authorisationService.canViewModuleCircuit(#module)")
	public ModuleLevelModuleView getModuleCircuit(@AuthenticatedSCPerson SCPerson person,
			@PathEntity SCModule module) {
		return moduleCircuitService.getModuleCircuit(module, person);
	}

	@PostMapping
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#create.edition.id)")
	public EditionLevelModuleView createModule(@AuthenticatedSCPerson SCPerson person,
			@RequestBody ModuleCreate create) {
		return editionCircuitService.convertToModuleView(moduleService.createModule(create), person);
	}

	@PatchMapping("{module}")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#module)")
	public void patchModule(@PathEntity SCModule module, @RequestBody ModulePatch patch) {
		moduleService.patchModule(module, patch);
	}

	@DeleteMapping("{module}")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#module)")
	public void deleteModule(@PathEntity SCModule module) {
		moduleService.deleteModule(module);
	}

}
