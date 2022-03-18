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

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private CourseControllerApi courseApi;

	private CourseService courseService;
	private AuthorisationService authorisationService;

	private ModuleRepository moduleRepository;

	@Autowired
	public HomeController(ModuleRepository moduleRepository, CourseService courseService,
			CourseControllerApi courseApi, AuthorisationService authorisationService) {
		this.moduleRepository = moduleRepository;
		this.courseService = courseService;
		this.courseApi = courseApi;
		this.authorisationService = authorisationService;
	}

	@Transactional
	@GetMapping("/")
	public String getHomePage(Model model) {
		List<CourseSummaryDTO> allCourses = courseApi.getAllCourses().collectList().block();

		allCourses = allCourses.stream()
				.filter(c -> authorisationService.canViewCourse(c.getId())
						|| courseService.hasAtLeastOneEditionVisibleToStudents(c.getId()))
				.toList();

		model.addAttribute("courses", allCourses);

		// Temporarily put all modules on the home page
		List<SCModule> allModules = moduleRepository.findAll();
		model.addAttribute("moduleIds", allModules.stream().map(SCModule::getId).toList());
		model.addAttribute("moduleNames",
				allModules.stream().collect(Collectors.toMap(SCModule::getId, SCModule::getName)));

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

}
