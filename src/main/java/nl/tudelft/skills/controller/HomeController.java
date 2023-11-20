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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.EditionService;

@Controller
public class HomeController {

	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;

	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;
	private final PersonRepository personRepository;

	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;

	private final EditionService editionService;
	private final CourseService courseService;

	private final PathPreferenceRepository pathPreferenceRepository;

	@Autowired
	public HomeController(EditionControllerApi editionApi, RoleControllerApi roleApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository,
			PersonRepository personRepository, SkillRepository skillRepository, TaskRepository taskRepository,
			EditionService editionService,
			CourseService courseService,
			PathPreferenceRepository pathPreferenceRepository) {
		this.editionApi = editionApi;
		this.roleApi = roleApi;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
		this.editionService = editionService;
		this.courseService = courseService;
		this.pathPreferenceRepository = pathPreferenceRepository;
	}

	/**
	 * Gets the home page. Loads all courses that the authenticated person teaches or that have active and
	 * visible editions.
	 *
	 * @param  person The authenticated person, or null if the user is unauthenticated
	 * @param  model  The model to add details to
	 * @return        The home page
	 */
	@Transactional
	@GetMapping("/")
	public String getHomePage(@AuthenticatedPerson(required = false) Person person, Model model) {
		List<EditionDetailsDTO> editions = person == null
				? editionApi.getEditionsById(editionApi.getAllEditionsActiveAtDate(LocalDateTime.now())
						.map(EditionSummaryDTO::getId).collectList().block()).collectList().block()
				: editionApi.getAllEditionsActiveOrTaughtBy(person.getId()).collectList().block();
		Set<Long> visible = editionRepository
				.findAllById(editions.stream().map(EditionDetailsDTO::getId).toList()).stream()
				.filter(SCEdition::isVisible).map(SCEdition::getId).collect(Collectors.toSet());
		Set<Long> teacherIds = person == null ? Set.of()
				: roleApi
						.getRolesById(
								editions.stream().map(EditionDetailsDTO::getId).collect(Collectors.toSet()),
								Set.of(person.getId()))
						.collectList().block().stream()
						.filter(r -> r.getType() == RoleDetailsDTO.TypeEnum.TEACHER)
						.map(r -> r.getEdition().getId()).collect(Collectors.toSet());

		List<CourseSummaryDTO> courses = editions.stream()
				.filter(e -> visible.contains(e.getId()) || teacherIds.contains(e.getId()))
				.map(EditionDetailsDTO::getCourse).distinct().toList();

		// Gets number of completed skills for each course
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		if (person != null) {
			SCPerson scperson = personRepository.findByIdOrThrow(person.getId());

			completedSkillsPerCourse = getCompletedSkillsPerCourse(courses, scperson);
		}

		boolean isAnySkillCompleted = completedSkillsPerCourse.entrySet().stream()
				.anyMatch(e -> e.getValue() > 0);
		model.addAttribute("courses", courses);
		model.addAttribute("completedSkillsPerCourse", completedSkillsPerCourse);
		model.addAttribute("isAnySkillCompleted", isAnySkillCompleted);
		return "index";
	}

	/**
	 * Returns a map of courses and the number of skills completed in a respective edition in that course.
	 *
	 * @param  courses  List of courses
	 * @param  scperson person
	 * @return          Map containing number of completed skills per course.
	 */
	public Map<Long, Integer> getCompletedSkillsPerCourse(List<CourseSummaryDTO> courses, SCPerson scperson) {
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		var ownSkills = scperson.getSkillsModified();
		for (var course : courses) {
			Long courseId = course.getId();

			int skillsDoneCount = 0;
			Long editionId = courseService.getLastStudentEditionForCourseOrLast(courseId);

			if (editionId != null) {
				List<Task> tasksDone = scperson.getTaskCompletions().stream().map(TaskCompletion::getTask)
						.toList();

				var personPathPreference = pathPreferenceRepository
						.findAllByPersonIdAndEditionId(scperson.getId(), editionId);

				Set<Skill> skillsWithTasks;
				Set<Skill> skillsDone;

				Set<Skill> ownSkillsDone = ownSkills.stream()
						.filter(s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
								editionId))
						.filter(s -> tasksDone.containsAll(
								scperson.getTasksAdded().stream().filter(t -> t.getSkill().equals(s))
										.toList()))
						.collect(Collectors.toSet());

				if (personPathPreference.isEmpty() || personPathPreference.get(0).getPath() == null) {
					var touchedSkill = tasksDone.stream().map(Task::getSkill).distinct().filter(
							s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
									editionId));
					skillsDone = touchedSkill
							.filter(s -> !ownSkills.contains(s) && tasksDone.containsAll(s.getTasks()))
							.collect(Collectors.toSet());

					skillsWithTasks = taskRepository.findAll().stream().map(Task::getSkill)
							.collect(Collectors.toSet());

				} else {
					Path personPath = personPathPreference.get(0).getPath();
					List<Skill> touchedSkill = tasksDone.stream()
							.filter(t -> t.getPaths().contains(personPath))
							.map(Task::getSkill).distinct().filter(
									s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
											editionId))
							.toList();

					skillsDone = touchedSkill
							.stream().filter(x -> !ownSkills.contains(x) && tasksDone.containsAll(
									x.getTasks().stream().filter(y -> y.getPaths().contains(personPath))
											.toList()))
							.collect(Collectors.toSet());

					skillsWithTasks = taskRepository.findAll().stream()
							.filter(t -> t.getPaths().contains(personPath)).map(Task::getSkill)
							.collect(Collectors.toSet());
				}

				var emptySkills = skillRepository.findAll().stream().filter(x -> !skillsWithTasks.contains(x))
						.filter(
								s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
										editionId))
						.toList();

				emptySkills.forEach(x -> addCompletedEmptySkills(skillsDone, emptySkills, x));

				skillsDone.addAll(ownSkillsDone);

				skillsDoneCount = skillsDone.size();
			}

			completedSkillsPerCourse.put(courseId, skillsDoneCount);
		}
		return completedSkillsPerCourse;
	}

	/**
	 * Checks if the given empty skill can be considered complete. If so, adds it to skillsDone. An empty
	 * skill is complete if all its essential parents are complete.
	 *
	 * @param skillsDone  The list of completed skills
	 * @param emptySkills The list of empty skills
	 * @param empty       The empty skill that needs to be checked
	 */
	private void addCompletedEmptySkills(Set<Skill> skillsDone, List<Skill> emptySkills, Skill empty) {
		if (empty == null) {
			return;
		}
		if (skillsDone.contains(empty)) {
			return;
		}
		List<AbstractSkill> parents = empty.getParents().stream().filter(p -> {
			Skill sk = (Skill) p;
			return sk.isEssential();
		}).toList();

		List<AbstractSkill> notCompletedParents = parents.stream()
				.filter(x -> !skillsDone.contains((Skill) x)).toList();

		for (AbstractSkill parent : notCompletedParents) {
			if (emptySkills.contains((Skill) parent)) {
				addCompletedEmptySkills(skillsDone, emptySkills, (Skill) parent);
				if (!skillsDone.contains(parent)) {
					return;
				}
			} else {
				return;
			}
		}
		skillsDone.add(empty);

	}

	/**
	 * Gets the page for logging in (development/LDAP only).
	 *
	 * @return The page to load
	 */
	@GetMapping("auth/login")
	public String getLoginPage() {
		return "login";
	}

	/**
	 * Gets the page for the privacy statement.
	 *
	 * @return The page to load
	 */
	@GetMapping("privacy")
	public String getPrivacyPage() {
		return "privacy";
	}

}
