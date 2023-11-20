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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.*;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.EditionService;
import reactor.core.publisher.Flux;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class HomeControllerTest extends ControllerTest {

	private final HomeController homeController;
	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;
	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;
	private final EditionRepository editionRepository;
	private final PersonRepository personRepository;
	private final EditionService editionService;
	private final CourseService courseService;
	private final PathPreferenceRepository pathPreferenceRepository;

	@Autowired
	public HomeControllerTest(EditionControllerApi editionApi, RoleControllerApi roleApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository,
			SkillRepository skillRepository, TaskRepository taskRepository, PersonRepository personRepository,
			EditionService editionService,
			PathPreferenceRepository pathPreferenceRepository) {
		this.editionApi = editionApi;
		this.roleApi = roleApi;
		this.editionRepository = editionRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
		this.personRepository = personRepository;
		this.editionService = editionService;
		this.courseService = mock(CourseService.class);
		this.pathPreferenceRepository = pathPreferenceRepository;
		this.homeController = new HomeController(editionApi, roleApi, editionRepository, moduleRepository,
				personRepository, skillRepository, taskRepository, editionService, courseService,
				pathPreferenceRepository);
	}

	@Test
	void getLoginPage() {
		assertThat(homeController.getLoginPage()).isEqualTo("login");
	}

	@Test
	@SuppressWarnings("unchecked")
	void getHomePage() {
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.saveAndFlush(edition);

		CourseSummaryDTO course = new CourseSummaryDTO().id(randomId());

		when(editionApi.getAllEditionsActiveAtDate(any()))
				.thenReturn(Flux.just(new EditionSummaryDTO().id(edition.getId())));
		when(editionApi.getEditionsById(anyList()))
				.thenReturn(Flux.just(new EditionDetailsDTO().id(edition.getId()).course(course)));

		homeController.getHomePage(null, model);

		assertThat((List<CourseSummaryDTO>) model.getAttribute("courses")).containsExactly(course);
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

		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(3);
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
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId())).isEqualTo(7);
	}

	@Test
	void getCompletedSkillsAllCompleted() {
		List<CourseSummaryDTO> courses = new ArrayList<>(
				Arrays.asList(new CourseSummaryDTO().id(db.getCourseRL().getId())));
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

		when(courseService.getLastStudentEditionForCourseOrLast(anyLong()))
				.thenReturn(db.getEditionRL().getId());

		// All the skills are completed in the course
		Map<Long, Integer> courseCompletedSkills = homeController.getCompletedSkillsPerCourse(courses,
				person);
		assertTrue(courseCompletedSkills.entrySet().stream().anyMatch(e -> e.getValue() > 0));
		assertThat(courseCompletedSkills.get(db.getCourseRL().getId()))
				.isEqualTo(skillRepository.findAll().size());
	}

}
