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
package nl.tudelft.skills.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.course.CourseLevelCourseViewDTO;
import nl.tudelft.skills.model.SCCourse;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.security.AuthorisationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CourseServiceTest {

	private final CourseControllerApi courseApi;
	private final EditionControllerApi editionApi;

	private final CourseRepository courseRepository;
	private final EditionRepository editionRepository;
	private final CourseService courseService;

	private final AuthorisationService authorisationService;

	private final LocalDateTime localDateTime;

	@Autowired
	public CourseServiceTest(CourseRepository courseRepository, EditionRepository editionRepository,
			CourseControllerApi courseApi, EditionControllerApi editionApi) {
		this.courseApi = courseApi;
		this.editionApi = editionApi;
		this.courseRepository = courseRepository;
		this.editionRepository = editionRepository;
		authorisationService = mock(AuthorisationService.class);
		courseService = new CourseService(courseApi, editionApi, courseRepository, editionRepository,
				authorisationService);

		localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);
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
		SCEdition edition = new SCEdition(1L, true, Collections.emptyList(), Collections.emptySet(),
				Collections.emptySet(), null);
		editionRepository.save(edition);

		assertThat(courseService.hasAtLeastOneEditionVisibleToStudents(1L)).isTrue();
	}

	@Test
	public void hasAtLeastOneEditionVisibleToStudentsFalse() {
		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().editions(Collections.emptyList())));

		assertThat(courseService.hasAtLeastOneEditionVisibleToStudents(1L)).isFalse();
	}

	@Test
	public void getLastEditionForCourse() {
		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime.plusMinutes(1)),
						new EditionSummaryDTO().id(2L).startDate(localDateTime)));

		editionRepository.save(new SCEdition(1L, true, null, null, null, null));
		editionRepository.save(new SCEdition(2L, true, null, null, null, null));

		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		SCEdition edition = new SCEdition();
		edition.setVisible(true);

		assertThat(courseService.getLastEditionForCourse(courseDetailsDTO)).isEqualTo(1L);
	}

	@Test
	public void getLastEditionForCourseReturnsNull() {
		// No editions
		assertThat(courseService.getLastEditionForCourse(new CourseDetailsDTO().editions(List.of())))
				.isNull();

		// Edition is not visible
		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime)));
		editionRepository.save(new SCEdition(1L, false, null, null, null, null));
		assertThat(courseService.getLastEditionForCourse(courseDetailsDTO)).isNull();
	}

	@Test
	public void getLastEditionForCoursePassNull() {
		assertThat(courseService.getLastEditionForCourse(null)).isNull();
		assertThat(courseService.getLastEditionForCourse(new CourseDetailsDTO().editions(null))).isNull();
	}

	@Test
	public void getDefaultHomepageEditionCourseReturnsMostRecent() {
		// Goal: Test scenario in which getLastEditionForCourse should be called
		// Setup:
		// - Editions (sorted by start date): 1 - visible, 2 - not visible
		// - Roles per edition: 1, 2 - all null
		// Should return: edition 1

		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime),
						new EditionSummaryDTO().id(2L).startDate(localDateTime.plusMinutes(1))));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));
		when(authorisationService.getRoleInEdition(anyLong())).thenReturn(null);

		editionRepository.save(new SCEdition(1L, true, null, null, null, null));
		editionRepository.save(new SCEdition(2L, false, null, null, null, null));

		// if student is not in any edition, then most recent edition is returned
		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isEqualTo(1L);
	}

	@Test
	public void getDefaultHomepageEditionReturnsLastEditionWithRole() {
		// Goal: Test scenarios in which last edition with a student/TA role should be returned
		// Setup:
		// - Editions (sorted by start date): 1, 2, 3 - all visible
		// - Roles per edition: First: 1 - student, 2 - TA, 3 - null; Second: 1 - TA, 2 - student, 3 - null
		// Should return: edition 2

		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime),
						new EditionSummaryDTO().id(2L).startDate(localDateTime.plusMinutes(1)),
						new EditionSummaryDTO().id(3L).startDate(localDateTime.plusMinutes(2))));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		editionRepository.save(new SCEdition(1L, true, null, null, null, null));
		editionRepository.save(new SCEdition(2L, true, null, null, null, null));
		editionRepository.save(new SCEdition(3L, true, null, null, null, null));

		// First setup: 1 - student, 2 - TA, 3 - null
		when(authorisationService.getRoleInEdition(1L)).thenReturn(RoleDetailsDTO.TypeEnum.STUDENT);
		when(authorisationService.getRoleInEdition(2L)).thenReturn(RoleDetailsDTO.TypeEnum.TA);
		when(authorisationService.getRoleInEdition(3L)).thenReturn(null);

		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isEqualTo(2L);

		// Second setup: 1 - TA, 2 - student, 3 - null
		when(authorisationService.getRoleInEdition(1L)).thenReturn(RoleDetailsDTO.TypeEnum.TA);
		when(authorisationService.getRoleInEdition(2L)).thenReturn(RoleDetailsDTO.TypeEnum.STUDENT);

		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isEqualTo(2L);
	}

	@ParameterizedTest
	@CsvSource({ "TEACHER", "HEAD_TA" })
	public void getDefaultHomepageEditionCourseReturnsLastManagedEdition(String role) {
		// Goal: Test scenario in which the managed edition is picked, and it is not visible
		// Setup:
		// - Editions (sorted by start date): 1 - visible, 2 - visible, 3 - not visible
		// - Roles per edition: 1 - student, 2 - TA, 3 - (role parameter)
		// Should return: edition 3

		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime),
						new EditionSummaryDTO().id(2L).startDate(localDateTime.plusMinutes(1)),
						new EditionSummaryDTO().id(3L).startDate(localDateTime.plusMinutes(2))));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		when(authorisationService.getRoleInEdition(1L)).thenReturn(RoleDetailsDTO.TypeEnum.STUDENT);
		when(authorisationService.getRoleInEdition(2L)).thenReturn(RoleDetailsDTO.TypeEnum.TA);
		when(authorisationService.getRoleInEdition(3L)).thenReturn(RoleDetailsDTO.TypeEnum.fromValue(role));

		editionRepository.save(new SCEdition(1L, true, null, null, null, null));
		editionRepository.save(new SCEdition(2L, true, null, null, null, null));
		editionRepository.save(new SCEdition(3L, false, null, null, null, null));

		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isEqualTo(3L);
	}

	@Test
	public void getDefaultHomepageEditionCourseNewestStudentOverwritesHeadTA() {
		// Goal: Test scenario in which there is a head TA edition, but the newer student edition is returned
		// Setup:
		// - Editions (sorted by start date): 1, 2, 3 - all visible
		// - Roles per edition: 1 - TA, 2 - head TA, 3 - student
		// Should return: edition 3

		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime),
						new EditionSummaryDTO().id(2L).startDate(localDateTime.plusMinutes(1)),
						new EditionSummaryDTO().id(3L).startDate(localDateTime.plusMinutes(2))));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		when(authorisationService.getRoleInEdition(1L)).thenReturn(RoleDetailsDTO.TypeEnum.TA);
		when(authorisationService.getRoleInEdition(2L)).thenReturn(RoleDetailsDTO.TypeEnum.HEAD_TA);
		when(authorisationService.getRoleInEdition(3L)).thenReturn(RoleDetailsDTO.TypeEnum.STUDENT);

		editionRepository.save(new SCEdition(1L, true, null, null, null, null));
		editionRepository.save(new SCEdition(2L, true, null, null, null, null));
		editionRepository.save(new SCEdition(3L, true, null, null, null, null));

		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isEqualTo(3L);
	}

	@Test
	public void getDefaultHomepageEditionCourseReturnsStudentIfTAIsHidden() {
		// Goal: Test scenario in which the latest edition is a TA edition, but not visible. It should return the
		// previous visible student edition
		// Setup:
		// - Editions (sorted by start date): 1 - visible, 2 - visible, 3 - not visible
		// - Roles per edition: 1 - student, 2 - student, 3 - TA
		// Should return: edition 2

		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(
				List.of(new EditionSummaryDTO().id(1L).startDate(localDateTime),
						new EditionSummaryDTO().id(2L).startDate(localDateTime.plusMinutes(1)),
						new EditionSummaryDTO().id(3L).startDate(localDateTime.plusMinutes(2))));
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));

		when(authorisationService.getRoleInEdition(1L)).thenReturn(RoleDetailsDTO.TypeEnum.STUDENT);
		when(authorisationService.getRoleInEdition(2L)).thenReturn(RoleDetailsDTO.TypeEnum.STUDENT);
		when(authorisationService.getRoleInEdition(3L)).thenReturn(RoleDetailsDTO.TypeEnum.TA);

		editionRepository.save(new SCEdition(1L, true, null, null, null, null));
		editionRepository.save(new SCEdition(2L, true, null, null, null, null));
		editionRepository.save(new SCEdition(3L, false, null, null, null, null));

		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isEqualTo(2L);
	}

	@Test
	public void getDefaultHomepageEditionCoursePassNull() {
		// Goal: Test scenario in which course or edition list is null
		// Should return: null

		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.empty());
		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isNull();

		CourseDetailsDTO courseDetailsDTO = new CourseDetailsDTO().editions(null);
		when(courseApi.getCourseById(anyLong())).thenReturn(Mono.just(courseDetailsDTO));
		assertThat(courseService.getDefaultHomepageEditionCourse(3L)).isNull();
	}

	@Test
	public void createSCEditionWhenNoneExists() {
		Long courseId = courseRepository.findAll().stream().mapToLong(SCCourse::getId).max().orElse(0) + 1L;
		assertThat(courseRepository.findById(courseId)).isNotPresent();

		SCCourse course = courseService.getOrCreateSCCourse(courseId);

		assertThat(courseRepository.findById(courseId)).isPresent().contains(course);
	}

	@Test
	public void getNumberOfEditions() {
		Long courseId = 0L;
		when(editionApi.getAllEditionsByCourse(anyLong()))
				.thenReturn(Flux.just(new EditionDetailsDTO().id(1L)));
		assertThat(courseService.getNumberOfEditions(courseId)).isEqualTo(1);
	}
}
