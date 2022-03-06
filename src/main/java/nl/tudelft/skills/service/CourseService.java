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
package nl.tudelft.skills.service;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.course.CourseLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.course.CourseLevelEditionViewDTO;
import nl.tudelft.skills.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

	private CourseControllerApi courseApi;
	private CourseRepository courseRepository;

	@Autowired
	public CourseService(CourseControllerApi courseApi, CourseRepository courseRepository) {
		this.courseApi = courseApi;
		this.courseRepository = courseRepository;
	}

	/**
	 * Return CourseViewDto for course with id, including course name from Labrador db and SCEditions.
	 *
	 * @param  id Course id.
	 * @return    CourseViewDTO for course with id.
	 */
	public CourseLevelCourseViewDTO getCourseView(Long id) {
		CourseDetailsDTO course = courseApi.getCourseById(id).block();

		CourseLevelCourseViewDTO view = View.convert(courseRepository.findByIdOrThrow(id),
				CourseLevelCourseViewDTO.class);

		view.setName(course.getName());

		view.setCode(course.getCode());

		view.setEditions(course.getEditions().stream()
				.map(e -> new CourseLevelEditionViewDTO(e.getId(), e.getName())).toList());

		return view;
	}

}
