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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.*;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class HomeControllerTest extends ControllerTest {

	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;
	private final PersonControllerApi personApi;
	private final CourseControllerApi courseApi;
	private final EditionRepository editionRepository;
	private final HomeController homeController;

	@Autowired
	public HomeControllerTest(EditionControllerApi editionApi, PersonControllerApi personApi,
			EditionRepository editionRepository, RoleControllerApi roleApi,
			CourseControllerApi courseApi, HomeController homeController) {
		this.editionApi = editionApi;
		this.personApi = personApi;
		this.editionRepository = editionRepository;
		this.roleApi = roleApi;
		this.courseApi = courseApi;
		this.homeController = homeController;
	}

	@Test
	void getLoginPage() {
		assertThat(homeController.getLoginPage()).isEqualTo("login");
	}

	@Test
	@SuppressWarnings("unchecked")
	@WithAnonymousUser
	void getHomePageExampleNotLoggedIn() {
		// Test setup:
		// - Course one:
		// --- Edition one: Currently active, visible
		// - User is not logged in
		// Desired result:
		// => Course is in "availableActive", with the edition as default edition

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.saveAndFlush(edition);

		CourseSummaryDTO course = new CourseSummaryDTO().id(randomId());

		LocalDateTime localDateTime = LocalDateTime.now();
		when(editionApi.getEditionsById(eq(List.of(edition.getId()))))
				.thenReturn(Flux.just(new EditionDetailsDTO().id(edition.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course)));
		when(courseApi.getCourseById(course.getId())).thenReturn(Mono.just(new CourseDetailsDTO()
				.editions(List.of(new EditionSummaryDTO().id(edition.getId())))));

		homeController.getHomePage(null, model);

		assertModelAttributesEmpty(Set.of("availableFinished", "ownActive", "ownFinished", "managed"));
		assertThat((List<CourseSummaryDTO>) model.getAttribute("availableActive")).containsExactly(course);
		assertThat((Map<Long, Long>) model.getAttribute("editionPerCourse"))
				.isEqualTo(Map.of(course.getId(), edition.getId()));
	}

	@ParameterizedTest
	@SuppressWarnings("unchecked")
	@WithUserDetails("username")
	@CsvSource({ "STUDENT,own,available", "TA,own,available", "HEAD_TA,own,available", "null,available,own" })
	void getHomePageExampleDifferentRoles(String role, String grouping, String emptyGrouping) {
		// Test setup:
		// - Course one:
		// ---- Edition one: Newer, active, role parameter (student, TA, head TA, no role)
		// ---- Edition two: Older, not active, role parameter (student, TA, head TA, no role)
		// - Course/edition two: Not active, role parameter (student, TA, head TA, no role)
		// Desired result:
		// => Course two in "grouping parameter + "Finished"", course one in "grouping parameter + "Active""
		// => Meaning, in "own" if a role is assigned, and in "available" if none is assigned

		Long usedEditionId = db.getEditionRL().getId();
		SCEdition edition1 = SCEdition.builder().id(usedEditionId + 1L).isVisible(true).build();
		editionRepository.saveAndFlush(edition1);
		SCEdition edition2 = SCEdition.builder().id(usedEditionId + 2L).isVisible(true).build();
		editionRepository.saveAndFlush(edition2);
		SCEdition edition3 = SCEdition.builder().id(usedEditionId + 3L).isVisible(true).build();
		editionRepository.saveAndFlush(edition3);

		CourseSummaryDTO course1 = new CourseSummaryDTO().id(1L);
		CourseSummaryDTO course2 = new CourseSummaryDTO().id(2L);

		// Mock responses for editions and courses
		LocalDateTime localDateTime = LocalDateTime.now();
		List<EditionDetailsDTO> editions = List.of(
				new EditionDetailsDTO().id(edition1.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course1),
				new EditionDetailsDTO().id(edition2.getId())
						.startDate(localDateTime.minusYears(2)).endDate(localDateTime.minusYears(1))
						.course(course1),
				new EditionDetailsDTO().id(edition3.getId())
						.startDate(localDateTime.minusYears(2)).endDate(localDateTime.minusYears(1))
						.course(course2));
		Person person = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Setup for roles
		Map<SCEdition, String> roleMap = new HashMap<>();
		if (role.equals("null")) {
			roleMap.put(edition1, null);
			roleMap.put(edition2, null);
			roleMap.put(edition3, null);
		} else {
			roleMap = Map.of(edition1, role, edition2, role, edition3, role);
		}

		mockCourseEditionProperties(
				editions,
				Map.of(1L, Set.of(editions.get(0), editions.get(1)), 2L, Set.of(editions.get(2))),
				roleMap,
				person.getId());

		homeController.getHomePage(person, model);

		assertThat((List<CourseSummaryDTO>) model.getAttribute(grouping + "Finished"))
				.containsExactly(course2);
		assertThat((List<CourseSummaryDTO>) model.getAttribute(grouping + "Active"))
				.containsExactly(course1);
		assertModelAttributesEmpty(Set.of(emptyGrouping + "Active", emptyGrouping + "Finished", "managed"));
		assertThat((Map<Long, Long>) model.getAttribute("editionPerCourse"))
				.isEqualTo(Map.of(1L, edition1.getId(), 2L, edition3.getId()));
	}

	@Test
	@SuppressWarnings("unchecked")
	@WithUserDetails("username")
	void getHomePageExampleManaged() {
		// Test setup:
		// - Course one:
		// --- Edition one: Older, student
		// --- Edition two: Newer, teacher
		// Desired result:
		// => The course should be added to managed courses. The default edition does not matter here, but with the
		// current implementation it is edition two since it is newer.

		Long usedEditionId = db.getEditionRL().getId();
		SCEdition edition1 = SCEdition.builder().id(usedEditionId + 1L).isVisible(true).build();
		editionRepository.saveAndFlush(edition1);
		SCEdition edition2 = SCEdition.builder().id(usedEditionId + 2L).isVisible(true).build();
		editionRepository.saveAndFlush(edition2);

		Long courseId = db.getCourseRL().getId();
		CourseSummaryDTO course = new CourseSummaryDTO().id(courseId);

		// Mock responses for editions and courses
		LocalDateTime localDateTime = LocalDateTime.now();
		List<EditionDetailsDTO> editions = List.of(
				new EditionDetailsDTO().id(edition1.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course),
				new EditionDetailsDTO().id(edition2.getId())
						.startDate(localDateTime.plusYears(1)).endDate(localDateTime.plusYears(2))
						.course(course));
		Person person = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		mockCourseEditionProperties(
				editions,
				Map.of(courseId, Set.of(editions.get(0), editions.get(1))),
				Map.of(edition1, "STUDENT", edition2, "TEACHER"),
				person.getId());

		homeController.getHomePage(person, model);

		assertThat((List<CourseSummaryDTO>) model.getAttribute("managed")).containsExactly(course);
		assertModelAttributesEmpty(
				Set.of("ownFinished", "availableActive", "ownActive", "availableFinished"));
		// The default edition should not be used in the frontend, since it is a managed edition
		assertThat((Map<Long, Long>) model.getAttribute("editionPerCourse"))
				.isEqualTo(Map.of(courseId, edition2.getId()));
	}

	@Test
	@SuppressWarnings("unchecked")
	@WithUserDetails("username")
	void getHomePageExampleHeadTA() {
		// Test setup:
		// - Course one:
		// --- Edition one: Older, student, visible, has task completions
		// --- Edition two: Newer, head TA, not visible, no task completions
		// Desired result:
		// => Course should be categorized as "ownActive" with the head TA edition as default. The task completions
		// should not matter in the categorization, only the role.

		// There are task completions in this edition
		SCEdition editionStudent = db.getEditionRL();
		editionStudent.setVisible(true);
		editionRepository.saveAndFlush(editionStudent);

		SCEdition editionHeadTA = SCEdition.builder().id(db.getEditionRL().getId() + 1L).isVisible(false)
				.build();
		editionRepository.saveAndFlush(editionHeadTA);

		Long courseId = db.getCourseRL().getId();
		CourseSummaryDTO course = new CourseSummaryDTO().id(courseId);

		// Mock responses for editions and courses
		LocalDateTime localDateTime = LocalDateTime.now();
		List<EditionDetailsDTO> editions = List.of(
				new EditionDetailsDTO().id(editionStudent.getId())
						.startDate(localDateTime.minusYears(3)).endDate(localDateTime.minusYears(2))
						.course(course),
				new EditionDetailsDTO().id(editionHeadTA.getId())
						.startDate(localDateTime.minusYears(1)).endDate(localDateTime.plusYears(1))
						.course(course));

		Person person = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		mockCourseEditionProperties(
				editions,
				Map.of(courseId, Set.of(editions.get(0), editions.get(1))),
				Map.of(editionStudent, "STUDENT", editionHeadTA, "HEAD_TA"),
				person.getId());

		homeController.getHomePage(person, model);

		assertThat((List<CourseSummaryDTO>) model.getAttribute("ownActive")).containsExactly(course);
		assertModelAttributesEmpty(Set.of("ownFinished", "availableActive", "availableFinished", "managed"));
		assertThat((Map<Long, Long>) model.getAttribute("editionPerCourse"))
				.isEqualTo(Map.of(courseId, editionHeadTA.getId()));
	}

	@SuppressWarnings("unchecked")
	void assertModelAttributesEmpty(Set<String> attributes) {
		for (String attribute : attributes) {
			assertThat((List<CourseSummaryDTO>) model.getAttribute(attribute)).isEmpty();
		}
	}

	void mockCourseEditionProperties(List<EditionDetailsDTO> editions,
			Map<Long, Set<EditionDetailsDTO>> courseToEditions,
			Map<SCEdition, String> editionToRole,
			Long personId) {
		List<Long> editionIds = editions.stream().map(EditionDetailsDTO::getId).toList();

		// Mock editions properties of courses
		when(editionApi.getEditionsById(editionIds)).thenReturn(Flux.fromIterable(editions));
		for (Map.Entry<Long, Set<EditionDetailsDTO>> entry : courseToEditions.entrySet()) {
			when(courseApi.getCourseById(entry.getKey())).thenReturn(Mono.just(new CourseDetailsDTO()
					.editions(entry.getValue().stream()
							.map(ed -> new EditionSummaryDTO().id(ed.getId()).startDate(ed.getStartDate())
									.endDate(ed.getEndDate()))
							.toList())));
		}

		// Mock roles of user
		Set<RoleDetailsDTO> roles = new HashSet<>();
		Map<Long, String> editionIdToRole = new HashMap<>();
		for (Map.Entry<SCEdition, String> entry : editionToRole.entrySet()) {
			Long editionId = entry.getKey().getId();
			editionIdToRole.put(editionId, entry.getValue());

			mockRoleForEdition(roleApi, entry.getValue(), editionId);

			if (entry.getValue() != null) {
				roles.add(getRoleDetails(editionId, RoleDetailsDTO.TypeEnum.valueOf(entry.getValue())));
			}
		}
		when(roleApi.getRolesById(eq(new HashSet<>(editionIds)), anySet()))
				.thenReturn(Flux.fromIterable(roles));
		mockRolesForEditions(editionIdToRole, personId);
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

	void mockRolesForEditions(Map<Long, String> roles, Long personId) {
		List<RoleEditionDetailsDTO> roleDTOS = new ArrayList<>();
		for (Map.Entry<Long, String> entry : roles.entrySet()) {
			if (entry.getValue() != null) {
				roleDTOS.add(new RoleEditionDetailsDTO()
						.id(new Id().editionId(entry.getKey()).personId(db.getPerson().getId()))
						.type(RoleEditionDetailsDTO.TypeEnum.valueOf(entry.getValue())));
			}
		}
		when(personApi.getRolesForPerson(eq(personId))).thenReturn(Flux.fromIterable(roleDTOS));
	}

}
