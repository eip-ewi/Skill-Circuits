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

import java.util.List;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.create.ExternalSkillCreate;
import nl.tudelft.skills.dto.create.SkillCreate;
import nl.tudelft.skills.dto.patch.SkillPatch;
import nl.tudelft.skills.dto.view.circuit.module.ModuleLevelSkillView;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.ModuleCircuitService;
import nl.tudelft.skills.service.SkillService;
import nl.tudelft.skills.service.TaskCompletionService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/skills")
public class SkillController {

	private final ModuleCircuitService moduleCircuitService;
	private final SkillService skillService;
	private final TaskCompletionService taskCompletionService;

	@GetMapping("{skill}/module")
	@PreAuthorize("@authorisationService.canViewSkill(#skill)")
	public Long getModuleFromSkill(@PathEntity AbstractSkill skill) {
		return skill.getSubmodule().getModule().getId();
	}

	@GetMapping("last-active")
	public Long getLastActiveSkill(@AuthenticatedSCPerson SCPerson person) {
		return taskCompletionService.getLastCompletedTask(person).map(task -> task.getSkill().getId())
				.orElseThrow(ResourceNotFoundException::new);
	}

	@GetMapping("unlocked")
	public List<Long> getUnlockedSkills(@AuthenticatedSCPerson SCPerson person) {
		return person.getSkillsRevealed().stream().map(AbstractSkill::getId).toList();
	}

	@PostMapping
	@PreAuthorize("@authorisationService.canEditSubmodule(#create.submodule)")
	public ModuleLevelSkillView createSkill(@AuthenticatedSCPerson SCPerson person,
			@RequestBody SkillCreate create) {
		return moduleCircuitService.convertToSkillView(skillService.createSkill(create), person);
	}

	@PostMapping("external")
	@PreAuthorize("@authorisationService.canEditModuleCircuit(#create.module)")
	public ModuleLevelSkillView createExternalSkill(@AuthenticatedSCPerson SCPerson person,
			@RequestBody ExternalSkillCreate create) {
		return moduleCircuitService.convertToSkillView(skillService.createSkill(create), person);
	}

	@PatchMapping("{skill}")
	@PreAuthorize("@authorisationService.canEditSkill(#skill)")
	public void patchSkill(@PathEntity AbstractSkill skill, @RequestBody SkillPatch patch) {
		skillService.patchSkill(skill, patch);
	}

	@DeleteMapping("{skill}")
	@PreAuthorize("@authorisationService.canEditSkill(#skill)")
	public void deleteSkill(@PathEntity AbstractSkill skill) {
		skillService.deleteSkill(skill);
	}

	@PatchMapping("{skill}/position")
	@PreAuthorize("@authorisationService.canEditSkill(#skill)")
	public void updatePosition(@PathEntity AbstractSkill skill, @RequestParam Integer column) {
		skillService.updatePosition(skill, column);
	}

	@DeleteMapping("{skill}/position")
	@PreAuthorize("@authorisationService.canEditSkill(#skill)")
	public void removeSkillFromCircuit(@PathEntity AbstractSkill skill) {
		skillService.updatePosition(skill, null);
	}

	@PostMapping("connections/{from}/{to}")
	@PreAuthorize("@authorisationService.canEditSkill(#from) and @authorisationService.canEditSkill(#to)")
	public void connect(@PathEntity AbstractSkill from, @PathEntity AbstractSkill to) {
		skillService.connect(from, to);
	}

	@DeleteMapping("connections/{from}/{to}")
	@PreAuthorize("@authorisationService.canEditSkill(#from) and @authorisationService.canEditSkill(#to)")
	public void disconnect(@PathEntity AbstractSkill from, @PathEntity AbstractSkill to) {
		skillService.disconnect(from, to);
	}

}
