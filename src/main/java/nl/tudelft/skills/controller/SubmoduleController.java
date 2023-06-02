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

import java.util.HashSet;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.SubmoduleCreateDTO;
import nl.tudelft.skills.dto.patch.SubmodulePatchDTO;
import nl.tudelft.skills.dto.patch.SubmodulePositionPatchDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import nl.tudelft.skills.service.EditionService;
import nl.tudelft.skills.service.SkillService;

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

	private final SubmoduleRepository submoduleRepository;
	private final ModuleRepository moduleRepository;
	private final SkillService skillService;
	private final EditionService editionService;
	private final HttpSession session;

	@Autowired
	public SubmoduleController(SubmoduleRepository submoduleRepository, ModuleRepository moduleRepository,
			SkillService skillService, EditionService editionService, HttpSession session) {
		this.submoduleRepository = submoduleRepository;
		this.moduleRepository = moduleRepository;
		this.skillService = skillService;
		this.editionService = editionService;
		this.session = session;
	}

	/**
	 * Creates a submodule.
	 *
	 * @param  create The DTO with information to create the submodule
	 * @return        The new version of the edition page for updating locally.
	 */
	@PostMapping
	@PreAuthorize("@authorisationService.canCreateSubmodule(#create.module.id)")
	public String createSubmodule(@RequestBody SubmoduleCreateDTO create, Model model) {
		Submodule submodule = submoduleRepository.saveAndFlush(create.apply());
		editionService.configureEditionModel(submodule.getModule().getEdition().getId(), model, session);
		return "edition/view";
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
		submodule.getSkills().stream().map(AbstractSkill::getId).forEach(skillService::deleteSkill);
		submodule.setSkills(new HashSet<>());
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
	@Transactional
	@PreAuthorize("@authorisationService.canEditSubmodule(#patch.id)")
	public String patchSubmodule(@Valid @RequestBody SubmodulePatchDTO patch, Model model) {
		Submodule submodule = submoduleRepository.findByIdOrThrow(patch.getId());
		submoduleRepository.save(patch.apply(submodule));
		patch.getRemovedItems().forEach(skillService::deleteSkill);

		model.addAttribute("level", "edition");
		model.addAttribute("groupType", "module");
		model.addAttribute("block", View.convert(submodule, EditionLevelSubmoduleViewDTO.class));
		model.addAttribute("group", submodule.getModule());
		model.addAttribute("circuit", buildCircuitFromSubmodule(submodule));
		model.addAttribute("canEdit", true);
		model.addAttribute("canDelete", true);

		return "block/view";
	}

	/**
	 * Creates a circuit view from a submodule.
	 *
	 * @param  submodule The submodule
	 * @return           The circuit view
	 */
	private EditionLevelEditionViewDTO buildCircuitFromSubmodule(Submodule submodule) {
		return EditionLevelEditionViewDTO.builder()
				.id(submodule.getModule().getEdition().getId())
				.modules(moduleRepository.findAllByEditionId(submodule.getModule().getEdition().getId())
						.stream()
						.map(m -> EditionLevelModuleViewDTO.builder().id(m.getId()).name(m.getName()).build())
						.toList())
				.build();
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
