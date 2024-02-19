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

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.course.CourseLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.course.CourseLevelEditionViewDTO;
import nl.tudelft.skills.model.SCCourse;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.security.AuthorisationService;

@Service
public class CourseService {

	private CourseControllerApi courseApi;
	private EditionControllerApi editionApi;
	private CourseRepository courseRepository;
	private EditionRepository editionRepository;
	private AuthorisationService authorisationService;

	@Autowired
	public CourseService(CourseControllerApi courseApi, EditionControllerApi editionApi,
			CourseRepository courseRepository,
			EditionRepository editionRepository, AuthorisationService authorisationService) {
		this.courseApi = courseApi;
		this.editionApi = editionApi;
		this.courseRepository = courseRepository;
		this.authorisationService = authorisationService;
		this.editionRepository = editionRepository;
	}

	/**
	 * Return CourseViewDto for course with id, including course name from Labrador db and SCEditions.
	 *
	 * @param  id Course id.
	 * @return    CourseViewDTO for course with id.
	 */
	public CourseLevelCourseViewDTO getCourseView(Long id) {
		CourseDetailsDTO course = courseApi.getCourseById(id).block();

		CourseLevelCourseViewDTO view = View.convert(getOrCreateSCCourse(id),
				CourseLevelCourseViewDTO.class);

		view.setName(course.getName());

		view.setCode(course.getCode());

		view.setEditions(course.getEditions().stream()
				.map(e -> new CourseLevelEditionViewDTO(e.getId(), e.getName())).toList());

		return view;
	}

	/**
	 * Returns the most recent edition that is visible for a course with id.
	 *
	 * @param  id Id of the course.
	 * @return    The most recent edition for the course.
	 */
	public Long getLastEditionForCourse(Long id) {
		CourseDetailsDTO course = courseApi.getCourseById(id).block();

		return course.getEditions().stream()
				.filter(e -> editionRepository.findById(e.getId()).map(SCEdition::isVisible).orElse(false))
				.max(Comparator.comparing(EditionSummaryDTO::getStartDate))
				.map(EditionSummaryDTO::getId)
				.orElse(null);
	}

	/**
	 * Returns the most recent edition in a course that a student is enrolled in. If none is found the most
	 * recent edition is returned.
	 *
	 * @param  id Id of the course.
	 * @return    The most recent edition in a course in which a student is enrolled.
	 */
	public Long getLastStudentEditionForCourseOrLast(Long id) {
		CourseDetailsDTO course = courseApi.getCourseById(id).block();

		return course.getEditions().stream()
				.filter(e -> {
					if (!authorisationService.isHeadTAInEdition(e.getId())) {
						return editionRepository.findById(e.getId()).map(SCEdition::isVisible).orElse(false)
								&&
								authorisationService.isStudentInEdition(e.getId());
					}
					return true;
				})
				.max(Comparator.comparing(EditionSummaryDTO::getStartDate))
				.map(EditionSummaryDTO::getId)
				.orElseGet(() -> getLastEditionForCourse(id));
	}

	/**
	 * Returns whether the course has any visible editions.
	 *
	 * @param  id Id of the course.
	 * @return    True if the course has any editions set to visible for students.
	 */
	public boolean hasAtLeastOneEditionVisibleToStudents(Long id) {
		CourseDetailsDTO course = courseApi.getCourseById(id).block();

		return course.getEditions().stream()
				.filter(e -> editionRepository.findById(e.getId()).map(SCEdition::isVisible).orElse(false))
				.toList().size() > 0;
	}

	/**
	 * Returns a SCCourse by course id. If it doesn't exist, creates one.
	 *
	 * @param  id The id of the course
	 * @return    The SCCourse with id.
	 */
	@Transactional
	public SCCourse getOrCreateSCCourse(Long id) {
		return courseRepository.findById(id)
				.orElseGet(() -> courseRepository.save(SCCourse.builder().id(id).build()));
	}

	/**
	 * Returns the number of editions in a course.
	 *
	 * @param  id Course id
	 * @return    The number of editions in a course.
	 */
	public Integer getNumberOfEditions(Long id) {
		return editionApi.getAllEditionsByCourse(id).count().block().intValue();
	}
}
