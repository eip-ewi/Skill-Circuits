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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.course.CourseLevelCourseViewDTO;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.service.CourseService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CourseControllerTest extends ControllerTest {

	private final CourseController courseController;
	private final CourseService courseService;

	@Autowired
	public CourseControllerTest(CourseRepository courseRepository) {
		this.courseService = mock(CourseService.class);
		this.courseController = new CourseController(courseRepository, courseService);
	}

	@Test
	void getCoursePage() {
		CourseLevelCourseViewDTO mockCourseView = View.convert(db.getCourseRL(),
				CourseLevelCourseViewDTO.class);

		mockCourseView.setName("course name");
		mockCourseView.setCode("course code");
		mockCourseView.setId(10L);
		mockCourseView.setEditions(new LinkedList<>());

		when(courseService.getCourseView(anyLong())).thenReturn(mockCourseView);

		Long courseId = db.getCourseRL().getId();

		courseController.getCoursePage(courseId, model);

		assertThat(model.getAttribute("course")).isEqualTo(mockCourseView);

		verify(courseService).getCourseView(courseId);
	}

	@Test
	void viewAllEditionsIsForbiddenForNonTeacher() throws Exception {
		mvc.perform(get("/course/{id}", db.course.getId()))
				.andExpect(status().is3xxRedirection());
	}
}
