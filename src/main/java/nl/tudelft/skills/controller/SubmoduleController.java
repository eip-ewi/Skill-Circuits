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
import nl.tudelft.skills.dto.create.SubmoduleCreate;
import nl.tudelft.skills.dto.patch.SubmodulePatch;
import nl.tudelft.skills.dto.view.SubmoduleView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelSubmoduleView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.service.EditionCircuitService;
import nl.tudelft.skills.service.SubmoduleService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/submodules")
public class SubmoduleController {

	private final SubmoduleService submoduleService;
	private final EditionCircuitService editionCircuitService;

	@GetMapping("{submodule}")
	@PreAuthorize("@authorisationService.canViewModuleCircuit(#submodule.module)")
	public SubmoduleView getSubmoduleInfo(@PathEntity Submodule submodule) {
		return new SubmoduleView(submodule.getModule().getId());
	}

	@PostMapping
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#create.module)")
	public EditionLevelSubmoduleView createSubmodule(@AuthenticatedSCPerson SCPerson person,
			@RequestBody SubmoduleCreate create) {
		return editionCircuitService.convertToSubmoduleView(submoduleService.createSubmodule(create), person);
	}

	@PatchMapping("{submodule}")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#submodule.module)")
	public void patchSubmodule(@PathEntity Submodule submodule, @RequestBody SubmodulePatch patch) {
		submoduleService.patchSubmodule(submodule, patch);
	}

	@PatchMapping("{submodule}/position")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#submodule.module)")
	public void updatePosition(@PathEntity Submodule submodule, @RequestParam Integer column) {
		submoduleService.updatePosition(submodule, column);
	}

	@DeleteMapping("{submodule}/position")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#submodule.module)")
	public void removeSubmoduleFromCircuit(@PathEntity Submodule submodule) {
		submoduleService.updatePosition(submodule, null);
	}

	@DeleteMapping("{submodule}")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#submodule.module)")
	public void deleteSubmodule(@PathEntity Submodule submodule) {
		submoduleService.deleteSubmodule(submodule);
	}

}
