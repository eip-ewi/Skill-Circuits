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
import nl.tudelft.skills.service.SkillService;
import nl.tudelft.skills.service.TaskCompletionService;

@Controller
public class HomeController {

	private final EditionControllerApi editionApi;
	private final PersonControllerApi personApi;

	private final EditionRepository editionRepository;
	private final PersonRepository personRepository;
	private final CourseService courseService;
	private final SkillService skillService;
	private final TaskCompletionService taskCompletionService;
	private final AuthorisationService authorisationService;

	@Autowired
	public HomeController(EditionControllerApi editionApi, PersonControllerApi personApi,
			EditionRepository editionRepository, PersonRepository personRepository,
			CourseService courseService,
			SkillService skillService, TaskCompletionService taskCompletionService,
			AuthorisationService authorisationService) {
		this.editionApi = editionApi;
		this.personApi = personApi;
		this.editionRepository = editionRepository;
		this.personRepository = personRepository;
		this.courseService = courseService;
		this.skillService = skillService;
		this.taskCompletionService = taskCompletionService;
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

		// All editions the user can view
		List<EditionDetailsDTO> visibleOrManagedEditions = visibleOrManagedIds.isEmpty() ? new ArrayList<>()
				: editionApi.getEditionsById(visibleOrManagedIds.stream().toList()).collectList().block();

		// All courses the user can view
		List<CourseSummaryDTO> courses = visibleOrManagedEditions.stream().map(EditionDetailsDTO::getCourse)
				.distinct().toList();

		// Map each course to its default edition
		Map<Long, Long> courseToEditionMap = getCourseToEditionMap(courses);

		// Get number of completed skills for each course
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		if (person != null) {
			SCPerson scperson = personRepository.findByIdOrThrow(person.getId());
			completedSkillsPerCourse = getCompletedSkillsPerCourse(scperson, courseToEditionMap);
		}

		// Get all course groups
		Map<String, List<CourseSummaryDTO>> courseGroups = getCourseGroups(
				person,
				courses,
				getActiveCourses(visibleOrManagedEditions, visible, teacherIds, courseToEditionMap),
				getOwnCourses(person, courseToEditionMap));

		// Add all attributes to the model
		for (Map.Entry<String, List<CourseSummaryDTO>> group : courseGroups.entrySet()) {
			model.addAttribute(group.getKey(), group.getValue());
		}
		model.addAttribute("editionPerCourse", courseToEditionMap);
		model.addAttribute("completedSkillsPerCourse", completedSkillsPerCourse);

		return "index";
	}

	/**
	 * Creates a map for courses to their default edition ids. If there is no valid edition for the course,
	 * the id is null. The pair is still added to the map, so that the key set corresponds to all visible or
	 * managed courses.
	 *
	 * @param  courses The course summaries.
	 * @return         Map of courses to the default edition ids (latest edition or last student edition).
	 */
	public Map<Long, Long> getCourseToEditionMap(List<CourseSummaryDTO> courses) {
		Map<Long, Long> courseToEditionMap = new HashMap<>();
		for (CourseSummaryDTO course : courses) {
			Long editionId = courseService.getDefaultHomepageEditionCourse(course.getId());
			courseToEditionMap.put(course.getId(), editionId);
		}
		return courseToEditionMap;
	}

	/**
	 * Group courses into own (has role in the edition), available (no role in edition), and both of these
	 * into the courses which have a currently active edition/do not have one. Also groups the courses managed
	 * by the user (courses which the user can see).
	 *
	 * @param  person        The logged-in person, if it exists, otherwise null.
	 * @param  courses       The course summaries to group, mapped to if they
	 * @param  activeCourses The ids of courses which should be considered as "active".
	 * @param  ownCourses    The ids of courses which should be considered as "owned" by the user (appear
	 *                       under "your courses").
	 * @return               A map of the model attribute strings, mapped to the list of the courses
	 *                       corresponding to that group.
	 */
	public Map<String, List<CourseSummaryDTO>> getCourseGroups(Person person, List<CourseSummaryDTO> courses,
			Set<Long> activeCourses, Set<Long> ownCourses) {
		List<CourseSummaryDTO> availableActive = new ArrayList<>();
		List<CourseSummaryDTO> availableFinished = new ArrayList<>();
		List<CourseSummaryDTO> ownActive = new ArrayList<>();
		List<CourseSummaryDTO> ownFinished = new ArrayList<>();
		List<CourseSummaryDTO> managed = new ArrayList<>();

		for (CourseSummaryDTO course : courses) {
			boolean hasActiveEdition = activeCourses.contains(course.getId());
			boolean isOwnEdition = ownCourses.contains(course.getId());

			if (person != null && authorisationService.canViewCourse(course.getId())) {
				// managed: The user can see the course, so manages it
				managed.add(course);
			} else if (person != null && hasActiveEdition && isOwnEdition) {
				// ownActive: The user has completed at least one skill, and the course has an active edition
				ownActive.add(course);
			} else if (person != null && isOwnEdition) {
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
	 * Returns the ids of the courses in which the default edition is currently active.
	 *
	 * @param  editions           The editions the user can view.
	 * @param  visible            The ids of the visible editions.
	 * @param  teacherIds         The ids of the editions in which the user is a teacher.
	 * @param  courseToEditionMap Map of courses to the default edition ids (latest edition or last student
	 *                            edition).
	 * @return                    Ids of courses in which there is an active edition, following the above
	 *                            criteria.
	 */
	public Set<Long> getActiveCourses(List<EditionDetailsDTO> editions, Set<Long> visible,
			Set<Long> teacherIds,
			Map<Long, Long> courseToEditionMap) {
		// Map each edition id to its edition
		Map<Long, EditionDetailsDTO> editionMap = editions.stream()
				.collect(Collectors.toMap(EditionDetailsDTO::getId,
						Function.identity()));

		Set<Long> activeCourses = new HashSet<>();
		for (Map.Entry<Long, Long> courseToEdition : courseToEditionMap.entrySet()) {
			Long courseId = courseToEdition.getKey();
			Long editionId = courseToEdition.getValue();

			// Skip if the user is neither a teacher for this edition, nor it is visible
			if (editionId == null || (!visible.contains(editionId) && !teacherIds.contains(editionId))) {
				continue;
			}

			// Check if edition is currently active, if so, add id to the Set
			EditionDetailsDTO edition = editionMap.get(editionId);
			boolean afterStartIncl = edition.getStartDate().isBefore(LocalDateTime.now()) ||
					edition.getStartDate().equals(LocalDateTime.now());
			boolean beforeEndIncl = edition.getEndDate().isAfter(LocalDateTime.now()) ||
					edition.getEndDate().equals(LocalDateTime.now());
			if (afterStartIncl && beforeEndIncl) {
				activeCourses.add(courseId);
			}
		}
		return activeCourses;
	}

	/**
	 * Returns the ids of the courses in which the user has a role assigned in the default edition on the
	 * homepage.
	 *
	 * @param  person             The logged-in person, if it exists, otherwise null.
	 * @param  courseToEditionMap Map of courses to the default edition ids.
	 * @return                    All course ids in which the user has a role in the default edition.
	 */
	public Set<Long> getOwnCourses(Person person, Map<Long, Long> courseToEditionMap) {
		Set<Long> ownCourses = new HashSet<>();

		// User has no roles if they are not logged in
		if (person == null) {
			return ownCourses;
		}

		for (Long course : courseToEditionMap.keySet()) {
			RoleDetailsDTO.TypeEnum role = authorisationService
					.getRoleInEdition(courseToEditionMap.get(course));

			// If the user has any role in the edition, add it to own courses
			if (role != null) {
				ownCourses.add(course);
			}
		}
		return ownCourses;
	}

	/**
	 * Returns a map of courses and the number of skills completed in a respective edition in that course.
	 *
	 * @param  scperson           person
	 * @param  courseToEditionMap Map of courses to the default edition ids.
	 * @return                    Map containing number of completed skills per course.
	 */
	public Map<Long, Integer> getCompletedSkillsPerCourse(SCPerson scperson,
			Map<Long, Long> courseToEditionMap) {
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();

		for (Map.Entry<Long, Long> courseToEdition : courseToEditionMap.entrySet()) {
			Long courseId = courseToEdition.getKey();
			Long editionId = courseToEdition.getValue();

			int skillsDoneCount = 0;

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
