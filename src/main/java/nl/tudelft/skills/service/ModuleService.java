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
package nl.tudelft.skills.service;

import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
public class ModuleService {

	private final ModuleRepository moduleRepository;
	private final PersonRepository personRepository;
	private final CircuitService circuitService;

	public ModuleService(ModuleRepository moduleRepository, PersonRepository personRepository,
			CircuitService circuitService) {
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.circuitService = circuitService;
	}

	/**
	 * Configures the model for the module circuit view.
	 *
	 * @param person  The currently logged in person, null if none
	 * @param id      The id of the module
	 * @param model   The module to configure
	 * @param session The http session
	 */
	public void configureModuleModel(Person person, Long id, Model model, HttpSession session) {
		ModuleLevelModuleViewDTO module = View.convert(moduleRepository.findByIdOrThrow(id),
				ModuleLevelModuleViewDTO.class);

		if (person != null) {
			setCompletedTasksForPerson(module, person.getId());
		}

		Set<Pair<Integer, Integer>> positions = module.getFilledPositions();
		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(0) + 1;
		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(0) + 1;
		Boolean studentMode = (Boolean) session.getAttribute("student-mode-" + module.getEdition().getId());

		model.addAttribute("level", "module");
		model.addAttribute("module", module);
		circuitService.setCircuitAttributes(model, positions, columns, rows);

		model.addAttribute("emptyBlock", ModuleLevelSkillViewDTO.empty());
		model.addAttribute("emptyGroup", ModuleLevelSubmoduleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);
	}

	/**
	 * In module level view, marks all tasks that are in that module and have been completed by a person as
	 * completed.
	 *
	 * @param moduleViewDTO the dto to set completed attributes for
	 * @param personId      the id of the person
	 */
	@Transactional
	public void setCompletedTasksForPerson(ModuleLevelModuleViewDTO moduleViewDTO, Long personId) {
		Set<Long> completedTasks = personRepository.getById(personId).getTasksCompleted().stream()
				.map(Task::getId).collect(Collectors.toSet());

		moduleViewDTO.getSubmodules().stream()
				.flatMap(sub -> sub.getSkills().stream())
				.forEach(skill -> {
					skill.setCompletedRequiredTasks(completedTasks.containsAll(skill.getRequiredTaskIds()));
					skill.getTasks()
							.forEach(task -> task.setCompleted(completedTasks.contains(task.getId())));
				});
	}

}
