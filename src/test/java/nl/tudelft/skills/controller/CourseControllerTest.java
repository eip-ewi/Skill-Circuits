/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nl.tudelft.skills.controller.old.CourseController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.old.view.course.CourseLevelCourseViewDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.service.old.CourseService;
import reactor.core.publisher.Mono;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CourseControllerTest extends ControllerTest {

	private final CourseController courseController;
	private final CourseService courseService;
	private final EditionRepository editionRepository;

	private final CourseControllerApi courseApi;

	@Autowired
	public CourseControllerTest(CourseRepository courseRepository, CourseControllerApi courseApi,
			EditionRepository editionRepository) {
		this.courseService = mock(CourseService.class);
		this.courseApi = courseApi;
		this.editionRepository = editionRepository;
		this.courseController = new CourseController(courseRepository, courseService, courseApi,
				editionRepository);
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
		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().editions(Collections.emptyList())));
		mvc.perform(get("/course/{id}", db.getCourseRL().getId()))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void getEditionsOfCourse() {
		EditionSummaryDTO edition1 = new EditionSummaryDTO().id(randomId()).name("D");
		EditionSummaryDTO edition2 = new EditionSummaryDTO().id(randomId()).name("b");
		EditionSummaryDTO edition3 = new EditionSummaryDTO().id(randomId()).name("C");
		editionRepository.save(SCEdition.builder().id(edition1.getId()).build());
		editionRepository.save(SCEdition.builder().id(edition2.getId()).build());

		Long courseId = randomId();
		when(courseApi.getCourseById(courseId)).thenReturn(
				Mono.just(new CourseDetailsDTO().editions(List.of(edition1, edition2, edition3))));

		assertThat(courseController.getEditionsOfCourse(courseId)).isEqualTo(List.of(edition2, edition1));
	}

}
