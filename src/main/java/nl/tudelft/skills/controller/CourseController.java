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

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("course")
public class CourseController {

	private CourseRepository courseRepository;
	private CourseService courseService;
	private CourseControllerApi courseApi;
	private EditionRepository editionRepository;

	@Autowired
	public CourseController(CourseRepository courseRepository, CourseService courseService,
			CourseControllerApi courseApi, EditionRepository editionRepository) {
		this.courseRepository = courseRepository;
		this.courseService = courseService;
		this.courseApi = courseApi;
		this.editionRepository = editionRepository;
	}

	/**
	 * Gets the page for a single course. This page contains a list of all editions in the course. Depending
	 * on the user, not all are visible.
	 *
	 * @param  id    The id of the course
	 * @param  model The model to add data to
	 * @return       The page to load
	 */
	@GetMapping("{id}")
	@PreAuthorize("@authorisationService.canViewCourse(#id)")
	public String getCoursePage(@PathVariable Long id, Model model) {
		model.addAttribute("course", courseService.getCourseView(id));

		return "course/view";
	}

	/**
	 * Gets the editions of a course.
	 *
	 * @param  id The id of the course
	 * @return    The list of course editions
	 */
	@GetMapping("{id}/editions")
	@PreAuthorize("@authorisationService.canGetEditionsOfCourse(#id)")
	public @ResponseBody List<EditionSummaryDTO> getEditionsOfCourse(@PathVariable Long id) {
		return courseApi.getCourseById(id).block().getEditions().stream()
				.filter(e -> editionRepository.existsById(e.getId())).toList();
	}

}
