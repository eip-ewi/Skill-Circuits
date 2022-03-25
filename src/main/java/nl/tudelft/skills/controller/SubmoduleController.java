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

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.SubmoduleCreateDTO;
import nl.tudelft.skills.dto.patch.SubmodulePatchDTO;
import nl.tudelft.skills.dto.patch.SubmodulePositionPatchDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.SubmoduleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("submodule")
public class SubmoduleController {

	private SubmoduleRepository submoduleRepository;

	@Autowired
	public SubmoduleController(SubmoduleRepository submoduleRepository) {
		this.submoduleRepository = submoduleRepository;
	}

	/**
	 * Creates a submodule.
	 *
	 * @param  create The DTO with information to create the submodule
	 * @return        A new submodule html element
	 */
	@PostMapping
	@Transactional
	@PreAuthorize("@authorisationService.canCreateSubmodule(#create.module.id)")
	public String createSubmodule(SubmoduleCreateDTO create, Model model) {
		Submodule submodule = submoduleRepository.save(create.apply());
		model.addAttribute("block", View.convert(submodule, EditionLevelSubmoduleViewDTO.class));
		model.addAttribute("group", submodule.getModule());
		model.addAttribute("circuit", submodule.getModule().getEdition());
		model.addAttribute("canEdit", true);
		model.addAttribute("canDelete", true);
		model.addAttribute("groupType", "module");
		return "block/view";
	}

	/**
	 * Deletes a submodule.
	 *
	 * @param  id The id of the submodule to delete
	 * @return    A redirect to the correct page
	 */
	@DeleteMapping
	@Transactional
	@PreAuthorize("@authorisationService.canDeleteSubmodule(#id)")
	public String deleteSubmodule(@RequestParam Long id, @RequestParam String page) {
		Submodule submodule = submoduleRepository.findByIdOrThrow(id);
		submoduleRepository.delete(submodule);
		return "redirect:/edition/" + submodule.getModule().getEdition().getId(); // TODO redirect to track page
	}

	/**
	 * Patches a submodule.
	 *
	 * @param  patch The patch containing the new data
	 * @return       Empty 200 response
	 */
	@PatchMapping
	@PreAuthorize("@authorisationService.canEditSubmodule(#patch.id)")
	public ResponseEntity<Void> patchSubmodule(SubmodulePatchDTO patch) {
		Submodule submodule = submoduleRepository.findByIdOrThrow(patch.getId());
		submoduleRepository.save(patch.apply(submodule));
		return ResponseEntity.ok().build();
	}

	/**
	 * Updates a submodule's position.
	 *
	 * @param  id    The id of the submodule to update
	 * @param  patch The patch containing the new position
	 * @return       Empty 200 response
	 */
	@PatchMapping("{id}/position")
	@PreAuthorize("@authorisationService.canEditSubmodule(#id)")
	public ResponseEntity<Void> updateSubmodulePosition(@PathVariable Long id,
			@RequestBody SubmodulePositionPatchDTO patch) {
		Submodule submodule = submoduleRepository.findByIdOrThrow(id);
		submoduleRepository.save(patch.apply(submodule));
		return ResponseEntity.ok().build();
	}

}
