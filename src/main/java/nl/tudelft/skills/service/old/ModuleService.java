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
package nl.tudelft.skills.service.old;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.old.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.PersonRepository;

public class ModuleService {

	private final ModuleRepository moduleRepository;
	private final PersonRepository personRepository;
	private final CircuitService circuitService;
	private final PersonService personService;
	private final TaskCompletionService taskCompletionService;
	private final ClickedLinkService clickedLinkService;
	private final CourseControllerApi courseApi;
	private final EditionControllerApi editionApi;

	public ModuleService(ModuleRepository moduleRepository, PersonRepository personRepository,
			CircuitService circuitService, PersonService personService,
			TaskCompletionService taskCompletionService,
			ClickedLinkService clickedLinkService, CourseControllerApi courseApi,
			EditionControllerApi editionApi) {
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.circuitService = circuitService;
		this.personService = personService;
		this.taskCompletionService = taskCompletionService;
		this.clickedLinkService = clickedLinkService;
		this.courseApi = courseApi;
		this.editionApi = editionApi;
	}

	// TODO handling of choice tasks: completions & their visibility

	/**
	 * Configures the model for the module circuit view.
	 *
	 * @param person  The currently logged in person (non-null)
	 * @param id      The id of the module
	 * @param model   The module to configure
	 * @param session The http session
	 */
	@Transactional
	public void configureModuleModel(Person person, Long id, Model model, HttpSession session) {
		//		ModuleLevelModuleViewDTO module = View.convert(moduleRepository.findByIdOrThrow(id),
		//				ModuleLevelModuleViewDTO.class);

		//		setCompletedTasksForPerson(module, person.getId());
		//
		//		Set<Pair<Integer, Integer>> positions = module.getFilledPositions();
		//		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(0) + 1;
		//		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(0) + 1;
		//		Boolean studentMode = (Boolean) session.getAttribute("student-mode-" + module.getEdition().getId());
		//
		//		// Set the path and modified skill/task properties, if a path is selected, get tasks in it
		//		Optional<Set<Long>> taskIdsInPath = personService.setPersonalPathAttributes(person.getId(), model,
		//				module.getEdition().getId(), null);
		//
		//		// If path is selected (doesn't apply for no-path), show only tasks on followed path
		//		// Tasks not in path get visibility property false
		//		taskIdsInPath.ifPresent(
		//				taskIdsInner -> module.getSubmodules().stream().flatMap(s -> s.getSkills().stream()).forEach(
		//						s -> s.setTasks(s.getTasks().stream()
		//								.peek(t -> t.setVisible(taskIdsInner.contains(t.getId()))).toList())));

		//		model.addAttribute("level", "module");
		//		model.addAttribute("module", module);
		//		circuitService.setCircuitAttributes(model, positions, columns, rows);
		//
		//		model.addAttribute("emptyBlock", ModuleLevelSkillViewDTO.empty());
		//		model.addAttribute("emptyGroup", ModuleLevelSubmoduleViewDTO.empty());
		//		model.addAttribute("studentMode", studentMode != null && studentMode);
		//
		//		model.addAttribute("tasksInPathIds", taskIdsInPath.orElse(new HashSet<>()));
		//		model.addAttribute("skillsRevealedIds", personRepository.getById(person.getId()).getSkillsRevealed()
		//				.stream().map(Skill::getId).collect(Collectors.toSet()));
		//
		//		EditionDetailsDTO edition = editionApi.getEditionById(module.getEdition().getId()).block();
		//		CourseDetailsDTO course = courseApi.getCourseById(edition.getCourse().getId()).block();
		//		model.addAttribute("courses",
		//				courseApi.getAllCoursesByProgram(course.getProgram().getId())
		//						.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collectList().block());
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

		//		moduleViewDTO.getSubmodules().stream()
		//				.flatMap(sub -> sub.getSkills().stream())
		//				.forEach(skill -> {
		//					skill.setCompletedRequiredTasks(completedTasks.containsAll(skill.getRequiredTaskIds()));
		//					skill.getTasks()
		//							.stream().flatMap(task -> {
		//								if (task instanceof nl.tudelft.skills.dto.view.module.RegularTaskViewDTO view) {
		//									return Stream.of(view);
		//								} else if (task instanceof nl.tudelft.skills.dto.view.module.ChoiceTaskViewDTO view) {
		//									return view.getTasks().stream();
		//								}
		//								return Stream.empty();
		//							}).forEach(view -> {
		//								view.getTaskInfo().setCompleted(completedTasks.contains(view.getId()));
		//							});
		//				});
	}

	/**
	 * Deletes a given module by its id.
	 *
	 * @param  moduleId The id of the module to delete.
	 * @return          The module that was deleted.
	 */
	@Transactional
	public SCModule deleteModule(Long moduleId) {
		SCModule module = moduleRepository.findByIdOrThrow(moduleId);

		// Delete all task completions and links (only necessary for RegularTasks)
		List<RegularTask> tasks = module.getSubmodules().stream()
				.flatMap(s -> s.getSkills().stream())
				.flatMap(s -> s.getTasks().stream())
				.filter(t -> t instanceof RegularTask)
				.map(t -> (RegularTask) t).collect(Collectors.toList());
		tasks.forEach(taskCompletionService::deleteTaskCompletionsOfTask);
		clickedLinkService.deleteClickedLinksForTasks(tasks);

		moduleRepository.delete(module);
		return module;
	}
}
