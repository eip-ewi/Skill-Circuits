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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSubmoduleViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
public class ModuleService {

	private final ModuleRepository moduleRepository;
	private final PersonRepository personRepository;
	private final AuthorisationService authorisationService;
	private final CircuitService circuitService;
	private final PersonService personService;
	private final PathRepository pathRepository;
	private final EditionService editionService;
	private final CourseControllerApi courseApi;
	private final EditionControllerApi editionApi;

	public ModuleService(ModuleRepository moduleRepository, PersonRepository personRepository,
			AuthorisationService authorisationService, CircuitService circuitService,
			PersonService personService, PathRepository pathRepository,
			EditionService editionService, CourseControllerApi courseApi, EditionControllerApi editionApi) {
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.authorisationService = authorisationService;
		this.circuitService = circuitService;
		this.personService = personService;
		this.pathRepository = pathRepository;
		this.editionService = editionService;
		this.courseApi = courseApi;
		this.editionApi = editionApi;
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

		// Paths
		Path path = getDefaultOrPreferredPath((person != null ? person.getId() : null),
				module.getEdition().getId());

		Set<Long> taskIds = path == null ? new HashSet<>()
				: path.getTasks().stream().map(Task::getId).collect(Collectors.toSet());

		if (path != null) {
			// if path is selected (doesn't apply for no-path), show only tasks on followed path
			// tasks not in path get visibility property false
			module.getSubmodules().stream().flatMap(s -> s.getSkills().stream()).forEach(
					s -> s.setTasks(s.getTasks().stream().map(t -> {
						t.setVisible(taskIds.contains(t.getId()));
						return t;
					}).toList()));

		}

		model.addAttribute("level", "module");
		model.addAttribute("module", module);
		circuitService.setCircuitAttributes(model, positions, columns, rows);

		model.addAttribute("emptyBlock", ModuleLevelSkillViewDTO.empty());
		model.addAttribute("emptyGroup", ModuleLevelSubmoduleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);

		model.addAttribute("selectedPathId", path != null ? path.getId() : null);
		model.addAttribute("tasksInPathIds", taskIds);

		model.addAttribute("tasksAdded", personRepository.getById(person.getId()).getTasksAdded().stream()
				.map(at -> View.convert(at, TaskViewDTO.class)).toList());
		model.addAttribute("skillsModified", personRepository.getById(person.getId()).getSkillsModified()
				.stream().map(at -> View.convert(at, ModuleLevelSkillViewDTO.class)).toList());

		EditionDetailsDTO edition = editionApi.getEditionById(module.getEdition().getId()).block();
		CourseDetailsDTO course = courseApi.getCourseById(edition.getCourse().getId()).block();
		model.addAttribute("courses",
				courseApi.getAllCoursesByProgram(course.getProgram().getId()).collectList().block());
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
		Set<Long> completedTasks = personRepository.getById(personId).getTaskCompletions().stream()
				.map(tc -> tc.getTask().getId()).collect(Collectors.toSet());

		moduleViewDTO.getSubmodules().stream()
				.flatMap(sub -> sub.getSkills().stream())
				.forEach(skill -> {
					skill.setCompletedRequiredTasks(completedTasks.containsAll(skill.getRequiredTaskIds()));
					skill.getTasks()
							.forEach(task -> task.setCompleted(completedTasks.contains(task.getId())));
				});
	}

	/**
	 * Return the followed path in an edition. This is either the user preferred path or the default one.
	 *
	 * @param  personId  Authenticated person id, or null if not authenticated
	 * @param  editionId Edition id.
	 * @return           Followed path.
	 */
	public Path getDefaultOrPreferredPath(Long personId, Long editionId) {

		Path path = editionService.getDefaultPath(editionId);

		Optional<PathPreference> preference = personService.getPathForEdition(personId, editionId);
		if (personId == null || preference.isEmpty()) {
			return path;
		}

		return preference.filter(p -> p.getPath() != null)
				.flatMap(p -> pathRepository.findById(p.getPath().getId())).orElse(null);

	}

}
