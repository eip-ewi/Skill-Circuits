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
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
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

	@Autowired
	public HomeController(EditionControllerApi editionApi, RoleControllerApi roleApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository,
			PersonRepository personRepository, EditionService editionService, CourseService courseService) {
		this.editionApi = editionApi;
		this.roleApi = roleApi;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
		this.editionService = editionService;
		this.courseService = courseService;
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
						.getRolesById(editions.stream().map(EditionDetailsDTO::getId).toList(),
								List.of(person.getId()))
						.collectList().block().stream()
						.filter(r -> r.getType() == RoleDetailsDTO.TypeEnum.TEACHER)
						.map(r -> r.getEdition().getId()).collect(Collectors.toSet());

		List<CourseSummaryDTO> courses = editions.stream()
				.filter(e -> visible.contains(e.getId()) || teacherIds.contains(e.getId()))
				.map(EditionDetailsDTO::getCourse).distinct().toList();

		// Gets number of completed skills for each course
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		boolean isAnySkillCompleted = false;
		if (person != null) {
			SCPerson scperson = personRepository.findByIdOrThrow(person.getId());

			completedSkillsPerCourse = getCompletedSkillsPerCourse(courses, scperson);
			isAnySkillCompleted = completedSkillsPerCourse.entrySet().stream()
					.anyMatch(e -> e.getValue() > 0);
		}

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

			int skillsDone = 0;
			Long editionId = courseService.getLastStudentEditionForCourseOrLast(courseId);

			if (editionId != null) {
				SCEdition edition = editionService.getOrCreateSCEdition(editionId);
				skillsDone = (int) edition.getModules().stream()
						.flatMap(m -> m.getSubmodules().stream())
						.flatMap(s -> s.getSkills().stream())
						.filter(s -> s.getTasks().size() > 0
								&& scperson.getTasksCompleted().containsAll(s.getTasks()))
						.count();
			}

			completedSkillsPerCourse.put(courseId, skillsDone);
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

}
