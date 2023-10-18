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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.*;

import javax.transaction.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.EditionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class HomeControllerTest extends ControllerTest {

	private final HomeController homeController;
	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;
	private final CourseControllerApi courseApi;
	private final EditionRepository editionRepository;
	private final PersonRepository personRepository;
	private final EditionService editionService;
	private final CourseService courseService;

	@Autowired
	public HomeControllerTest(EditionControllerApi editionApi, RoleControllerApi roleApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository,
			PersonRepository personRepository, EditionService editionService,
			AuthorisationService authorisationService, CourseControllerApi courseApi) {
		this.editionApi = editionApi;
		this.roleApi = roleApi;
		this.courseApi = courseApi;
		this.editionRepository = editionRepository;
		this.personRepository = personRepository;
		this.editionService = editionService;
		this.courseService = mock(CourseService.class);
		this.homeController = new HomeController(editionApi, roleApi, editionRepository, moduleRepository,
				personRepository, editionService, courseService, authorisationService);
	}

	@Test
	void getLoginPage() {
		assertThat(homeController.getLoginPage()).isEqualTo("login");
	}

	@Test
	@SuppressWarnings("unchecked")
	void getHomePage() {
		// Exactly one edition which is currently active and visible, the user being null

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.saveAndFlush(edition);

		CourseSummaryDTO course = new CourseSummaryDTO().id(randomId());

		LocalDateTime localDateTime = LocalDateTime.now();
		when(editionApi.getAllEditions())
				.thenReturn(Flux.just(new EditionDetailsDTO().id(edition.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course)));

		homeController.getHomePage(null, model);

		Set<String> attributes = Set.of("availableFinished", "ownActive", "ownFinished", "managed");
		for (String attribute : attributes) {
			assertThat((List<CourseSummaryDTO>) model.getAttribute(attribute)).isEmpty();
		}

		assertThat((List<CourseSummaryDTO>) model.getAttribute("availableActive")).containsExactly(course);
	}

	@Test
	void getCompletedSkillsFalse() {
		List<CourseSummaryDTO> courses = new ArrayList<>(
				Arrays.asList(new CourseSummaryDTO().id(db.getCourseRL().getId())));
		SCPerson person = new SCPerson();
		TaskCompletion completion = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		person.setTaskCompletions(Set.of(completion));

		when(courseService.getLastStudentEditionForCourseOrLast(anyLong()))
				.thenReturn(db.getEditionRL().getId());

		// There are no completed skills in the course
		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(courses,
				person);

		assertFalse(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(0);
	}

	@Test
	void getCompletedSkillsTrue() {
		List<CourseSummaryDTO> courses = new ArrayList<>(
				Arrays.asList(new CourseSummaryDTO().id(db.getCourseRL().getId())));
		SCPerson person = new SCPerson();
		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
				.person(person).task(db.getTaskRead12()).build();
		TaskCompletion completion2 = TaskCompletion.builder().id(2L)
				.person(person).task(db.getTaskDo12ae()).build();
		person.setTaskCompletions(Set.of(completion1, completion2));

		when(courseService.getLastStudentEditionForCourseOrLast(anyLong()))
				.thenReturn(db.getEditionRL().getId());

		// There is one completed skill in the course
		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(courses,
				person);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(1);
	}

	@Test
	public void getCourseGroupsPersonNull() {
		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L), new CourseSummaryDTO().id(2L),
				new CourseSummaryDTO().id(3L));
		Set<Long> activeCourses = Set.of(1L);
		Map<Long, Boolean> completedTaskInCourse = Map.of(1L, false, 2L, false, 3L, false);
		Map<String, List<CourseSummaryDTO>> courseGroups = homeController.getCourseGroups(null, courses,
				activeCourses, completedTaskInCourse);

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
		Map<Long, Boolean> completedTaskInCourse = Map.of(1L, true);
		Map<String, List<CourseSummaryDTO>> courseGroups = homeController.getCourseGroups(null, courses,
				activeCourses, completedTaskInCourse);

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
		Map<Long, Boolean> completedTaskInCourse = Map.of(1L, false, 2L, true, 3L, false,
				4L, true, 5L, false);

		// Mock roles
		when(courseApi.getCourseById(1L)).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(1L)))));
		when(courseApi.getCourseById(not(eq(1L)))).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(2L)))));
		mockRoleForEdition(roleApi, "TEACHER", 1L);
		mockRoleForEdition(roleApi, "STUDENT", 2L);

		Person person = new Person();
		Map<String, List<CourseSummaryDTO>> courseGroups = homeController.getCourseGroups(person, courses,
				activeCourses, completedTaskInCourse);

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
		List<EditionDetailsDTO> editions = List.of(new EditionDetailsDTO().id(1L),
				new EditionDetailsDTO().id(2L));
		assertThat(homeController.getTeacherIds(null, editions)).isEmpty();
	}

	@Test
	@WithUserDetails("username")
	public void testGetTeacherIdsNotTeacher() {
		Set<RoleDetailsDTO> roles = Set.of(getRoleDetails(1L, RoleDetailsDTO.TypeEnum.STUDENT),
				getRoleDetails(2L, RoleDetailsDTO.TypeEnum.TA),
				getRoleDetails(3L, RoleDetailsDTO.TypeEnum.HEAD_TA));
		when(roleApi.getRolesById(eq(Set.of(1L, 2L, 3L)), anySet()))
				.thenReturn(Flux.fromIterable(roles));

		mockRoleForEdition(roleApi, "STUDENT", 1L);
		mockRoleForEdition(roleApi, "STUDENT", 2L);
		mockRoleForEdition(roleApi, "STUDENT", 3L);

		List<EditionDetailsDTO> editions = List.of(new EditionDetailsDTO().id(1L),
				new EditionDetailsDTO().id(2L),
				new EditionDetailsDTO().id(3L));
		Person person = Person.builder().id(db.getPerson().getId()).build();
		assertThat(homeController.getTeacherIds(person, editions)).isEmpty();
	}

	@Test
	@WithUserDetails("username")
	public void testGetTeacherIds() {
		Set<RoleDetailsDTO> roles = Set.of(getRoleDetails(1L, RoleDetailsDTO.TypeEnum.STUDENT),
				getRoleDetails(2L, RoleDetailsDTO.TypeEnum.TEACHER),
				getRoleDetails(3L, RoleDetailsDTO.TypeEnum.TEACHER));
		when(roleApi.getRolesById(eq(Set.of(1L, 2L, 3L)), anySet()))
				.thenReturn(Flux.fromIterable(roles));

		List<EditionDetailsDTO> editions = List.of(new EditionDetailsDTO().id(1L),
				new EditionDetailsDTO().id(2L),
				new EditionDetailsDTO().id(3L));
		Long userId = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal())
						.getUser().getId();
		Person person = Person.builder().id(userId).build();
		assertThat(homeController.getTeacherIds(person, editions)).containsExactlyInAnyOrder(2L, 3L);
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

		// Make each course contain exactly one edition with the same id as the course
		List<CourseSummaryDTO> courseSummaries = new ArrayList<>();
		for (long i = 1L; i <= 10L; i++) {
			courseSummaries.add(new CourseSummaryDTO().id(i));
			when(courseService.getLastStudentEditionForCourseOrLast(i)).thenReturn(i);
			editionRepository.save(SCEdition.builder().id(i).build());
		}

		Map<Long, EditionDetailsDTO> editions = new HashMap<>();
		LocalDateTime beforeNow = LocalDateTime.now().minusYears(1);
		LocalDateTime afterNow = LocalDateTime.now().plusYears(1);

		// Start -- End -- Now => Not active
		for (long i = 2L; i <= 4L; i++) {
			editions.put(i,
					new EditionDetailsDTO().id(i).startDate(beforeNow).endDate(beforeNow.plusDays(1)));
		}

		// Now -- Start -- End => Not active
		for (long i = 5L; i <= 7L; i++) {
			editions.put(i, new EditionDetailsDTO().id(i).startDate(afterNow).endDate(afterNow.plusYears(1)));
		}

		// Start -- Now -- End => Not active
		for (long i = 8L; i <= 10L; i++) {
			editions.put(i, new EditionDetailsDTO().id(i).startDate(beforeNow).endDate(afterNow));
		}

		// Should only contain 8, 9, 10 as active courses (for all others there are different reasons as to why they
		// are not active)
		assertThat(homeController.getActiveCourses(courseSummaries, editions, visible, teacherIds))
				.containsExactlyInAnyOrder(8L, 9L, 10L);
	}

	@Test
	public void testGetCompletedTaskInCourse() {
		Long usedEditionId = db.getEditionRL().getId();

		// Mock latest or last edition values
		when(courseService.getLastStudentEditionForCourseOrLast(1L)).thenReturn(usedEditionId);
		when(courseService.getLastStudentEditionForCourseOrLast(2L)).thenReturn(usedEditionId + 1);
		when(courseService.getLastStudentEditionForCourseOrLast(3L)).thenReturn(usedEditionId + 2);

		// Save new editions and add a task completion to the second edition
		editionRepository.save(SCEdition.builder().id(usedEditionId + 1).build());
		Skill skill = db.createSkillInEditionHelper(usedEditionId + 2, true);
		Task task = Task.builder().name("Task").time(1).skill(skill).build();
		skill.getTasks().add(task);
		TaskCompletion completion = TaskCompletion.builder().task(task).person(db.getPerson()).build();
		db.getPerson().getTaskCompletions().add(completion);

		// Has completed more than one task in the edition previously saved in the db, none in the first saved edition
		// and exactly one in the second saved edition
		List<CourseSummaryDTO> courses = List.of(new CourseSummaryDTO().id(1L), new CourseSummaryDTO().id(2L),
				new CourseSummaryDTO().id(3L));
		Map<Long, Boolean> completedTask = homeController.getCompletedTaskInCourse(courses, db.getPerson());
		assertThat(completedTask.keySet()).containsExactlyInAnyOrder(1L, 2L, 3L);
		assertThat(completedTask.get(1L)).isTrue();
		assertThat(completedTask.get(2L)).isFalse();
		assertThat(completedTask.get(3L)).isTrue();
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
}
