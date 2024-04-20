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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.*;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.SkillService;
import nl.tudelft.skills.service.TaskCompletionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class HomeControllerTest extends ControllerTest {

	private final HomeController homeController;
	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;
	private final PersonControllerApi personApi;
	private final SkillRepository skillRepository;
	private final CourseControllerApi courseApi;
	private final EditionRepository editionRepository;
	private final CourseService courseService;

	@Autowired
	public HomeControllerTest(EditionControllerApi editionApi,
			PersonControllerApi personApi, EditionRepository editionRepository,
			PersonRepository personRepository, SkillService skillService,
			TaskCompletionService taskCompletionService, AuthorisationService authorisationService,
			SkillRepository skillRepository, RoleControllerApi roleApi, CourseControllerApi courseApi) {
		this.editionApi = editionApi;
		this.personApi = personApi;
		this.editionRepository = editionRepository;
		this.courseService = mock(CourseService.class);
		this.skillRepository = skillRepository;
		this.roleApi = roleApi;
		this.courseApi = courseApi;
		this.homeController = new HomeController(editionApi, personApi, editionRepository,
				personRepository, courseService, skillService, taskCompletionService, authorisationService);
	}

	@Test
	void getLoginPage() {
		assertThat(homeController.getLoginPage()).isEqualTo("login");
	}

	@Test
	@SuppressWarnings("unchecked")
	void getHomePageExampleUserNull() {
		// Exactly one edition which is currently active and visible, the user being null

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.saveAndFlush(edition);

		CourseSummaryDTO course = new CourseSummaryDTO().id(randomId());

		LocalDateTime localDateTime = LocalDateTime.now();
		when(editionApi.getEditionsById(eq(List.of(edition.getId()))))
				.thenReturn(Flux.just(new EditionDetailsDTO().id(edition.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course)));

		when(courseService.getDefaultHomepageEditionCourse(course.getId())).thenReturn(edition.getId());

		homeController.getHomePage(null, model);

		Set<String> attributes = Set.of("availableFinished", "ownActive", "ownFinished", "managed");
		for (String attribute : attributes) {
			assertThat((List<CourseSummaryDTO>) model.getAttribute(attribute)).isEmpty();
		}

		assertThat((List<CourseSummaryDTO>) model.getAttribute("availableActive")).containsExactly(course);
	}

	@Test
	@SuppressWarnings("unchecked")
	@WithUserDetails("username")
	void getHomePageExampleLoggedIn() {
		// One course is not active and has also not been worked in (course2), one course is active
		// and has been worked in (course1), and one course is managed as teacher (course3)

		SCEdition edition1 = db.getEditionRL();
		edition1.setVisible(true);
		editionRepository.saveAndFlush(edition1);
		SCEdition edition2 = SCEdition.builder().id(db.getEditionRL().getId() + 1L).isVisible(true).build();
		editionRepository.saveAndFlush(edition2);
		SCEdition edition3 = SCEdition.builder().id(db.getEditionRL().getId() + 2L).isVisible(false).build();
		editionRepository.saveAndFlush(edition3);

		CourseSummaryDTO course1 = new CourseSummaryDTO().id(1L);
		CourseSummaryDTO course2 = new CourseSummaryDTO().id(2L);
		CourseSummaryDTO course3 = new CourseSummaryDTO().id(3L);

		// Mock responses for editions and courses
		LocalDateTime localDateTime = LocalDateTime.now();
		Set<EditionDetailsDTO> editions = Set.of(
				new EditionDetailsDTO().id(edition1.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course1),
				new EditionDetailsDTO().id(edition2.getId())
						.startDate(localDateTime.minusYears(2)).endDate(localDateTime.minusYears(1))
						.course(course2),
				new EditionDetailsDTO().id(edition3.getId())
						.startDate(localDateTime.minusYears(2)).endDate(localDateTime.plusYears(1))
						.course(course3));
		when(editionApi.getEditionsById(List.of(edition1.getId(), edition2.getId(), edition3.getId())))
				.thenReturn(Flux.fromIterable(editions));

		when(courseService.getDefaultHomepageEditionCourse(1L)).thenReturn(edition1.getId());
		when(courseService.getDefaultHomepageEditionCourse(2L)).thenReturn(edition2.getId());
		when(courseService.getDefaultHomepageEditionCourse(3L)).thenReturn(edition3.getId());
		when(courseApi.getCourseById(1L)).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(edition1.getId())))));
		when(courseApi.getCourseById(2L)).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(edition2.getId())))));
		when(courseApi.getCourseById(3L)).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(edition3.getId())))));

		// Mock roles of user
		mockRoleForEdition(roleApi, "STUDENT", db.getEditionRL().getId());
		mockRoleForEdition(roleApi, "STUDENT", db.getEditionRL().getId() + 1L);
		mockRoleForEdition(roleApi, "TEACHER", db.getEditionRL().getId() + 2L);
		Set<RoleDetailsDTO> roles = Set.of(
				getRoleDetails(db.getEditionRL().getId(), RoleDetailsDTO.TypeEnum.STUDENT),
				getRoleDetails(db.getEditionRL().getId() + 1L, RoleDetailsDTO.TypeEnum.STUDENT),
				getRoleDetails(db.getEditionRL().getId() + 2L, RoleDetailsDTO.TypeEnum.TEACHER));
		when(roleApi.getRolesById(eq(Set.of(db.getEditionRL().getId(), db.getEditionRL().getId() + 1L,
				db.getEditionRL().getId() + 2L)), anySet())).thenReturn(Flux.fromIterable(roles));

		Person person = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();
		Map<Long, String> roleMap = Map.of(db.getEditionRL().getId(), "STUDENT",
				db.getEditionRL().getId() + 1L, "STUDENT", db.getEditionRL().getId() + 2L, "TEACHER");
		mockRolesForEditions(roleMap, person.getId());

		homeController.getHomePage(person, model);

		assertThat((List<CourseSummaryDTO>) model.getAttribute("ownFinished")).containsExactly(course2);
		assertThat((List<CourseSummaryDTO>) model.getAttribute("availableActive")).isEmpty();
		assertThat((List<CourseSummaryDTO>) model.getAttribute("ownActive")).containsExactly(course1);
		assertThat((List<CourseSummaryDTO>) model.getAttribute("availableFinished")).isEmpty();
		assertThat((List<CourseSummaryDTO>) model.getAttribute("managed")).containsExactly(course3);
	}

	@Test
	void getCompletedSkillsFalse() {
		SCPerson person = new SCPerson();
		TaskCompletion completion = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		person.setTaskCompletions(Set.of(completion));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		// There are no completed skills in the course
		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
				courseToEditionMap);

		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(3);
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
		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
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
		Map<String, List<CourseSummaryDTO>> courseGroups = homeController.getCourseGroups(null, courses,
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
	public void getCourseGroupsPersonNullMalformedCompletedTask() {
		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L));
		Set<Long> activeCourses = Set.of(1L);
		Set<Long> ownCourses = Set.of(1L);
		Map<String, List<CourseSummaryDTO>> courseGroups = homeController.getCourseGroups(null, courses,
				activeCourses, ownCourses);

		assertThat(courseGroups.keySet()).containsExactlyInAnyOrder("availableActive", "availableFinished",
				"ownActive", "ownFinished", "managed");
		assertThat(courseGroups.get("availableActive")).containsExactly(new CourseSummaryDTO().id(1L));
		assertThat(courseGroups.get("availableFinished")).isEmpty();
		assertThat(courseGroups.get("ownActive")).isEmpty();
		assertThat(courseGroups.get("ownFinished")).isEmpty();
		assertThat(courseGroups.get("managed")).isEmpty();
	}

	@Test
	@WithUserDetails("username")
	public void getCourseGroups() {
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
		mockRoleForEdition(roleApi, "STUDENT", 2L);

		Person person = new Person();
		Map<String, List<CourseSummaryDTO>> courseGroups = homeController.getCourseGroups(person, courses,
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
	public void testGetTeacherIdsPersonNull() {
		assertThat(homeController.getTeacherIds(null)).isEmpty();
	}

	@Test
	@WithUserDetails("username")
	public void testGetTeacherIdsNotTeacher() {
		Set<RoleDetailsDTO> roles = Set.of(getRoleDetails(1L, RoleDetailsDTO.TypeEnum.STUDENT),
				getRoleDetails(2L, RoleDetailsDTO.TypeEnum.TA),
				getRoleDetails(3L, RoleDetailsDTO.TypeEnum.HEAD_TA));
		when(roleApi.getRolesById(eq(Set.of(1L, 2L, 3L)), anySet()))
				.thenReturn(Flux.fromIterable(roles));

		Person person = Person.builder().id(db.getPerson().getId()).build();
		Map<Long, String> roleMap = Map.of(1L, "STUDENT", 2L, "STUDENT", 3L, "STUDENT");
		mockRolesForEditions(roleMap, person.getId());
		assertThat(homeController.getTeacherIds(person)).isEmpty();
	}

	@Test
	@WithUserDetails("username")
	public void testGetTeacherIds() {
		Long userId = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal())
				.getUser().getId();

		Map<Long, String> roleMap = Map.of(1L, "STUDENT", 2L, "HEAD_TA", 3L, "TEACHER",
				4L, "ADMIN", 5L, "TA");
		mockRolesForEditions(roleMap, userId);

		Person person = Person.builder().id(userId).build();
		assertThat(homeController.getTeacherIds(person)).containsExactlyInAnyOrder(2L, 3L, 4L);
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

		Set<Long> visible = Set.of(2L, 4L, 5L, 7L, 8L, 10L);
		Set<Long> teacherIds = Set.of(3L, 4L, 6L, 7L, 9L, 10L);
		// TODO add null value as well
		Map<Long, Long> courseToEditionMap = Map.of(1L, 1L, 2L, 2L, 3L, 3L, 4L, 4L, 5L, 5L, 6L, 6L, 7L, 7L,
				8L, 8L, 9L, 9L, 10L, 10L);

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

		// Should only contain 8, 9, 10 as active courses (for all others there are different reasons as to why they
		// are not active)
		assertThat(homeController.getActiveCourses(editions, visible, teacherIds, courseToEditionMap))
				.containsExactlyInAnyOrder(8L, 9L, 10L);
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
		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
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

		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
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

		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
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

		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(6);
	}

	@Test
	void getCompletedSkillsCustomizedEmptyNotCompleted() {
		SCPerson person = new SCPerson();
		Task t = Task.builder().name("Task").time(3).build();
		db.getSkillAssumption().setTasks(Arrays.asList(t));
		person.setSkillsModified(new HashSet<>(Arrays.asList(db.getSkillAssumption())));

		Map<Long, Long> courseToEditionMap = Map.of(db.getCourseRL().getId(), db.getEditionRL().getId());

		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(person,
				courseToEditionMap);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(3);
	}

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
