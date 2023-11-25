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

		for (var course : courses) {
			Long courseId = course.getId();
			int skillsDoneCount = 0;
			Long editionId = courseService.getLastStudentEditionForCourseOrLast(courseId);

			if (editionId != null) {
				Set<Skill> ownSkillsWithTask = getOwnSkillsWithTask(scperson, editionId);
				var personPathPreference = scperson.getPathPreferences().stream()
						.filter(p -> Objects.equals(p.getEdition().getId(), editionId)).toList();

				// All completed skills
				Set<Skill> skillsDone = determineSkillsDone(ownSkillsWithTask, scperson, editionId,
						personPathPreference);

				// All empty skills
				Set<Skill> emptySkills = determineEmptySkills(ownSkillsWithTask, personPathPreference,
						scperson, editionId);

				// Collect completed empty skills
				emptySkills.forEach(x -> addCompletedEmptySkills(skillsDone, emptySkills, x));

				skillsDoneCount = skillsDone.size();
			}

			completedSkillsPerCourse.put(courseId, skillsDoneCount);
		}
		return completedSkillsPerCourse;
	}

	/**
	 * Collects the customized skills that have at least one task in them
	 *
	 * @param  scperson  The person having the customized skills
	 * @param  editionId The edition
	 * @return           The set of customized skills
	 */
	private Set<Skill> getOwnSkillsWithTask(SCPerson scperson, long editionId) {
		return scperson.getTasksAdded().stream().map(Task::getSkill)
				.filter(s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
						editionId))
				.collect(Collectors.toSet());
	}

	/**
	 * Collects the completed tasks in an edition
	 *
	 * @param  scperson  The person completing the tasks
	 * @param  editionId The edition
	 * @return           The set of completed tasks
	 */
	private Set<Task> getTasksDone(SCPerson scperson, long editionId) {
		return scperson.getTaskCompletions().stream().map(TaskCompletion::getTask)
				.filter(s -> Objects.equals(
						s.getSkill().getSubmodule().getModule().getEdition().getId(), editionId))
				.collect(Collectors.toSet());
	}

	/**
	 * Collects the completed skills based on the given path preference and customized skills
	 *
	 * @param  ownSkillsWithTask    All the customized non-empty skills in current edition
	 * @param  scPerson             The person completing skills
	 * @param  editionId            The edition
	 * @param  personPathPreference The chosen path for the current edition
	 * @return                      The set of completed skills
	 */
	private Set<Skill> determineSkillsDone(Set<Skill> ownSkillsWithTask, SCPerson scPerson, long editionId,
			List<PathPreference> personPathPreference) {
		Set<Task> tasksDone = getTasksDone(scPerson, editionId);

		// Completed skills from the customized skills
		Set<Skill> ownSkillsDone = ownSkillsWithTask.stream()
				.filter(s -> tasksDone.containsAll(scPerson.getTasksAdded().stream()
						.filter(t -> t.getSkill().equals(s)).toList()))
				.collect(Collectors.toSet());

		// Completed non-customized skills based on chosen path
		Set<Skill> skillsDone;
		if (personPathPreference.isEmpty() || personPathPreference.get(0).getPath() == null) {
			skillsDone = tasksDone.stream()
					.map(Task::getSkill)
					.filter(s -> !ownSkillsWithTask.contains(s) && tasksDone.containsAll(s.getTasks()))
					.collect(Collectors.toSet());
		} else {
			Path personPath = personPathPreference.get(0).getPath();
			skillsDone = tasksDone.stream()
					.filter(t -> t.getPaths().contains(personPath))
					.map(Task::getSkill)
					.filter(x -> !ownSkillsWithTask.contains(x) && tasksDone.containsAll(x.getTasks()
							.stream().filter(y -> y.getPaths().contains(personPath)).toList()))
					.collect(Collectors.toSet());
		}
		skillsDone.addAll(ownSkillsDone);

		return skillsDone;
	}

	/**
	 * Collects the empty skills based on path preferences and customized skills
	 *
	 * @param  ownSkillsWithTask    All the customized non-empty skills in current edition
	 * @param  personPathPreference The chosen path for the current edition
	 * @param  scPerson             The person completing skills
	 * @param  editionId            The edition
	 * @return                      The set of empty skills
	 */
	private Set<Skill> determineEmptySkills(Set<Skill> ownSkillsWithTask,
			List<PathPreference> personPathPreference,
			SCPerson scPerson, long editionId) {
		Set<Skill> ownSkills = scPerson.getSkillsModified().stream().filter(s -> Objects.equals(
				s.getSubmodule().getModule().getEdition().getId(), editionId))
				.collect(Collectors.toSet());

		// Customized empty skills
		Set<Skill> ownEmptySkills = ownSkills.stream()
				.filter(s -> !ownSkillsWithTask.contains(s)).collect(Collectors.toSet());

		// Non-customized empty skills based on chosen path
		Set<Skill> emptySkills;
		Set<Skill> notOwnSkills = skillRepository.findAll().stream()
				.filter(s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
						editionId))
				.filter(s -> !ownSkills.contains(s)).collect(Collectors.toSet());

		if (personPathPreference.isEmpty() || personPathPreference.get(0).getPath() == null) {
			emptySkills = notOwnSkills.stream().filter(s -> s.getTasks().isEmpty())
					.collect(Collectors.toSet());
		} else {
			Path personPath = personPathPreference.get(0).getPath();
			emptySkills = notOwnSkills.stream().filter(s -> s.getTasks().stream()
					.filter(t -> t.getPaths().contains(personPath)).collect(Collectors.toSet()).isEmpty())
					.collect(Collectors.toSet());
		}
		emptySkills.addAll(ownEmptySkills);
		return emptySkills;
	}

	/**
	 * Checks if the given empty skill can be considered complete. If so, adds it to skillsDone. An empty
	 * skill is complete if all its essential parents are complete.
	 *
	 * @param skillsDone  The list of completed skills
	 * @param emptySkills The list of empty skills
	 * @param empty       The empty skill that needs to be checked
	 */
	private void addCompletedEmptySkills(Set<Skill> skillsDone, Set<Skill> emptySkills, Skill empty) {
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
			if (!emptySkills.contains((Skill) parent)) {
				return;
			}
			addCompletedEmptySkills(skillsDone, emptySkills, (Skill) parent);
			if (!skillsDone.contains(parent)) {
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
