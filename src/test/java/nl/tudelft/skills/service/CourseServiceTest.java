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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.course.CourseLevelCourseViewDTO;
import nl.tudelft.skills.model.SCCourse;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.security.AuthorisationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CourseServiceTest {

	private CourseControllerApi courseApi;

	private CourseRepository courseRepository;
	private EditionRepository editionRepository;

	private CourseService courseService;

	private final AuthorisationService authorisationService;

	@Autowired
	public CourseServiceTest(CourseRepository courseRepository, EditionRepository editionRepository,
			CourseControllerApi courseApi) {
		this.courseApi = courseApi;
		this.courseRepository = courseRepository;
		this.editionRepository = editionRepository;
		authorisationService = mock(AuthorisationService.class);
		courseService = new CourseService(courseApi, courseRepository, editionRepository,
				authorisationService);
	}

	@Test
	public void getCourseView() {
		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().id(1L).name("course").code("code")
				.editions(List.of());

		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		CourseLevelCourseViewDTO view = new CourseLevelCourseViewDTO(1L, "course", "code", List.of());

		assertThat(courseService.getCourseView(1L)).isEqualTo(view);
	}

	@Test
	public void hasAtLeastOneEditionVisibleToStudents() {
		when(courseApi.getCourseById(anyLong())).thenReturn(
				Mono.just(new CourseDetailsDTO().editions(List.of(new EditionSummaryDTO().id(1L)))));
		SCEdition edition = new SCEdition(1L, true, Collections.emptySet());
		editionRepository.save(edition);

		assertThat(courseService.hasAtLeastOneEditionVisibleToStudents(1L));
	}

	@Test
	public void hasAtLeastOneEditionVisibleToStudentsFalse() {
		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().editions(Collections.emptyList())));

		assertThat(!courseService.hasAtLeastOneEditionVisibleToStudents(1L));
	}

	@Test
	public void getLastEditionForCourse() {
		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(LocalDateTime.now()),
						new EditionSummaryDTO().id(2L).startDate(LocalDateTime.now())));

		editionRepository.save(new SCEdition(1L, true, null));
		editionRepository.save(new SCEdition(2L, true, null));

		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		SCEdition edition = new SCEdition();
		edition.setVisible(true);

		//		when(editionRepository.findById(anyLong())).thenReturn(Optional.of(edition));

		assertThat(courseService.getLastEditionForCourse(3L)).isEqualTo(2L);
	}

	@Test
	public void getLastStudentEditionForCourseOrLastReturnsMostRecent() {
		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(LocalDateTime.now()),
						new EditionSummaryDTO().id(2L).startDate(LocalDateTime.now())));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));
		when(authorisationService.isStudentInEdition(anyLong())).thenReturn(false);

		editionRepository.save(new SCEdition(1L, true, null));
		editionRepository.save(new SCEdition(2L, true, null));

		// if student is not in any edition, then most recent edition is returned
		assertThat(courseService.getLastStudentEditionForCourseOrLast(3L)).isEqualTo(2L);
	}

	@Test
	public void getLastStudentEditionForCourseOrLastReturnsLast() {
		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(LocalDateTime.now()),
						new EditionSummaryDTO().id(2L).startDate(LocalDateTime.now()),
						new EditionSummaryDTO().id(3L).startDate(LocalDateTime.now())));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		when(authorisationService.isStudentInEdition(1L)).thenReturn(true);
		when(authorisationService.isStudentInEdition(2L)).thenReturn(true);
		when(authorisationService.isStudentInEdition(3L)).thenReturn(false);

		editionRepository.save(new SCEdition(1L, true, null));
		editionRepository.save(new SCEdition(2L, true, null));
		editionRepository.save(new SCEdition(3L, true, null));

		// if student is at least in one edition, then most recent active edition is returned
		assertThat(courseService.getLastStudentEditionForCourseOrLast(3L)).isEqualTo(2L);
	}

	@Test
	public void createSCEditionWhenNoneExists() {
		Long courseId = 1L;
		assertThat(courseRepository.findById(courseId)).isNotPresent();

		SCCourse course = courseService.getOrCreateSCCourse(courseId);

		assertThat(courseRepository.findById(courseId)).isPresent().contains(course);
	}
}
