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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.old.create.SCModuleCreateDTO;
import nl.tudelft.skills.dto.old.patch.SCModulePatchDTO;
import nl.tudelft.skills.dto.old.view.SkillSummaryDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.old.ClickedLinkService;
import nl.tudelft.skills.service.old.ModuleService;
import nl.tudelft.skills.service.old.TaskCompletionService;

@RequestMapping("module")
public class ModuleController {

	private ModuleRepository moduleRepository;
	private ModuleService moduleService;
	private HttpSession session;
	private final TaskCompletionService taskCompletionService;
	private final ClickedLinkService clickedLinkService;

	//	Playlist feature
	private AuthorisationService authorisationService;

	@Autowired
	public ModuleController(ModuleRepository moduleRepository, ModuleService moduleService,
			HttpSession session, TaskCompletionService taskCompletionService,
			ClickedLinkService clickedLinkService,
			AuthorisationService authorisationService) {
		this.moduleRepository = moduleRepository;
		this.moduleService = moduleService;
		this.session = session;
		this.taskCompletionService = taskCompletionService;
		this.clickedLinkService = clickedLinkService;
		this.authorisationService = authorisationService;
	}

	/**
	 * Gets the page for a single module. This page contains a circuit with all submodules, skills, and tasks
	 * in the module. If a person is authenticated, marks the tasks they've completed as completed.
	 *
	 * @param  person The currently authenticated person. Null if no authenticated person.
	 * @param  id     The id of the module
	 * @param  model  The model to add data to
	 * @return        The page to load
	 */
	@GetMapping("{id}")
	@PreAuthorize("@authorisationService.canViewModule(#id)")
	public String getModulePage(@AuthenticatedPerson(required = false) Person person, @PathVariable Long id,
			Model model) {
		moduleService.configureModuleModel(person, id, model, session);

		//		Playlist feature
		long accId = 643L;
		//		if (moduleRepository.findByIdOrThrow(id).getEdition().getId() == accId
		//				& !authorisationService.canEditEdition(accId)) {
		//			researchParticipantService.addRPInfoToModel(person, model);
		//		}
		return "module/view";
	}

	/**
	 * Creates a module.
	 *
	 * @param  create The DTO with information to create the module
	 * @return        A new module html element
	 */
	@PostMapping
	@Transactional
	@PreAuthorize("@authorisationService.canCreateModuleInEdition(#create.edition.id)")
	public String createModule(SCModuleCreateDTO create, Model model) {
		//		SCModule module = moduleRepository.save(create.apply());
		//		model.addAttribute("module", module);
		//		model.addAttribute("edition", module.getEdition());
		return "module/block";
	}

	/**
	 * Creates a module, and returns a module element for the setup sidebar.
	 *
	 * @param  create The DTO with information to create the module
	 * @return        A new module html element
	 */
	@PostMapping("setup")
	@Transactional
	@PreAuthorize("@authorisationService.canCreateModuleInEdition(#create.edition.id)")
	public String createModuleInEditionSetup(SCModuleCreateDTO create, Model model) {
		//		SCModule module = moduleRepository.save(create.apply());
		//		model.addAttribute("module", View.convert(module, EditionLevelModuleViewDTO.class));
		return "edition_setup/module";
	}

	/**
	 * Deletes a module, and redirects to the edition page.
	 *
	 * @param  id The id of the module to delete
	 * @return    A redirect to the edition page
	 */
	@DeleteMapping
	@Transactional
	@PreAuthorize("@authorisationService.canDeleteModule(#id)")
	public String deleteModule(@RequestParam Long id) {
		SCModule module = moduleRepository.findByIdOrThrow(id);
		var tasks = module.getSubmodules().stream()
				.flatMap(s -> s.getSkills().stream())
				.flatMap(s -> s.getTasks().stream()).toList();
		tasks.forEach(taskCompletionService::deleteTaskCompletionsOfTask);
		//		clickedLinkService.deleteClickedLinksForTasks(tasks);
		moduleRepository.delete(module);
		return "redirect:/edition/" + module.getEdition().getId();
	}

	/**
	 * Deletes a module.
	 *
	 * @param  id The id of the module to delete
	 * @return    A redirect to the edition page
	 */
	@DeleteMapping("setup")
	@Transactional
	@PreAuthorize("@authorisationService.canDeleteModule(#id)")
	public ResponseEntity<Void> deleteModuleSetup(@RequestParam Long id) {
		SCModule module = moduleRepository.findByIdOrThrow(id);
		var tasks = module.getSubmodules().stream()
				.flatMap(s -> s.getSkills().stream())
				.flatMap(s -> s.getTasks().stream()).toList();
		tasks.forEach(taskCompletionService::deleteTaskCompletionsOfTask);
		//		clickedLinkService.deleteClickedLinksForTasks(tasks);
		moduleRepository.delete(module);
		return ResponseEntity.ok().build();
	}

	/**
	 * Patches a module.
	 *
	 * @param  patch The patch containing the new data
	 * @return       Empty 200 response
	 */
	@PatchMapping
	@PreAuthorize("@authorisationService.canEditModule(#patch.id)")
	public ResponseEntity<Void> patchModule(SCModulePatchDTO patch) {
		SCModule module = moduleRepository.findByIdOrThrow(patch.getId());
		//		moduleRepository.save(patch.apply(module));
		return ResponseEntity.ok().build();
	}

	/**
	 * Patches a module, and returns a new edition page html element.
	 *
	 * @param  patch The patch containing the new data
	 * @return       The new circuit
	 */
	@PatchMapping("/setup")
	@PreAuthorize("@authorisationService.canEditModule(#patch.id)")
	public String patchModuleSetup(SCModulePatchDTO patch, Model model) {
		SCModule module = moduleRepository.findByIdOrThrow(patch.getId());
		//		moduleRepository.save(patch.apply(module));

		return "";
		//		return SpringContext.getBean(EditionController.class).getEditionPage(module.getEdition().getId(),
		//				"circuit", model);
	}

	/**
	 * Toggles student mode for a specific edition from a module.
	 *
	 * @param  id The id of the module
	 * @return    The module page
	 */
	@PostMapping("{id}/studentmode")
	public String toggleStudentMode(@PathVariable Long id) {
		Long editionId = moduleRepository.findByIdOrThrow(id).getEdition().getId();
		Boolean currentStudentMode = (Boolean) session.getAttribute("student-mode-" + editionId);
		session.setAttribute("student-mode-" + editionId,
				currentStudentMode == null || !currentStudentMode);
		return "redirect:/module/{id}";
	}

	/**
	 * Gets the skills of a module sorted in alphabetic order.
	 *
	 * @param  id The id of the module
	 * @return    The list of skills
	 */
	@GetMapping("{id}/skills")
	@PreAuthorize("@authorisationService.canGetSkillsOfModule(#id)")
	public @ResponseBody List<SkillSummaryDTO> getSkillsOfModule(@PathVariable Long id) {
		//		return View.convert(moduleRepository.findByIdOrThrow(id).getSubmodules().stream()
		//				.flatMap(s -> s.getSkills().stream())
		//				.sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList(),
		//				SkillSummaryDTO.class);
		return Collections.emptyList();
	}

}
