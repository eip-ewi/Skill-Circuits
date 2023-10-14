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

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.EditionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;

	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;
	private final PersonRepository personRepository;

	private final EditionService editionService;
	private final CourseService courseService;
	private final AuthorisationService authorisationService;

	@Autowired
	public HomeController(EditionControllerApi editionApi, RoleControllerApi roleApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository,
			PersonRepository personRepository, EditionService editionService, CourseService courseService,
			AuthorisationService authorisationService) {
		this.editionApi = editionApi;
		this.roleApi = roleApi;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.editionService = editionService;
		this.courseService = courseService;
		this.authorisationService = authorisationService;
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
		// Get all available editions
		List<EditionDetailsDTO> editions = editionApi.getAllEditions().collectList().block();

		// Filter visible editions in SC
		Set<Long> visible = editionRepository
				.findAllById(editions.stream().map(EditionDetailsDTO::getId).toList()).stream()
				.filter(SCEdition::isVisible).map(SCEdition::getId).collect(Collectors.toSet());

		// Get ids of editions for which the user is a teacher
		Set<Long> teacherIds = getTeacherIds(person, editions);

		// All visible courses or courses for which the user is teacher in an edition
		List<CourseSummaryDTO> courses = editions.stream()
				.filter(e -> visible.contains(e.getId()) || teacherIds.contains(e.getId()))
				.map(EditionDetailsDTO::getCourse).distinct().toList();

		// Get number of completed skills and if a task is completed for each course
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		Map<Long, Boolean> completedTaskInCourse = new HashMap<>();
		if (person != null) {
			SCPerson scperson = personRepository.findByIdOrThrow(person.getId());

			completedSkillsPerCourse = getCompletedSkillsPerCourse(courses, scperson);
			completedTaskInCourse = getCompletedTaskInCourse(courses, scperson);
		}

		// Check if any skill has been completed
		boolean isAnySkillCompleted = completedSkillsPerCourse.entrySet().stream()
				.anyMatch(e -> e.getValue() > 0);

		model.addAttribute("completedSkillsPerCourse", completedSkillsPerCourse);
		model.addAttribute("isAnySkillCompleted", isAnySkillCompleted);

		// Get courses for which the latest student edition/last edition is currently active
		Set<Long> activeCourses = getActiveCourses(editions, visible, teacherIds);

		// Get course groups and add them to the model
		Map<String, List<CourseSummaryDTO>> courseGroups = getCourseGroups(person, courses, activeCourses,
				completedTaskInCourse);
		for (Map.Entry<String, List<CourseSummaryDTO>> group : courseGroups.entrySet()) {
			model.addAttribute(group.getKey(), group.getValue());
		}

		return "index";
	}

	/**
	 * Group courses into own (> 0 skills completed), available (0 skills completed), and both of these into
	 * the courses which have a currently active edition/do not have one Also groups the courses managed by
	 * the user (courses which the user can see)
	 *
	 * @param  person                The logged-in person, if it exists, otherwise null.
	 * @param  courses               The list of the course summaries to group.
	 * @param  activeCourses         The ids of courses which should be considered as "active".
	 * @param  completedTaskInCourse The ids of courses in which the user has completed at least one task in
	 *                               the latest student edition/last edition.
	 * @return                       A map of the model attribute strings, mapped to the list of the courses
	 *                               corresponding to that group.
	 */
	public Map<String, List<CourseSummaryDTO>> getCourseGroups(Person person, List<CourseSummaryDTO> courses,
			Set<Long> activeCourses,
			Map<Long, Boolean> completedTaskInCourse) {
		List<CourseSummaryDTO> availableActive = new ArrayList<>();
		List<CourseSummaryDTO> availableFinished = new ArrayList<>();
		List<CourseSummaryDTO> ownActive = new ArrayList<>();
		List<CourseSummaryDTO> ownFinished = new ArrayList<>();
		List<CourseSummaryDTO> managed = new ArrayList<>();

		for (CourseSummaryDTO course : courses) {
			boolean hasActiveEdition = activeCourses.contains(course.getId());
			Boolean completedTask = completedTaskInCourse.get(course.getId());

			if (person != null && authorisationService.canViewCourse(course.getId())) {
				// managed: The user can see the course, so manages it
				managed.add(course);
			} else if (person != null && completedTask && hasActiveEdition) {
				// ownActive: The user has completed at least one skill, and the course has an active edition
				ownActive.add(course);
			} else if (person != null && completedTask) {
				// ownFinished: The user has completed at least one skill, and the course does not have
				// an active edition
				ownFinished.add(course);
			} else if (hasActiveEdition) {
				// availableActive: Course has an active edition, and the user is either not logged in
				// or does not have any skills completed in it
				availableActive.add(course);
			} else {
				// availableFinished: Course does not have an active edition, and the user is either not logged in
				// or does not have any skills completed in it
				availableFinished.add(course);
			}
		}

		Map<String, List<CourseSummaryDTO>> courseGroups = new HashMap<>();
		courseGroups.put("availableActive", availableActive);
		courseGroups.put("availableFinished", availableFinished);
		courseGroups.put("ownActive", ownActive);
		courseGroups.put("ownFinished", ownFinished);
		courseGroups.put("managed", managed);

		return courseGroups;
	}

	/**
	 * Returns the ids of the editions in which the user is a teacher.
	 *
	 * @param  person   The logged-in person, if it exists, otherwise null.
	 * @param  editions The editions.
	 * @return          The ids of the editions in which the person is a teacher. Returns an empty set if the
	 *                  user is not logged-in.
	 */
	public Set<Long> getTeacherIds(Person person, List<EditionDetailsDTO> editions) {
		if (person == null) {
			return Set.of();
		}

		return roleApi
				.getRolesById(
						editions.stream().map(EditionDetailsDTO::getId).collect(Collectors.toSet()),
						Set.of(person.getId()))
				.collectList().block().stream()
				.filter(r -> r.getType() == RoleDetailsDTO.TypeEnum.TEACHER)
				.map(r -> r.getEdition().getId()).collect(Collectors.toSet());
	}

	/**
	 * Returns the ids of the courses in which either: 1. there is an active edition the user was a student in
	 * 2. the user is not a student in any edition, and any of the editions is active
	 *
	 * @param  editions   The editions.
	 * @param  visible    The ids of the visible editions.
	 * @param  teacherIds The ids of the editions in which the user is a teacher.
	 * @return            Ids of courses in which there is an active edition, following the above criteria.
	 */
	public Set<Long> getActiveCourses(List<EditionDetailsDTO> editions, Set<Long> visible,
			Set<Long> teacherIds) {
		// TODO change to take student editions into account
		Set<Long> activeCourses = new HashSet<>();
		for (EditionDetailsDTO edition : editions) {
			// Skip if the user is neither a teacher for this edition, nor it is visible
			if (!visible.contains(edition.getId()) && !teacherIds.contains(edition.getId())) {
				continue;
			}

			// Check if edition is currently active, if so, add id to the Set
			boolean afterStartIncl = edition.getStartDate().isBefore(LocalDateTime.now()) ||
					edition.getStartDate().equals(LocalDateTime.now());
			boolean beforeEndIncl = edition.getEndDate().isAfter(LocalDateTime.now()) ||
					edition.getEndDate().equals(LocalDateTime.now());
			if (afterStartIncl && beforeEndIncl) {
				activeCourses.add(edition.getCourse().getId());
			}
		}

		return activeCourses;
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

			int skillsDone = 0;
			Long editionId = courseService.getLastStudentEditionForCourseOrLast(courseId);

			if (editionId != null) {
				List<Task> tasksDone = scperson.getTaskCompletions().stream().map(TaskCompletion::getTask)
						.toList();

				skillsDone = (int) tasksDone.stream().map(Task::getSkill).distinct()
						.filter(s -> tasksDone.containsAll(s.getTasks())).count();
			}

			completedSkillsPerCourse.put(courseId, skillsDone);
		}
		return completedSkillsPerCourse;
	}

	/**
	 * Returns a map of courses and whether a task has been completed in a respective edition in that course.
	 *
	 * @param  courses  List of courses
	 * @param  scperson person
	 * @return          Map containing whether the person has a completed a task in each course.
	 */
	public Map<Long, Boolean> getCompletedTaskInCourse(List<CourseSummaryDTO> courses, SCPerson scperson) {
		Map<Long, Boolean> completedTaskInCourse = new HashMap<>();
		for (CourseSummaryDTO course : courses) {
			Long courseId = course.getId();

			boolean completedTask = false;
			Long editionId = courseService.getLastStudentEditionForCourseOrLast(courseId);

			if (editionId != null) {
				List<Task> tasksDone = scperson.getTaskCompletions().stream().map(TaskCompletion::getTask)
						.toList();

				// TODO task completed regardless of active path, or should path be considered?
				completedTask = tasksDone.stream()
						.map((Task t) -> t.getSkill().getSubmodule().getModule().getEdition().getId())
						.anyMatch((Long id) -> Objects.equals(id, editionId));
			}

			completedTaskInCourse.put(courseId, completedTask);
		}
		return completedTaskInCourse;
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
