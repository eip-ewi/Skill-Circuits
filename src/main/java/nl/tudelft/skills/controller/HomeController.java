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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.EditionService;
import nl.tudelft.skills.service.SkillService;
import nl.tudelft.skills.service.TaskCompletionService;

@Controller
public class HomeController {

	private final EditionControllerApi editionApi;
	private final PersonControllerApi personApi;

	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;
	private final PersonRepository personRepository;

	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;

	private final EditionService editionService;
	private final CourseService courseService;
	private final SkillService skillService;
	private final TaskCompletionService taskCompletionService;
	private final AuthorisationService authorisationService;

	private final PathPreferenceRepository pathPreferenceRepository;

	@Autowired
	public HomeController(EditionControllerApi editionApi, PersonControllerApi personApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository,
			PersonRepository personRepository, SkillRepository skillRepository, TaskRepository taskRepository,
			EditionService editionService,
			CourseService courseService,
			SkillService skillService, TaskCompletionService taskCompletionService,
			PathPreferenceRepository pathPreferenceRepository,
			AuthorisationService authorisationService) {
		this.editionApi = editionApi;
		this.personApi = personApi;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
		this.editionService = editionService;
		this.courseService = courseService;
		this.skillService = skillService;
		this.taskCompletionService = taskCompletionService;
		this.pathPreferenceRepository = pathPreferenceRepository;
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
		Set<Long> visible = editionRepository.findByIsVisible(true).stream().map(SCEdition::getId)
				.collect(Collectors.toSet());

		Set<Long> teacherIds = getTeacherIds(person);

		Set<Long> visibleOrManagedIds = new HashSet<>();
		visibleOrManagedIds.addAll(visible);
		visibleOrManagedIds.addAll(teacherIds);

		List<EditionDetailsDTO> visibleOrManagedEditions = visibleOrManagedIds.isEmpty() ? new ArrayList<>()
				: editionApi.getEditionsById(visibleOrManagedIds.stream().toList()).collectList().block();

		// All visible courses or courses for which the user is teacher in an edition
		List<CourseSummaryDTO> courses = visibleOrManagedEditions.stream().map(EditionDetailsDTO::getCourse)
				.distinct().toList();

		// Map the courses to their corresponding "default" edition (latest edition or last student edition)
		Map<Long, Long> courseToEditionMap = getCourseToEditionMap(courses);

		// Get number of completed skills and if a task is completed for each course
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		Map<Long, Boolean> completedTaskInCourse = new HashMap<>();
		if (person != null) {
			SCPerson scperson = personRepository.findByIdOrThrow(person.getId());

			completedSkillsPerCourse = getCompletedSkillsPerCourse(courses, scperson, courseToEditionMap);
			completedTaskInCourse = getCompletedTaskInCourse(courses, scperson, courseToEditionMap);
		}

		// Check if any skill has been completed
		boolean isAnySkillCompleted = completedSkillsPerCourse.entrySet().stream()
				.anyMatch(e -> e.getValue() > 0);

		model.addAttribute("completedSkillsPerCourse", completedSkillsPerCourse);
		model.addAttribute("isAnySkillCompleted", isAnySkillCompleted);

		// Get courses for which the latest student edition/last edition is currently active
		Map<Long, EditionDetailsDTO> editionMap = visibleOrManagedEditions.stream()
				.collect(Collectors.toMap(EditionDetailsDTO::getId,
						Function.identity()));
		Set<Long> activeCourses = getActiveCourses(courses, editionMap, visible, teacherIds,
				courseToEditionMap);

		// Get course groups and add them to the model
		Map<String, List<CourseSummaryDTO>> courseGroups = getCourseGroups(person, courses, activeCourses,
				completedTaskInCourse);
		for (Map.Entry<String, List<CourseSummaryDTO>> group : courseGroups.entrySet()) {
			model.addAttribute(group.getKey(), group.getValue());
		}

		return "index";
	}

	/**
	 * Creates a map for courses to their default edition ids (latest edition or last student edition). If
	 * there is no published edition for the course, i.e., such an id would be null, the course is not added
	 * to the map.
	 *
	 * @param  courses The course summaries.
	 * @return         Map of courses to the default edition ids (latest edition or last student edition).
	 */
	public Map<Long, Long> getCourseToEditionMap(List<CourseSummaryDTO> courses) {
		Map<Long, Long> courseToEditionMap = new HashMap<>();
		for (CourseSummaryDTO course : courses) {
			Long editionId = courseService.getLastStudentEditionForCourseOrLast(course.getId());

			if (editionId != null) {
				courseToEditionMap.put(course.getId(), editionId);
			}
		}
		return courseToEditionMap;
	}

	/**
	 * Group courses into own (> 0 skills completed), available (0 skills completed), and both of these into
	 * the courses which have a currently active edition/do not have one. Also groups the courses managed by
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
			boolean completedTask = completedTaskInCourse.getOrDefault(course.getId(), false);

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
	 * Returns the ids of the editions in which the user is a teacher or a head TA.
	 *
	 * @param  person The logged-in person, if it exists, otherwise null.
	 * @return        The ids of the editions in which the person is a teacher. Returns an empty set if the
	 *                user is not logged-in.
	 */
	public Set<Long> getTeacherIds(Person person) {
		if (person == null) {
			return Set.of();
		}

		// Retrieve the roles of the logged-in user
		List<RoleEditionDetailsDTO> roles = personApi.getRolesForPerson(person.getId()).collectList().block();

		// The user needs to be at least a head TA to manage the edition
		return roles.stream()
				.filter(role -> role.getType().equals(RoleEditionDetailsDTO.TypeEnum.ADMIN)
						|| role.getType().equals(RoleEditionDetailsDTO.TypeEnum.TEACHER)
						|| role.getType().equals(RoleEditionDetailsDTO.TypeEnum.HEAD_TA))
				.map(role -> role.getId().getEditionId())
				.collect(Collectors.toSet());
	}

	/**
	 * Returns the ids of the courses in which either: 1. if there is at least one student edition, the latest
	 * edition the user was a student in, is active 2. the user is not a student in any edition, and the
	 * latest edition is active
	 *
	 * @param  courses            List of the course summaries.
	 * @param  editions           Map of edition ids to the corresponding edition.
	 * @param  visible            The ids of the visible editions.
	 * @param  teacherIds         The ids of the editions in which the user is a teacher.
	 * @param  courseToEditionMap Map of courses to the default edition ids (latest edition or last student
	 *                            edition).
	 * @return                    Ids of courses in which there is an active edition, following the above
	 *                            criteria.
	 */
	public Set<Long> getActiveCourses(List<CourseSummaryDTO> courses, Map<Long, EditionDetailsDTO> editions,
			Set<Long> visible, Set<Long> teacherIds, Map<Long, Long> courseToEditionMap) {
		Set<Long> activeCourses = new HashSet<>();

		for (CourseSummaryDTO course : courses) {
			// Get the id of either the latest student edition or the last edition, if no student edition exists
			Long editionId = courseToEditionMap.get(course.getId());

			// Skip if the user is neither a teacher for this edition, nor it is visible
			if (!visible.contains(editionId) && !teacherIds.contains(editionId)) {
				continue;
			}

			// Check if edition is currently active, if so, add id to the Set
			EditionDetailsDTO edition = editions.get(editionId);
			boolean afterStartIncl = edition.getStartDate().isBefore(LocalDateTime.now()) ||
					edition.getStartDate().equals(LocalDateTime.now());
			boolean beforeEndIncl = edition.getEndDate().isAfter(LocalDateTime.now()) ||
					edition.getEndDate().equals(LocalDateTime.now());
			if (afterStartIncl && beforeEndIncl) {
				activeCourses.add(course.getId());
			}
		}

		return activeCourses;
	}

	/**
	 * Returns a map of courses and the number of skills completed in a respective edition in that course.
	 *
	 * @param  courses            List of courses
	 * @param  scperson           person
	 * @param  courseToEditionMap Map of courses to the default edition ids (latest edition or last student
	 *                            edition).
	 * @return                    Map containing number of completed skills per course.
	 */
	public Map<Long, Integer> getCompletedSkillsPerCourse(List<CourseSummaryDTO> courses, SCPerson scperson,
			Map<Long, Long> courseToEditionMap) {
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();

		for (var course : courses) {
			Long courseId = course.getId();
			int skillsDoneCount = 0;
			Long editionId = courseToEditionMap.get(courseId);

			if (editionId != null) {
				Set<Skill> ownSkillsWithTask = skillService.getOwnSkillsWithTask(scperson, editionId);
				var personPathPreference = scperson.getPathPreferences().stream()
						.filter(p -> Objects.equals(p.getEdition().getId(), editionId)).toList();

				// All completed skills
				Set<Skill> skillsDone = taskCompletionService.determineSkillsDone(ownSkillsWithTask, scperson,
						editionId,
						personPathPreference);

				// All empty skills
				Set<Skill> emptySkills = taskCompletionService.determineEmptySkills(ownSkillsWithTask,
						personPathPreference,
						scperson, editionId);

				// Collect completed empty skills
				emptySkills.forEach(
						x -> taskCompletionService.addCompletedEmptySkills(skillsDone, emptySkills, x));

				skillsDoneCount = skillsDone.size();
			}

			completedSkillsPerCourse.put(courseId, skillsDoneCount);
		}
		return completedSkillsPerCourse;
	}

	/**
	 * Returns a map of courses and whether a task has been completed in a respective edition in that course.
	 *
	 * @param  courses            List of courses
	 * @param  scperson           person
	 * @param  courseToEditionMap Map of courses to the default edition ids (latest edition or last student
	 *                            edition).
	 * @return                    Map containing whether the person has a completed a task in each course.
	 */
	public Map<Long, Boolean> getCompletedTaskInCourse(List<CourseSummaryDTO> courses, SCPerson scperson,
			Map<Long, Long> courseToEditionMap) {
		// TODO currently, this will not consider paths. Is this correct?

		Map<Long, Boolean> completedTaskInCourse = new HashMap<>();
		for (CourseSummaryDTO course : courses) {
			Long courseId = course.getId();

			boolean completedTask = false;
			Long editionId = courseToEditionMap.get(courseId);

			if (editionId != null) {
				List<Task> tasksDone = scperson.getTaskCompletions().stream().map(TaskCompletion::getTask)
						.toList();

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
