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
package nl.tudelft.skills.controller.old;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.skills.TestSkillCircuitsApplication;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PersonControllerTest extends ControllerTest {

	//	private final PersonController personController;
	//	private final PersonRepository personRepository;
	//	private final TaskRepository taskRepository;
	//	private final TaskCompletionService taskCompletionService;
	//	private final PathPreferenceRepository pathPreferenceRepository;
	//	private final AuthorisationService authorisationService;
	//	private final RoleControllerApi roleApi;
	//	private final PlaylistService playlistService;
	//	private final PersonService personService;
	//
	//	@Autowired
	//	public PersonControllerTest(PersonRepository personRepository, TaskRepository taskRepository,
	//			TaskCompletionService taskCompletionService,
	//			PathPreferenceRepository pathPreferenceRepository,
	//			SkillRepository skillRepository,
	//			PathRepository pathRepository,
	//			AuthorisationService authorisationService,
	//			RoleControllerApi roleApi,
	//			PlaylistService playlistService,
	//			PersonService personService) {
	//		this.personRepository = personRepository;
	//		this.playlistService = playlistService;
	//		this.personController = new PersonController(taskRepository, personRepository, taskCompletionService,
	//				skillRepository, pathRepository, authorisationService, roleApi, playlistService,
	//				personService);
	//		this.taskRepository = taskRepository;
	//		this.taskCompletionService = taskCompletionService;
	//		this.pathPreferenceRepository = pathPreferenceRepository;
	//		this.personService = personService;
	//		this.authorisationService = authorisationService;
	//		this.roleApi = roleApi;
	//	}
	//
	//	@Test
	//	void setTasksCompletedForPerson() {
	//		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompleted).doesNotContain(db.getTaskDo10a(), db.getTaskRead10());
	//
	//		Person person = new Person();
	//		person.setId(db.getPerson().getId());
	//		personController.setTasksCompletedForPerson(person,
	//				List.of(db.getTaskDo10a().getId(), db.getTaskRead10().getId()));
	//
	//		List<Task> tasksCompletedAfter = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompletedAfter).contains(db.getTaskDo10a(), db.getTaskRead10());
	//	}
	//
	//	@ParameterizedTest
	//	@WithUserDetails("username")
	//	@CsvSource({ "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,false", ",true" })
	//	void updateTaskCompletedForPersonTrue(String role, boolean addRole) {
	//		completeTaskTrueHelper(role, addRole);
	//	}
	//
	//	@ParameterizedTest
	//	@WithUserDetails("teacher")
	//	@CsvSource({ "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	//	void updateTaskCompletedForPersonTrueDefaultTeacherRole(String role, boolean addRole) {
	//		completeTaskTrueHelper(role, addRole);
	//	}
	//
	//	/**
	//	 * Grouped functionality for tests checking a task being completed. It is used in multiple tests since
	//	 * different user details are needed.
	//	 */
	//	void completeTaskTrueHelper(String role, boolean addRole) {
	//		mockRole(roleApi, role);
	//
	//		// Return value is not checked, only call on method
	//		when(roleApi.addRole(any())).thenReturn(Mono.empty());
	//
	//		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompleted).doesNotContain(db.getTaskDo10a());
	//
	//		Set<Skill> skillsRevealed = db.getPerson().getSkillsRevealed();
	//		assertThat(skillsRevealed).doesNotContain(db.getSkillVariablesHidden());
	//
	//		Person person = new Person();
	//		person.setId(db.getPerson().getId());
	//		personController.updateTaskCompletedForPerson(person, db.getTaskRead10().getId(), true);
	//		TaskCompletedDTO taskCompletedDTO = personController.updateTaskCompletedForPerson(person,
	//				db.getTaskDo10a().getId(), true);
	//
	//		List<Task> tasksCompletedAfter = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompletedAfter).contains(db.getTaskDo10a());
	//		assertThat(taskCompletedDTO.getShowSkills()).containsExactly(db.getSkillVariablesHidden().getId());
	//
	//		Set<Skill> skillsRevealedAfter = db.getPerson().getSkillsRevealed();
	//		assertThat(skillsRevealedAfter).contains(db.getSkillVariablesHidden());
	//
	//		// Assert that a role was added or that no role was added
	//		if (addRole) {
	//			RoleCreateDTO roleCreateDTO = new RoleCreateDTO()
	//					.person(new PersonIdDTO().id(db.getPerson().getId()))
	//					.edition(new EditionIdDTO().id(db.getEditionRL().getId()))
	//					.type(RoleCreateDTO.TypeEnum.STUDENT);
	//
	//			verify(roleApi, times(2)).addRole(roleCreateDTO);
	//		} else {
	//			verify(roleApi, never()).addRole(any());
	//		}
	//	}
	//
	//	@Test
	//	void updateTaskCompletedForPersonFalse() {
	//		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompleted).contains(db.getTaskDo11ad());
	//
	//		Person person = new Person();
	//		person.setId(db.getPerson().getId());
	//		TaskCompletedDTO taskCompletedDTO = personController.updateTaskCompletedForPerson(person,
	//				db.getTaskDo11ad().getId(), false);
	//
	//		List<Task> tasksCompletedAfter = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompletedAfter).doesNotContain(db.getTaskDo11ad());
	//		assertThat(taskCompletedDTO.getShowSkills()).hasSize(0);
	//	}
	//
	//	/**
	//	 * Test to ensure a previously revealed skill is not sent to the front-end as it will result in a
	//	 * duplicated skill block
	//	 */
	//	@WithUserDetails("username")
	//	@Test
	//	void prevRevealedSkill() {
	//		mockRole(roleApi, "STUDENT");
	//		List<Task> tasksCompleted = db.getPerson().getTaskCompletions().stream()
	//				.map(TaskCompletion::getTask).toList();
	//		assertThat(tasksCompleted).doesNotContain(db.getTaskDo10a());
	//
	//		db.getPerson().getSkillsRevealed().add(db.getSkillVariablesHidden());
	//		personRepository.save(db.getPerson());
	//
	//		Set<Skill> skillsRevealed = db.getPerson().getSkillsRevealed();
	//		assertThat(skillsRevealed).contains(db.getSkillVariablesHidden());
	//
	//		Person person = new Person();
	//		person.setId(db.getPerson().getId());
	//		personController.updateTaskCompletedForPerson(person, db.getTaskRead10().getId(), true);
	//
	//		TaskCompletedDTO taskCompletedDTO = personController.updateTaskCompletedForPerson(person,
	//				db.getTaskDo10a().getId(), true);
	//		assertThat(taskCompletedDTO.getShowSkills()).isEqualTo(List.of());
	//
	//		Set<Skill> skillsRevealedAfter = db.getPerson().getSkillsRevealed();
	//		assertThat(skillsRevealedAfter).containsExactly(db.getSkillVariablesHidden());
	//	}
	//
	//	@Test
	//	void addAllTaskFromCurrentPath() {
	//		SCPerson person = db.getPerson();
	//
	//		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
	//				.edition(db.getEditionRL()).person(db.getPerson()).build();
	//		pathPreferenceRepository.save(pathPreference);
	//
	//		Task task = db.getTaskDo12ae();
	//		personController.addAllTaskFromCurrentPath(person, task);
	//
	//		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskRead12());
	//	}
	//
	//	@Test
	//	void resetSkill() throws IOException {
	//		SCPerson person = db.getPerson();
	//		person.getSkillsModified().add(db.getSkillImplication());
	//		person.getTasksAdded().add(db.getTaskDo12ae());
	//		personRepository.save(person);
	//
	//		Person authPerson = Person.builder().id(person.getId()).build();
	//
	//		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
	//				.edition(db.getEditionRL()).person(db.getPerson()).build();
	//		pathPreferenceRepository.save(pathPreference);
	//
	//		personController.resetSkill(authPerson, db.getSkillImplication().getId(),
	//				mock(HttpServletResponse.class));
	//
	//		//		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskRead12());
	//		assertThat(db.getPerson().getTasksAdded()).doesNotContain(db.getTaskDo12ae());
	//		assertThat(db.getPerson().getSkillsModified()).doesNotContain(db.getSkillImplication());
	//	}
	//
	//	@Test
	//	void addTaskToOwnPath() throws IOException {
	//		SCPerson person = db.getPerson();
	//
	//		Person authPerson = Person.builder().id(person.getId()).build();
	//
	//		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
	//				.edition(db.getEditionRL()).person(db.getPerson()).build();
	//		pathPreferenceRepository.save(pathPreference);
	//
	//		personController.addTaskToOwnPath(authPerson, db.getTaskDo12ae().getId());
	//
	//		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskRead12());
	//		assertThat(db.getPerson().getTasksAdded()).contains(db.getTaskDo12ae());
	//		assertThat(db.getPerson().getSkillsModified()).contains(db.getSkillImplication());
	//	}
	//
	//	@Test
	//	void removeTaskFromOwnPath() throws IOException {
	//		SCPerson person = db.getPerson();
	//
	//		Person authPerson = Person.builder().id(person.getId()).build();
	//
	//		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
	//				.edition(db.getEditionRL()).person(db.getPerson()).build();
	//		pathPreferenceRepository.save(pathPreference);
	//
	//		personController.removeTaskFromOwnPath(authPerson, db.getTaskRead12().getId());
	//
	//		assertThat(db.getPerson().getTasksAdded()).doesNotContain(db.getTaskRead12());
	//		assertThat(db.getPerson().getSkillsModified()).contains(db.getSkillImplication());
	//	}
}
