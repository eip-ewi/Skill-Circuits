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
import java.util.Set;
import java.util.stream.IntStream;

import javax.servlet.http.HttpSession;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.SCModuleCreateDTO;
import nl.tudelft.skills.dto.patch.SCModulePatchDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.service.ModuleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("module")
public class ModuleController {

	private ModuleRepository moduleRepository;
	private ModuleService moduleService;
	private HttpSession session;

	@Autowired
	public ModuleController(ModuleRepository moduleRepository, ModuleService moduleService,
			HttpSession session) {
		this.moduleRepository = moduleRepository;
		this.moduleService = moduleService;
		this.session = session;
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
	public String getModulePage(@AuthenticatedPerson(required = false) Person person, @PathVariable Long id,
			Model model) {
		ModuleLevelModuleViewDTO module = View.convert(moduleRepository.findByIdOrThrow(id),
				ModuleLevelModuleViewDTO.class);

		if (person != null) {
			moduleService.setCompletedTasksForPerson(module, person.getId());
		}

		Set<Pair<Integer, Integer>> positions = module.getFilledPositions();
		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(0) + 1;
		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(0) + 1;
		Boolean studentMode = (Boolean) session.getAttribute("student-mode-" + module.getEdition().getId());

		model.addAttribute("level", "module");
		model.addAttribute("module", module);
		model.addAttribute("columns", columns);
		model.addAttribute("rows", rows);
		if (positions.isEmpty()) {
			model.addAttribute("emptySpaces", List.of(Pair.of(0, 0)));
		} else {
			model.addAttribute("emptySpaces", IntStream.range(0, rows).boxed()
					.flatMap(row -> IntStream.range(0, columns).mapToObj(col -> Pair.of(col, row)))
					.filter(pos -> !positions.contains(pos))
					.toList());
		}

		model.addAttribute("emptyBlock", ModuleLevelSkillViewDTO.empty());
		model.addAttribute("emptyGroup", ModuleLevelSubmoduleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);
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
		SCModule module = moduleRepository.save(create.apply());
		model.addAttribute("module", module);
		model.addAttribute("edition", module.getEdition());
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
		SCModule module = moduleRepository.save(create.apply());
		model.addAttribute("module", View.convert(module, EditionLevelModuleViewDTO.class));
		return "edition-setup/module";
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
		module.getSubmodules().stream()
				.flatMap(s -> s.getSkills().stream())
				.flatMap(s -> s.getTasks().stream())
				.forEach(t -> t.getPersons()
						.forEach(p -> p.getTasksCompleted().remove(t)));
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
		module.getSubmodules().stream()
				.flatMap(s -> s.getSkills().stream())
				.flatMap(s -> s.getTasks().stream())
				.forEach(t -> t.getPersons()
						.forEach(p -> p.getTasksCompleted().remove(t)));
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
		moduleRepository.save(patch.apply(module));
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
		moduleRepository.save(patch.apply(module));

		return SpringContext.getBean(EditionController.class).getEditionPage(module.getEdition().getId(),
				model);
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

}
