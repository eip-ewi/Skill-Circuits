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
package nl.tudelft.skills.controller.old;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.old.create.ExternalSkillCreateDTO;
import nl.tudelft.skills.dto.old.create.SkillCreateDTO;
import nl.tudelft.skills.dto.old.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.dto.old.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.PersonRepository;
import nl.tudelft.skills.service.old.*;

@RequestMapping("skill")
@AllArgsConstructor
public class SkillController {

	private final SkillRepository skillRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final AbstractSkillRepository abstractSkillRepository;
	private final TaskRepository taskRepository;
	private final SubmoduleRepository submoduleRepository;
	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final PersonRepository personRepository;
	private final SkillService skillService;
	private final ModuleService moduleService;
	private final TaskCompletionService taskCompletionService;
	private final ClickedLinkService clickedLinkService;
	private final PersonService personService;
	private final HttpSession session;

	/**
	 * Gets a single skill by id.
	 *
	 * @param  id The id of the skill
	 * @return    The skill html element
	 */
	@GetMapping("{id}")
	@PreAuthorize("@authorisationService.canViewSkill(#id)")
	public String getSkill(@AuthenticatedPerson Person person, @PathVariable Long id, Model model) {
		Skill skill = skillRepository.findByIdOrThrow(id);
		//		ModuleLevelSkillViewDTO view = View.convert(skill, ModuleLevelSkillViewDTO.class);
		//		view.setCompletedRequiredTasks(true);

		// Set completed tasks
		Set<Long> completedTasks = personRepository.getById(person.getId()).getTaskCompletions().stream()
				.map(tc -> tc.getTask().getId()).collect(Collectors.toSet());
		//		view.getTasks().forEach(t -> t.setCompleted(completedTasks.contains(t.getId())));

		// Add general model attributes
		model.addAttribute("level", "module");
		model.addAttribute("groupType", "submodule");
		//		model.addAttribute("block", view);
		model.addAttribute("group", skill.getSubmodule());
		model.addAttribute("circuit", buildCircuitFromSkill(skill));
		model.addAttribute("canEdit", false);
		model.addAttribute("canDelete", false);

		// Add information concerning personal path to model
		Optional<Set<Long>> taskIdsInPath = personService.setPersonalPathAttributes(person.getId(), model,
				skill.getSubmodule().getModule().getEdition().getId(), skill);
		//		taskIdsInPath.ifPresent(
		//				taskIdsInner -> view.getTasks().forEach(t -> t.setVisible(taskIdsInner.contains(t.getId()))));

		return "block/view";
	}

	/**
	 * Creates a skill.
	 *
	 * @param  create The DTO with information to create the skill
	 * @return        A new circuit html element
	 */
	@PostMapping
	@Transactional
	@PreAuthorize("@authorisationService.canCreateSkill(#create.submodule.id)")
	public String createSkill(@AuthenticatedPerson Person person, @RequestBody SkillCreateDTO create,
			Model model) {
		//		if (create.getCheckpoint() == null) {
		//			create.setCheckpoint(new CheckpointIdDTO(
		//					checkpointRepository.saveAndFlush(create.getCheckpointCreate().apply()).getId()));
		//		}
		//		Skill skill = skillRepository.saveAndFlush(create.apply());
		//		List<Task> tasks = create.getNewItems().stream()
		//				.sorted(Comparator.comparingInt(TaskCreateDTO::getIndex).reversed()).map(dto -> {
		//					dto.setSkill(SkillIdDTO.builder().id(skill.getId()).build());
		//					return dto.apply();
		//				}).toList();
		//		for (Task task : tasks) {
		//			task.setSkill(skill);
		//		}
		//		skill.setTasks(taskRepository.saveAll(tasks));

		//		checkpointRepository.findBySkillsContains(skill).getSkills().add(skill);

		// New tasks will be included in all paths by default
		//		SCEdition edition = skill.getSubmodule().getModule().getEdition();

		//		Set<Path> paths = new HashSet<>(pathRepository.findAllByEditionId(edition.getId()));
		//		tasks.forEach(t -> {
		//			t.setPaths(paths);
		//			taskRepository.save(t);
		//		});

		//		moduleService.configureModuleModel(person, skill.getSubmodule().getModule().getId(), model, session);
		return "module/view";
	}

	/**
	 * Creates an external skill.
	 *
	 * @param  create The DTO with information to create the skill
	 * @return        A new circuit html element
	 */
	@Transactional
	@PostMapping("external")
	@PreAuthorize("@authorisationService.canCreateSkillInModule(#create.module.id)")
	public String createSkill(@AuthenticatedPerson Person person, @RequestBody ExternalSkillCreateDTO create,
			Model model) {
		//		ExternalSkill skill = externalSkillRepository.saveAndFlush(create.apply());
		//		skill.setSkill(skillRepository.findByIdOrThrow(skill.getSkill().getId()));
		//
		//		moduleService.configureModuleModel(person, skill.getModule().getId(), model, session);
		return "module/view";
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
		AbstractSkill skill = skillService.deleteSkill(id);
		SCModule module = skill instanceof ExternalSkill s ? s.getModule() : skill.getSubmodule().getModule();
		return page.equals("block") ? "redirect:/module/" + module.getId()
				: "redirect:/edition/" + module.getEdition().getId();
	}

	/**
	 * Patches a skill.
	 *
	 * @param  patch The patch containing the new data in JSON format
	 * @return       The HTML for the updated skill block
	 */
	//	@PatchMapping
	//	@Transactional
	//	@PreAuthorize("@authorisationService.canEditSkill(#patch.id)")
	//	public String patchSkill(@Valid @RequestBody SkillPatchDTO patch, Model model) {
	//		Skill skill = skillRepository.findByIdOrThrow(patch.getId());
	//		List<Task> oldTasks = skill.getTasks();
	////		skillRepository.save(patch.apply(skill));
	//
	//		// Remove selected tasks from custom skill in person
	//		skill.getPersonModifiedSkill().forEach(p -> {
	//			p.setTasksAdded(p.getTasksAdded().stream()
	//					.filter(t -> !patch.getRemovedItems().contains(t.getId())).collect(Collectors.toSet()));
	//			personRepository.save(p);
	//		});
	//
	//		taskRepository.findAllByIdIn(patch.getRemovedItems())
	//				.forEach(taskCompletionService::deleteTaskCompletionsOfTask);
	//		clickedLinkService.deleteClickedLinksForTasks(taskRepository.findAllByIdIn(patch.getRemovedItems()));
	//
	//		taskRepository.deleteAllByIdIn(patch.getRemovedItems());
	//		taskRepository.saveAll(skill.getRequiredTasks());
	//
	//		// New tasks will be included in all paths by default
	//		SCEdition edition = skill.getSubmodule().getModule().getEdition();
	//
	//		Set<Path> paths = new HashSet<>(pathRepository.findAllByEditionId(edition.getId()));
	//		skillRepository.findByIdOrThrow(skill.getId()).getTasks().stream().filter(t -> !oldTasks.contains(t))
	//				.forEach(t -> {
	//					t.setPaths(paths);
	//					t.setSkill(skill);
	//					taskRepository.save(t);
	//				});
	//
	//		model.addAttribute("level", "module");
	//		model.addAttribute("groupType", "submodule");
	////		model.addAttribute("block", View.convert(skill, ModuleLevelSkillViewDTO.class));
	//		model.addAttribute("group", skill.getSubmodule());
	//		model.addAttribute("circuit", buildCircuitFromSkill(skill));
	//		model.addAttribute("canEdit", true);
	//		model.addAttribute("canDelete", true);
	//		Boolean studentMode = (Boolean) session
	//				.getAttribute("student-mode-" + skill.getSubmodule().getModule().getEdition().getId());
	//		model.addAttribute("studentMode", studentMode != null && studentMode);
	//
	//		return "block/view";
	//	}

	/**
	 * Creates a circuit view from a skill.
	 *
	 * @param  skill The skill
	 * @return       The circuit view
	 */
	private ModuleLevelModuleViewDTO buildCircuitFromSkill(Skill skill) {
		//		return ModuleLevelModuleViewDTO.builder()
		//				.id(skill.getSubmodule().getModule().getId())
		//				.submodules(submoduleRepository.findAllByModuleId(skill.getSubmodule().getModule().getId())
		//						.stream()
		//						.map(s -> ModuleLevelSubmoduleViewDTO.builder().id(s.getId()).name(s.getName())
		//								.build())
		//						.toList())
		//				.edition(ModuleLevelEditionViewDTO.builder()
		//						.id(skill.getSubmodule().getModule().getEdition().getId()).build())
		//				.build();
		return null;
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
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(id);
		//		abstractSkillRepository.save(patch.apply(skill));
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
		AbstractSkill parent = abstractSkillRepository.findByIdOrThrow(parentId);
		AbstractSkill child = abstractSkillRepository.findByIdOrThrow(childId);
		parent.getChildren().add(child);
		child.getParents().add(parent);
		abstractSkillRepository.save(parent);
		abstractSkillRepository.save(child);
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
		AbstractSkill parent = abstractSkillRepository.findByIdOrThrow(parentId);
		AbstractSkill child = abstractSkillRepository.findByIdOrThrow(childId);
		parent.getChildren().remove(child);
		child.getParents().remove(parent);
		abstractSkillRepository.save(parent);
		abstractSkillRepository.save(child);
		return ResponseEntity.ok().build();
	}

	/**
	 * Redirects to the correct skill when an external skill is clicked. The link should redirect to the skill
	 * in the most recent edition which the person has last worked on. If none such edition exists, the most
	 * recent edition is chosen. If there is no skill that the person can view, the current skills view is
	 * rendered.
	 *
	 * @param  skillId    The id of the external skill.
	 * @param  authPerson The currently logged in person.
	 * @return            The redirection link to the module in which the correct skill is.
	 */
	@GetMapping("external/{skillId}")
	@PreAuthorize("@authorisationService.canViewSkill(#skillId)")
	public String redirectToExternalSkill(@PathVariable Long skillId,
			@AuthenticatedPerson Person authPerson) {
		ExternalSkill externalSkill = externalSkillRepository.findByIdOrThrow(skillId);
		Skill redirectedSkill = skillService.recentActiveEditionForSkillOrLatest(authPerson.getId(),
				externalSkill);

		// If there is no valid skill to link to, return to the initial page
		return "redirect:/module/"
				+ (redirectedSkill != null ? (redirectedSkill.getSubmodule().getModule().getId() +
						"#block-" + redirectedSkill.getId() + "-name") : externalSkill.getModule().getId());
	}
}
