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
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.SkillRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("skill")
public class SkillController {

	private SkillRepository skillRepository;

	@Autowired
	public SkillController(SkillRepository skillRepository) {
		this.skillRepository = skillRepository;
	}

	/**
	 * Creates a skill.
	 *
	 * @param  create The DTO with information to create the skill
	 * @return        A new skill html element
	 */
	@PostMapping
	@Transactional
	@PreAuthorize("@authorisationService.canCreateSkill(#create.submodule.id)")
	public String createSkill(SkillCreateDTO create, Model model) {
		Skill skill = skillRepository.save(create.apply());
		model.addAttribute("block", View.convert(skill, ModuleLevelSkillViewDTO.class));
		model.addAttribute("group", skill.getSubmodule());
		model.addAttribute("circuit", skill.getSubmodule().getModule());
		model.addAttribute("canEdit", true);
		model.addAttribute("canDelete", true);
		return "block/view";
	}

	/**
	 * Deletes a skill.
	 *
	 * @param  id The id of the skill to delete
	 * @return    A redirect to the module page
	 */
	@DeleteMapping
	@Transactional
	@PreAuthorize("@authorisationService.canDeleteSkill(#id)")
	public String deleteSkill(@RequestParam Long id) {
		Skill skill = skillRepository.findByIdOrThrow(id);
		skill.getChildren().forEach(c -> c.getParents().remove(skill));
		skillRepository.delete(skill);
		return "redirect:/module/" + skill.getSubmodule().getModule().getId();
	}

	/**
	 * Patches a skill.
	 *
	 * @param  patch The patch containing the new data
	 * @return       Empty 200 response
	 */
	@PatchMapping
	@PreAuthorize("@authorisationService.canEditSkill(#patch.id)")
	public ResponseEntity<Void> patchSkill(SkillPatchDTO patch) {
		Skill skill = skillRepository.findByIdOrThrow(patch.getId());
		skillRepository.save(patch.apply(skill));
		return ResponseEntity.ok().build();
	}

	/**
	 * Updates a skill's position.
	 *
	 * @param  id    The id of the skill to update
	 * @param  patch The patch containing the new position
	 * @return       Empty 200 response
	 */
	@PatchMapping("{id}/position")
	@PreAuthorize("@authorisationService.canEditSkill(#id)")
	public ResponseEntity<Void> updateSkillPosition(@PathVariable Long id,
			@RequestBody SkillPositionPatchDTO patch) {
		Skill skill = skillRepository.findByIdOrThrow(id);
		skillRepository.save(patch.apply(skill));
		return ResponseEntity.ok().build();
	}

}
