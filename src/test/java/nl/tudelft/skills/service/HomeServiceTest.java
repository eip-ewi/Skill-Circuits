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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.test.TestDatabaseLoader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class HomeServiceTest {

	private final RoleControllerApi roleApi;
	private final CourseControllerApi courseApi;
	private final PersonControllerApi personApi;
	private final HomeService homeService;
	private final CourseService courseService;
	private final SkillRepository skillRepository;
	private final TestDatabaseLoader db;

	@Autowired
	public HomeServiceTest(RoleControllerApi roleApi, CourseControllerApi courseApi,
			PersonControllerApi personApi,
			SkillService skillService, TaskCompletionService taskCompletionService,
			AuthorisationService authorisationService, SkillRepository skillRepository,
			TestDatabaseLoader db) {
		this.roleApi = roleApi;
		this.courseApi = courseApi;
		this.personApi = personApi;
		this.skillRepository = skillRepository;
		this.courseService = mock(CourseService.class);
		this.homeService = new HomeService(personApi, courseService, skillService, taskCompletionService,
				authorisationService);
		this.db = db;
	}

	@Test
	void getCompletedSkillsFalse() {
		SCPerson person = new SCPerson();
		TaskCompletion completion = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		person.setTaskCompletions(Set.of(completion));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		// There are no completed skills in the course
		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);

		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(3);
	}

	@Test
	void getCompletedSkillsNullInMap() {
		SCPerson person = new SCPerson();
		TaskCompletion completion = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		person.setTaskCompletions(Set.of(completion));

		Map<Long, Long> courseToEditionMap = new HashMap<>();
		courseToEditionMap.put(db.getCourseRL().getId(), null);

		// There are no completed skills in the course
		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);

		assertTrue(courseCompletedSkills.entrySet().stream().noneMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(0);
	}

	@Test
	void getCompletedSkillsTrue() {
		SCPerson person = new SCPerson();
		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		TaskCompletion completion2 = TaskCompletion.builder().id(2L)
				.person(person).task(db.getTaskDo12ae()).build();
		person.setTaskCompletions(Set.of(completion1, completion2));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		// There is one completed skill in the course
		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(7);
	}

	@Test
	public void getCourseGroupsPersonNull() {
		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L), new CourseSummaryDTO().id(2L),
				new CourseSummaryDTO().id(3L));
		Set<Long> activeCourses = Set.of(1L);
		Set<Long> ownCourses = Set.of();
		Map<String, List<CourseSummaryDTO>> courseGroups = homeService.getCourseGroups(null, courses,
				activeCourses, ownCourses);

		assertThat(courseGroups.keySet()).containsExactlyInAnyOrder("availableActive", "availableFinished",
				"ownActive", "ownFinished", "managed");
		assertThat(courseGroups.get("availableActive")).containsExactly(new CourseSummaryDTO().id(1L));
		assertThat(courseGroups.get("availableFinished")).containsExactly(new CourseSummaryDTO().id(2L),
				new CourseSummaryDTO().id(3L));
		assertThat(courseGroups.get("ownActive")).isEmpty();
		assertThat(courseGroups.get("ownFinished")).isEmpty();
		assertThat(courseGroups.get("managed")).isEmpty();
	}

	@Test
	public void getCourseGroupsPersonNullMalformed() {
		// Malformed, since the person is null but the course is categorized as "own course"
		// With the current implementation, no error is raised, and the course should be categorized as
		// "available" (not "own")

		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L));
		Set<Long> activeCourses = Set.of(1L);
		Set<Long> ownCourses = Set.of(1L);
		Map<String, List<CourseSummaryDTO>> courseGroups = homeService.getCourseGroups(null, courses,
				activeCourses, ownCourses);

		assertThat(courseGroups.keySet()).containsExactlyInAnyOrder("availableActive", "availableFinished",
				"ownActive", "ownFinished", "managed");
		assertThat(courseGroups.get("availableActive")).containsExactly(new CourseSummaryDTO().id(1L));
		assertThat(courseGroups.get("availableFinished")).isEmpty();
		assertThat(courseGroups.get("ownActive")).isEmpty();
		assertThat(courseGroups.get("ownFinished")).isEmpty();
		assertThat(courseGroups.get("managed")).isEmpty();
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "HEAD_TA", "TA", "STUDENT" })
	public void getCourseGroups(String role) {
		// In terms of course groupings, students, TAs and head TAs should be handled the same way

		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L), new CourseSummaryDTO().id(2L),
				new CourseSummaryDTO().id(3L), new CourseSummaryDTO().id(4L), new CourseSummaryDTO().id(5L));
		Set<Long> activeCourses = Set.of(4L, 5L);
		Set<Long> ownCourses = Set.of(2L, 4L);

		// Mock roles
		when(courseApi.getCourseById(1L)).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(1L)))));
		when(courseApi.getCourseById(not(eq(1L)))).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(2L)))));
		mockRoleForEdition(roleApi, "TEACHER", 1L);
		mockRoleForEdition(roleApi, role, 2L);

		Person person = new Person();
		Map<String, List<CourseSummaryDTO>> courseGroups = homeService.getCourseGroups(person, courses,
				activeCourses, ownCourses);

		assertThat(courseGroups.keySet()).containsExactlyInAnyOrder("availableActive", "availableFinished",
				"ownActive", "ownFinished", "managed");
		assertThat(courseGroups.get("availableActive")).containsExactly(new CourseSummaryDTO().id(5L));
		assertThat(courseGroups.get("availableFinished")).containsExactly(new CourseSummaryDTO().id(3L));
		assertThat(courseGroups.get("ownActive")).containsExactly(new CourseSummaryDTO().id(4L));
		assertThat(courseGroups.get("ownFinished")).containsExactly(new CourseSummaryDTO().id(2L));
		assertThat(courseGroups.get("managed")).containsExactly(new CourseSummaryDTO().id(1L));
	}

	@Test
	@WithUserDetails("admin")
	public void getCourseGroupsAsAdmin() {
		// Regardless of the active/"own" status of courses, an admin has the course under "managed"

		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L), new CourseSummaryDTO().id(2L),
				new CourseSummaryDTO().id(3L), new CourseSummaryDTO().id(4L));
		Set<Long> activeCourses = Set.of(3L, 4L);
		Set<Long> ownCourses = Set.of(2L, 3L);

		Person person = new Person();
		Map<String, List<CourseSummaryDTO>> courseGroups = homeService.getCourseGroups(person, courses,
				activeCourses, ownCourses);

		assertThat(courseGroups.keySet()).containsExactlyInAnyOrder("availableActive", "availableFinished",
				"ownActive", "ownFinished", "managed");
		assertThat(courseGroups.get("managed")).containsExactlyInAnyOrderElementsOf(courses);
	}

	@Test
	public void testGetTeacherIdsPersonNull() {
		assertThat(homeService.getTeacherIds(null)).isEmpty();
	}

	@Test
	public void testGetTeacherIds() {
		Person person = Person.builder().id(1L).build();
		Map<Long, String> roleMap = Map.of(1L, "STUDENT", 2L, "HEAD_TA", 3L, "TEACHER",
				4L, "TA");
		mockRolesForEditions(roleMap, 1L);

		assertThat(homeService.getTeacherIds(person)).containsExactlyInAnyOrder(2L, 3L);

		// Admin should have access to all editions they have a role in
		person = Person.builder().id(1L).defaultRole(DefaultRole.ADMIN).build();
		assertThat(homeService.getTeacherIds(person)).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
	}

	@Test
	public void testGetActiveCourses() {
		// Test different groupings of courses/editions:
		// 1 -> not visible, not a teacher => inactive
		// 2, 3, 4 -> current time is after start and after end => inactive
		// 5, 6, 7 -> current time is before start and before end => inactive
		// 8, 9, 10 -> current time is after start and before end => active
		// For all groups of 3, there is one where the edition is visible (2, 5, 8), one where the user is a teacher
		// (3, 6, 9) and one where both are true (4, 7, 10).
		// Additionally, there is a course (11) for which there is no default edition => inactive

		Set<Long> visible = Set.of(2L, 4L, 5L, 7L, 8L, 10L);
		Set<Long> teacherIds = Set.of(3L, 4L, 6L, 7L, 9L, 10L);

		List<EditionDetailsDTO> editions = new ArrayList<>();
		LocalDateTime beforeNow = LocalDateTime.now().minusYears(1);
		LocalDateTime afterNow = LocalDateTime.now().plusYears(1);

		// Start -- End -- Now => Not active
		for (long i = 2L; i <= 4L; i++) {
			editions.add(new EditionDetailsDTO().id(i).startDate(beforeNow).endDate(beforeNow.plusDays(1)));
		}

		// Now -- Start -- End => Not active
		for (long i = 5L; i <= 7L; i++) {
			editions.add(new EditionDetailsDTO().id(i).startDate(afterNow).endDate(afterNow.plusYears(1)));
		}

		// Start -- Now -- End => Not active
		for (long i = 8L; i <= 10L; i++) {
			editions.add(new EditionDetailsDTO().id(i).startDate(beforeNow).endDate(afterNow));
		}

		// Create courseToEditionMap
		Map<Long, Long> courseToEditionMap = new HashMap<>();
		for (long i = 1L; i <= 10L; i++) {
			courseToEditionMap.put(i, i);
		}
		// Add a course that does not have any default edition (e.g., all editions are not visible and user is student)
		courseToEditionMap.put(11L, null);

		// Should only contain 8, 9, 10 as active courses (for all others there are different reasons as to why they
		// are not active)
		assertThat(homeService.getActiveCourses(editions, visible, teacherIds, courseToEditionMap))
				.containsExactlyInAnyOrder(8L, 9L, 10L);
	}

	@Test
	public void testGetCourseToEditionMap() {
		// Contains a course with a default edition, and a course without (mocked response)

		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L),
				new CourseSummaryDTO().id(2L));

		when(courseService.getDefaultHomepageEditionCourse(1L)).thenReturn(11L);
		when(courseService.getDefaultHomepageEditionCourse(2L)).thenReturn(null);

		Map<Long, Long> courseToEditionMap = homeService.getCourseToEditionMap(courses);
		Map<Long, Long> expected = new HashMap<>();
		expected.put(1L, 11L);
		expected.put(2L, null);

		assertThat(courseToEditionMap).isEqualTo(expected);
	}

	@Test
	public void testGetOwnCoursesNotLoggedIn() {
		assertThat(homeService.getOwnCourses(null, Map.of(1L, 2L, 3L, 4L))).isEmpty();
	}

	@Test
	@WithUserDetails("username")
	public void testGetOwnCourses() {
		// Test setup:
		// - Map contains courses with a default edition, and a course without (null as value in map)
		// - User has different roles in some courses with editions
		// - User has no role in one of the courses with edition

		Map<Long, Long> courseToEditionMap = new HashMap<>();
		courseToEditionMap.put(1L, 11L);
		courseToEditionMap.put(2L, 12L);
		courseToEditionMap.put(3L, 13L);
		courseToEditionMap.put(4L, 14L);
		courseToEditionMap.put(5L, 15L);
		courseToEditionMap.put(6L, null);
		Person person = new Person();

		// Mock roles of user (check most important roles - role should not matter)
		mockRoleForEdition(roleApi, "STUDENT", 11L);
		mockRoleForEdition(roleApi, "TEACHER", 12L);
		mockRoleForEdition(roleApi, "TA", 13L);
		mockRoleForEdition(roleApi, "HEAD_TA", 14L);
		mockRoleForEdition(roleApi, null, 15L);

		assertThat(homeService.getOwnCourses(person, courseToEditionMap))
				.containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
	}

	@Test
	void getCompletedSkillsAllCompleted() {
		SCPerson person = new SCPerson();
		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		TaskCompletion completion2 = TaskCompletion.builder().id(2L)
				.person(person).task(db.getTaskDo12ae()).build();
		TaskCompletion completion3 = TaskCompletion.builder().id(3L)
				.person(person).task(db.getTaskRead11()).build();
		TaskCompletion completion4 = TaskCompletion.builder().id(4L)
				.person(person).task(db.getTaskDo11ad()).build();
		TaskCompletion completion5 = TaskCompletion.builder().id(5L)
				.person(person).task(db.getTaskRead10()).build();
		TaskCompletion completion6 = TaskCompletion.builder().id(6L)
				.person(person).task(db.getTaskDo10a()).build();
		person.setTaskCompletions(
				Set.of(completion1, completion2, completion3, completion4, completion5, completion6));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		// All the skills are completed in the course
		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId()))
				.isEqualTo(skillRepository.findAll().size());
	}

	@Test
	void getCompletedSkillsCustomizedEmpty() {
		SCPerson person = new SCPerson();
		person.setSkillsModified(new HashSet<>(Arrays.asList(db.getSkillImplication())));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(7);
	}

	@Test
	void getCompletedSkillsCustomizedCompleted() {
		SCPerson person = new SCPerson();
		person.setSkillsModified(new HashSet<>(Arrays.asList(db.getSkillNegation())));
		person.setTasksAdded(new HashSet<>(Arrays.asList(db.getTaskRead11())));

		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead11()).build();

		person.setTaskCompletions(Set.of(completion1));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(5);
	}

	@Test
	void getCompletedSkillsPathFinderPath() {
		SCPerson person = new SCPerson();
		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
				.edition(db.getEditionRL()).person(person).build();
		person.setPathPreferences(new HashSet<>(Arrays.asList(pathPreference)));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(7);
	}

	@Test
	void getCompletedSkillsCustomizedEmptyNotCompleted() {
		SCPerson person = new SCPerson();
		Task t = Task.builder().name("Task").time(3).build();
		db.getSkillAssumption().setTasks(Arrays.asList(t));
		person.setSkillsModified(new HashSet<>(Arrays.asList(db.getSkillAssumption())));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		Map<Long, Integer> courseCompletedSkills = homeService.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(3);
	}

	/**
	 * Creates a RoleDetailsDTO with the specified edition id and role.
	 *
	 * @param  id   The id of the edition.
	 * @param  role The role.
	 * @return      A RoleDetailsDTO with the specified edition id and role.
	 */
	private RoleDetailsDTO getRoleDetails(Long id, RoleDetailsDTO.TypeEnum role) {
		return new RoleDetailsDTO().id(new Id().editionId(id).personId(db.getPerson().getId()))
				.person(new PersonSummaryDTO().id(db.getPerson().getId()).username("username"))
				.edition(new EditionSummaryDTO().id(id))
				.type(role);
	}

	/**
	 * Mocks the response of the role api for a given role for a specific edition.
	 *
	 * @param roleApi The roleApi.
	 * @param role    The role to return for the user.
	 * @param edition The edition for which to return this role.
	 */
	protected void mockRoleForEdition(RoleControllerApi roleApi, String role, Long edition) {
		if (role == null || role.isBlank()) {
			when(roleApi.getRolesById(eq(Set.of(edition)), anySet())).thenReturn(Flux.empty());
		} else {
			when(roleApi.getRolesById(eq(Set.of(edition)), anySet()))
					.thenReturn(Flux.just(new RoleDetailsDTO()
							.id(new Id().editionId(edition)
									.personId(db.getPerson().getId()))
							.person(new PersonSummaryDTO().id(db.getPerson().getId()).username("username"))
							.type(RoleDetailsDTO.TypeEnum.valueOf(role))));
		}
	}

	/**
	 * Mock response from person API for retrieving all the roles of one person.
	 *
	 * @param roles    A map of edition id to the role in that edition. The role in the map may be null, in
	 *                 which case the user has no role in that edition.
	 * @param personId The id of the user.
	 */
	void mockRolesForEditions(Map<Long, String> roles, Long personId) {
		List<RoleEditionDetailsDTO> roleDTOS = roles
				.entrySet().stream().map(entry -> new RoleEditionDetailsDTO()
						.id(new Id().editionId(entry.getKey())
								.personId(db.getPerson().getId()))
						.type(RoleEditionDetailsDTO.TypeEnum.valueOf(entry.getValue())))
				.toList();
		when(personApi.getRolesForPerson(eq(personId))).thenReturn(Flux.fromIterable(roleDTOS));
	}

}
