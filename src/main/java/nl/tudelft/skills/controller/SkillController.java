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

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.service.SkillService;

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

	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;
	private final SubmoduleRepository submoduleRepository;
	private final SkillService skillService;

	@Autowired
	public SkillController(SkillRepository skillRepository, TaskRepository taskRepository,
			SubmoduleRepository submoduleRepository,
			SkillService skillService) {
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
		this.submoduleRepository = submoduleRepository;
		this.skillService = skillService;
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
	public String createSkill(@AuthenticatedPerson Person person, @RequestBody SkillCreateDTO create,
			Model model) {
		Skill skill = skillRepository.saveAndFlush(create.apply());
		Set<Task> tasks = create.getNewItems().stream().map(dto -> {
			dto.setSkill(SkillIdDTO.builder().id(skill.getId()).build());
			return dto.apply();
		}).collect(Collectors.toSet());
		skill.setTasks(Set.copyOf(taskRepository.saveAllAndFlush(tasks)));

		return SpringContext.getBean(ModuleController.class)
				.getModulePage(person, skill.getSubmodule().getModule().getId(), model);
	}

	/**
	 * Deletes a skill.
	 *
	 * @param  id The id of the skill to delete
	 * @return    A redirect to the correct page
	 */
	@DeleteMapping
	@Transactional
	@PreAuthorize("@authorisationService.canDeleteSkill(#id)")
	public String deleteSkill(@RequestParam Long id, @RequestParam String page) {
		Skill skill = skillService.deleteSkill(id);
		return page.equals("block") ? "redirect:/module/" + skill.getSubmodule().getModule().getId()
				: "redirect:/edition/" + skill.getSubmodule().getModule().getEdition().getId();
	}

	/**
	 * Patches a skill.
	 *
	 * @param  patch The patch containing the new data in JSON format
	 * @return       The HTML for the updated skill block
	 */
	@PatchMapping
	@Transactional
	@PreAuthorize("@authorisationService.canEditSkill(#patch.id)")
	public String patchSkill(@Valid @RequestBody SkillPatchDTO patch, Model model) {
		Skill skill = skillRepository.findByIdOrThrow(patch.getId());
		skillRepository.save(patch.apply(skill));
		taskRepository.findAllByIdIn(patch.getRemovedItems())
				.forEach(t -> t.getPersons().forEach(p -> p.getTasksCompleted().remove(t)));
		taskRepository.deleteAllByIdIn(patch.getRemovedItems());

		model.addAttribute("level", "module");
		model.addAttribute("groupType", "submodule");
		model.addAttribute("block", View.convert(skill, ModuleLevelSkillViewDTO.class));
		model.addAttribute("group", skill.getSubmodule());
		model.addAttribute("circuit", buildCircuitFromSkill(skill));
		model.addAttribute("canEdit", true);
		model.addAttribute("canDelete", true);

		return "block/view";
	}

	/**
	 * Creates a circuit view from a skill.
	 *
	 * @param  skill The skill
	 * @return       The circuit view
	 */
	private ModuleLevelModuleViewDTO buildCircuitFromSkill(Skill skill) {
		return ModuleLevelModuleViewDTO.builder()
				.id(skill.getSubmodule().getModule().getId())
				.submodules(submoduleRepository.findAllByModuleId(skill.getSubmodule().getModule().getId())
						.stream()
						.map(s -> ModuleLevelSubmoduleViewDTO.builder().id(s.getId()).name(s.getName())
								.build())
						.toList())
				.build();
	}

	/**
	 * Updates a skill's position.
	 *
	 * @param  id    The id of the skill to update
	 * @param  patch The patch containing the new position
	 * @return       Empty 200 response
	 */
	@PatchMapping("{id}/position")
	@Transactional
	@PreAuthorize("@authorisationService.canEditSkill(#id)")
	public ResponseEntity<Void> updateSkillPosition(@PathVariable Long id,
			@RequestBody SkillPositionPatchDTO patch) {
		Skill skill = skillRepository.findByIdOrThrow(id);
		skillRepository.save(patch.apply(skill));
		return ResponseEntity.ok().build();
	}

	/**
	 * Connects a skill to another.
	 *
	 * @param  parentId The parent skill id
	 * @param  childId  The child skill id
	 * @return          Empty 200 response
	 */
	@Transactional
	@PostMapping("connect/{parentId}/{childId}")
	@PreAuthorize("@authorisationService.canEditSkill(#parentId) or @authorisationService.canEditSkill(#childId)")
	public ResponseEntity<Void> connectSkill(@PathVariable Long parentId, @PathVariable Long childId) {
		Skill parent = skillRepository.findByIdOrThrow(parentId);
		Skill child = skillRepository.findByIdOrThrow(childId);
		parent.getChildren().add(child);
		child.getParents().add(parent);
		skillRepository.save(parent);
		skillRepository.save(child);
		return ResponseEntity.ok().build();
	}

	/**
	 * Disconnect a skill from another.
	 *
	 * @param  parentId The parent skill id
	 * @param  childId  The child skill id
	 * @return          Empty 200 response
	 */
	@Transactional
	@PostMapping("disconnect/{parentId}/{childId}")
	@PreAuthorize("@authorisationService.canEditSkill(#parentId) or @authorisationService.canEditSkill(#childId)")
	public ResponseEntity<Void> disconnectSkill(@PathVariable Long parentId, @PathVariable Long childId) {
		Skill parent = skillRepository.findByIdOrThrow(parentId);
		Skill child = skillRepository.findByIdOrThrow(childId);
		parent.getChildren().remove(child);
		child.getParents().remove(parent);
		skillRepository.save(parent);
		skillRepository.save(child);
		return ResponseEntity.ok().build();
	}

}
