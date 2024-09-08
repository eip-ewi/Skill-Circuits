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

import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.HomeService;

@Controller
public class HomeController {

	private final EditionControllerApi editionApi;
	private final EditionRepository editionRepository;
	private final PersonRepository personRepository;
	private final HomeService homeService;

	@Autowired
	public HomeController(EditionControllerApi editionApi, EditionRepository editionRepository,
			PersonRepository personRepository, HomeService homeService) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		this.personRepository = personRepository;
		this.homeService = homeService;
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
		Set<Long> teacherIds = homeService.getTeacherIds(person);
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
		Map<Long, Long> courseToEditionMap = homeService.getCourseToEditionMap(courses);

		// Get number of completed skills for each course
		Map<Long, Integer> completedSkillsPerCourse = new HashMap<>();
		if (person != null) {
			SCPerson scperson = personRepository.findByIdOrThrow(person.getId());
			completedSkillsPerCourse = homeService.getCompletedSkillsPerCourse(scperson, courseToEditionMap);
		}

		// Get all course groups
		Map<String, List<CourseSummaryDTO>> courseGroups = homeService.getCourseGroups(
				person,
				courses,
				homeService.getActiveCourses(visibleOrManagedEditions, visible, teacherIds,
						courseToEditionMap),
				homeService.getOwnCourses(person, courseToEditionMap));

		// Add all attributes to the model
		for (Map.Entry<String, List<CourseSummaryDTO>> group : courseGroups.entrySet()) {
			model.addAttribute(group.getKey(), group.getValue());
		}
		model.addAttribute("editionPerCourse", courseToEditionMap);
		model.addAttribute("completedSkillsPerCourse", completedSkillsPerCourse);

		return "index";
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
