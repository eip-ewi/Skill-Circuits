// /*
//  * Skill Circuits
//  * Copyright (C) 2025 - Delft University of Technology
//  *
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License as
//  * published by the Free Software Foundation, either version 3 of the
//  * License, or (at your option) any later version.
//  *
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
//  */
// package nl.tudelft.skills.service.old;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.when;
//
// import java.util.*;
//
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.ValueSource;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;
//
// import nl.tudelft.labracore.api.EditionControllerApi;
// import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
// import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
// import nl.tudelft.labracore.lib.security.user.Person;
// import nl.tudelft.skills.TestSkillCircuitsApplication;
// import nl.tudelft.skills.model.*;
// import nl.tudelft.skills.model.labracore.SCPerson;
// import nl.tudelft.skills.repository.SkillRepository;
// import nl.tudelft.skills.repository.TaskCompletionRepository;
// import nl.tudelft.skills.repository.TaskRepository;
// import nl.tudelft.skills.repository.PersonRepository;
// import nl.tudelft.skills.test.TestDatabaseLoader;
// import reactor.core.publisher.Mono;
//
// @Transactional
// @SpringBootTest(classes = TestSkillCircuitsApplication.class)
// public class TaskCompletionServiceTest {
//
// 	private final EditionControllerApi editionApi;
// 	private final TaskCompletionRepository taskCompletionRepository;
// 	private final TaskCompletionService taskCompletionService;
// 	private final SkillRepository skillRepository;
//
// 	private final TestDatabaseLoader db;
// 	private TaskRepository taskRepository;
//
// 	@Autowired
// 	public TaskCompletionServiceTest(EditionControllerApi editionApi, PersonRepository personRepository,
// 			TaskCompletionRepository taskCompletionRepository, SkillRepository skillRepository,
// 			TaskRepository taskRepository, TestDatabaseLoader db) {
// 		this.editionApi = editionApi;
// 		this.taskCompletionRepository = taskCompletionRepository;
// 		this.skillRepository = skillRepository;
// 		this.taskRepository = taskRepository;
// 		this.db = db;
// 		this.taskCompletionService = new TaskCompletionService(taskCompletionRepository, personRepository,
// 				editionApi, this.skillRepository);
// 	}
//
// 	@Test
// 	public void testLatestTaskCompletionDoesNotExist() {
// 		// Reset the person to not have done any tasks yet
// 		db.resetTaskCompletions();
// 		SCPerson scPerson = db.getPerson();
//
// 		// The person did not complete any tasks yet
// 		Person person = Person.builder().id(scPerson.getId()).build();
// 		assertThat(taskCompletionService.latestTaskCompletion(person)).isNull();
// 		// If the person is null, should also return null
// 		assertThat(taskCompletionService.latestTaskCompletion(null)).isNull();
// 	}
//
// 	@Test
// 	public void testLatestTaskCompletionNullTimestamp() {
// 		// Reset the person to not have done any tasks yet
// 		db.resetTaskCompletions();
// 		SCPerson scPerson = db.getPerson();
//
// 		// Add a task completion which has null as the timestamp
// 		TaskCompletion timestampNull = TaskCompletion.builder().task(db.getTaskDo10a()).person(scPerson)
// 				.timestamp(null).build();
// 		taskCompletionRepository.save(timestampNull);
// 		scPerson.getTaskCompletions().add(timestampNull);
// 		db.getTaskDo10a().getCompletedBy().add(timestampNull);
//
// 		// The person has only completed a task with null as the timestamp,
// 		// this should not be considered the latest task completion
// 		// Therefore, null is expected
// 		Person person = Person.builder().id(scPerson.getId()).build();
// 		assertThat(taskCompletionService.latestTaskCompletion(person)).isNull();
// 	}
//
// 	@Test
// 	public void testLatestTaskCompletionReturnsCompletion() {
// 		// In the test database, the most recently completed task by the
// 		// saved person is taskRead11
// 		RegularTask expectedLastTask = db.getTaskRead11();
//
// 		// Assert that the latest task completion is the most recent one
// 		Person person = Person.builder().id(db.getPerson().getId()).build();
// 		assertThat(taskCompletionService.latestTaskCompletion(person)).isEqualTo(expectedLastTask);
// 	}
//
// 	@Test
// 	public void testGetLocationString() {
// 		RegularTask task = db.getTaskDo10a();
// 		SCEdition dbEdition = db.getEditionRL();
// 		SCCourse dbCourse = db.getCourseRL();
// 		EditionDetailsDTO editionDetailsDTO = new EditionDetailsDTO().id(dbEdition.getId()).name("edition")
// 				.course(new CourseSummaryDTO().id(dbCourse.getId()).name("Reasoning and Logic"));
//
// 		when(editionApi.getEditionById(db.getEditionRL().getId())).thenReturn(Mono.just(editionDetailsDTO));
//
// 		String expected = "Reasoning and Logic - Proof Techniques - Logic Basics - Variables";
// 		assertThat(taskCompletionService.getLocationString(task)).isEqualTo(expected);
// 	}
//
// 	@Test
// 	public void testAddTaskCompletion() {
// 		TaskCompletion completion = taskCompletionService.addTaskCompletion(db.getPerson(),
// 				db.getTaskDo10a());
// 		assertThat(taskCompletionRepository.getById(completion.getId())).isEqualTo(completion);
// 		assertThat(db.getPerson().getTaskCompletions()).contains(completion);
// 		assertThat(db.getTaskDo10a().getCompletedBy()).contains(completion);
// 	}
//
// 	@Test
// 	public void testDeleteTaskCompletionExists() {
// 		TaskCompletion taskCompletion = db.getCompleteRead12();
// 		assertThat(db.getTaskRead12().getCompletedBy()).containsExactly(taskCompletion);
// 		assertThat(db.getPerson().getTaskCompletions()).contains(taskCompletion);
// 		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isPresent();
//
// 		TaskCompletion deleted = taskCompletionService.deleteTaskCompletion(db.getPerson(),
// 				db.getTaskRead12());
// 		assertThat(deleted).isEqualTo(taskCompletion);
//
// 		// The TaskCompletion should be removed from the sets
// 		assertThat(db.getPerson().getTaskCompletions()).doesNotContain(taskCompletion);
// 		assertThat(db.getTaskRead10().getCompletedBy()).doesNotContain(taskCompletion);
//
// 		// The TaskCompletion should be removed from the repository
// 		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isEmpty();
// 	}
//
// 	@Test
// 	public void testDeleteTaskCompletionDoesNotExist() {
// 		TaskCompletion completion = taskCompletionService.deleteTaskCompletion(db.getPerson(),
// 				db.getTaskRead10());
// 		// Should return null since this completion was not saved
// 		assertThat(completion).isNull();
// 	}
//
// 	@Test
// 	public void testDeleteTaskCompletionsOfTask() {
// 		TaskCompletion taskCompletion = db.getCompleteRead12();
// 		assertThat(db.getTaskRead12().getCompletedBy()).containsExactly(taskCompletion);
// 		assertThat(db.getPerson().getTaskCompletions()).contains(taskCompletion);
// 		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isPresent();
//
// 		taskCompletionService.deleteTaskCompletionsOfTask(db.getTaskRead12());
//
// 		assertThat(db.getTaskRead12().getCompletedBy()).isEmpty();
// 		assertThat(db.getPerson().getTaskCompletions()).doesNotContain(taskCompletion);
// 		assertThat(taskCompletionRepository.findById(taskCompletion.getId())).isEmpty();
// 	}
//
// 	@Test
// 	public void determineSkillsDoneNoneCompleted() {
// 		SCPerson person = new SCPerson();
// 		person.setTaskCompletions(Set.of());
// 		Set<Skill> skillsDone = taskCompletionService.determineSkillsDone(Collections.emptySet(), person,
// 				db.getEditionRL().getId(), Collections.emptyList());
// 		assertThat(skillsDone).isEmpty();
// 	}
//
// 	@Test
// 	public void determineSkillsDoneSimple() {
// 		SCPerson person = new SCPerson();
// 		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
// 				.person(person).task(db.getTaskRead12()).build();
// 		TaskCompletion completion2 = TaskCompletion.builder().id(2L)
// 				.person(person).task(db.getTaskDo12ae()).build();
// 		person.setTaskCompletions(Set.of(completion1, completion2));
// 		Set<Skill> skillsDone = taskCompletionService.determineSkillsDone(Collections.emptySet(), person,
// 				db.getEditionRL().getId(), Collections.emptyList());
// 		assertEquals((Set.of(db.getSkillImplication())), skillsDone);
// 	}
//
// 	// TODO check adding CT to own path
//
// 	@ParameterizedTest
// 	@ValueSource(ints = { 0, 1 })
// 	public void determineSkillsDoneWithChoiceTask(int indexTaskToComplete) {
// 		SCPerson person = new SCPerson();
// 		// Which task is completed should not matter, so test for both separately
// 		RegularTask taskToComplete = db.getChoiceTaskBookOrVideo().getTasks().get(indexTaskToComplete)
// 				.getTask();
//
// 		// Complete one task in choice task
// 		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
// 				.person(person).task(taskToComplete).build();
// 		// Complete remaining regular tasks in skill
// 		TaskCompletion completion2 = TaskCompletion.builder().id(2L)
// 				.person(person).task(db.getTaskRead10()).build();
// 		TaskCompletion completion3 = TaskCompletion.builder().id(2L)
// 				.person(person).task(db.getTaskDo10a()).build();
// 		person.setTaskCompletions(Set.of(completion1, completion2, completion3));
//
// 		// The skill should be completed
// 		Set<Skill> skillsDone = taskCompletionService.determineSkillsDone(Collections.emptySet(), person,
// 				db.getEditionRL().getId(), Collections.emptyList());
// 		assertEquals((Set.of(db.getSkillVariables())), skillsDone);
// 	}
//
// 	@Test
// 	public void determineSkillsDoneCustomSkill() {
// 		SCPerson person = new SCPerson();
// 		TaskCompletion completion = TaskCompletion.builder().id(1L)
// 				.person(person).task(db.getTaskRead12()).build();
// 		person.setTaskCompletions(Set.of(completion));
// 		person.setTasksAdded(Set.of(db.getTaskRead12()));
// 		Set<Skill> skillsDone = taskCompletionService.determineSkillsDone(Set.of(db.getSkillImplication()),
// 				person,
// 				db.getEditionRL().getId(), Collections.emptyList());
// 		assertEquals((Set.of(db.getSkillImplication())), skillsDone);
// 	}
//
// 	@Test
// 	public void determineSkillsDoneWithPathPreference() {
// 		RegularTask task = db.getTaskRead12();
// 		task.setPaths(new HashSet<>(Set.of(db.getPathFinderPath())));
// 		taskRepository.save(task);
//
// 		SCPerson person = new SCPerson();
// 		TaskCompletion completion = TaskCompletion.builder().id(1L)
// 				.person(person).task(task).build();
// 		person.setTaskCompletions(Set.of(completion));
// 		person.setTasksAdded(Set.of(task));
//
// 		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath()).person(person)
// 				.edition(db.getEditionRL()).build();
//
// 		Set<Skill> skillsDone = taskCompletionService.determineSkillsDone(Set.of(db.getSkillImplication()),
// 				person,
// 				db.getEditionRL().getId(), List.of(pathPreference));
// 		assertEquals((Set.of(db.getSkillImplication())), skillsDone);
// 	}
//
// 	@Test
// 	public void testIsListOfTasksCompletedOnlyRegularTasksFalse() {
// 		// Only one completed
// 		assertFalse(
// 				taskCompletionService.isListOfTasksCompleted(List.of(db.getTaskDo10a(), db.getTaskRead12()),
// 						Set.of(db.getTaskDo10a())));
//
// 		// None completed
// 		assertFalse(
// 				taskCompletionService.isListOfTasksCompleted(List.of(db.getTaskDo10a(), db.getTaskRead12()),
// 						Set.of()));
//
// 		// One completed but not in list
// 		assertFalse(
// 				taskCompletionService.isListOfTasksCompleted(List.of(db.getTaskDo10a(), db.getTaskRead12()),
// 						Set.of(db.getTaskRead11())));
// 	}
//
// 	@Test
// 	public void testIsListOfTasksCompletedOnlyRegularTasksTrue() {
// 		// Same collection
// 		assertTrue(
// 				taskCompletionService.isListOfTasksCompleted(List.of(db.getTaskDo10a(), db.getTaskRead12()),
// 						Set.of(db.getTaskDo10a(), db.getTaskRead12())));
//
// 		// Additional task completed
// 		assertTrue(
// 				taskCompletionService.isListOfTasksCompleted(List.of(db.getTaskDo10a(), db.getTaskRead12()),
// 						Set.of(db.getTaskDo10a(), db.getTaskRead12(), db.getTaskRead11())));
// 	}
//
// 	@Test
// 	public void testIsListOfTasksCompletedChoiceTaskDoneTrue() {
// 		// One task in choice task completed
// 		assertTrue(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo()),
// 				Set.of(db.getTaskBook())));
// 		assertTrue(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo()),
// 				Set.of(db.getTaskVideo())));
//
// 		// Both tasks in choice task completed
// 		assertTrue(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo()),
// 				Set.of(db.getTaskBook(), db.getTaskVideo())));
//
// 		// Additional task completed
// 		assertTrue(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo()),
// 				Set.of(db.getTaskBook(), db.getTaskVideo(), db.getTaskRead12())));
// 	}
//
// 	@Test
// 	public void testIsListOfTasksCompletedTaskAndChoiceTaskFalse() {
// 		// Choice task not completed (once with additional completed task, once without)
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo(),
// 						db.getTaskDo10a()),
// 				Set.of(db.getTaskDo10a())));
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo(),
// 						db.getTaskDo10a()),
// 				Set.of(db.getTaskDo10a(), db.getTaskRead12())));
//
// 		// Regular task not completed (once with additional completed task, once without)
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo(),
// 						db.getTaskDo10a()),
// 				Set.of(db.getTaskBook())));
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo(),
// 						db.getTaskDo10a()),
// 				Set.of(db.getTaskBook(), db.getTaskRead12())));
// 	}
//
// 	@Test
// 	public void testIsListOfTasksCompletedTaskAndChoiceTaskTrue() {
// 		// Once with additional completed task, once without
// 		assertTrue(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo(),
// 						db.getTaskDo10a()),
// 				Set.of(db.getTaskBook(), db.getTaskDo10a())));
// 		assertTrue(taskCompletionService.isListOfTasksCompleted(
// 				List.of(db.getChoiceTaskBookOrVideo(), db.getTaskBook(), db.getTaskVideo(),
// 						db.getTaskDo10a()),
// 				Set.of(db.getTaskBook(), db.getTaskDo10a(), db.getTaskRead12())));
// 	}
//
// 	@Test
// 	public void testIsListOfTasksCompletedTooLittleTasksInChoiceTaskDone() {
// 		// Check that completing a task in a choice task that requires more tasks to be done, does not make the
// 		// choice task completed
//
// 		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
// 		RegularTask newTask = db.createTaskBySkillAndName(db.getSkillVariables(), "Additional task");
// 		choiceTask.getTasks().add(newTask.getTaskInfo());
// 		choiceTask.setMinTasks(2);
// 		taskRepository.save(choiceTask);
// 		taskRepository.save(newTask);
//
// 		// Only one task in choice task completed and two are needed
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(choiceTask, db.getTaskBook(), db.getTaskVideo(), newTask),
// 				Set.of(db.getTaskBook())));
//
// 		// No task completed
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(choiceTask, db.getTaskBook(), db.getTaskVideo(), newTask),
// 				Set.of()));
//
// 		// Task outside the choice task completed
// 		assertFalse(taskCompletionService.isListOfTasksCompleted(
// 				List.of(choiceTask, db.getTaskBook(), db.getTaskVideo(), newTask),
// 				Set.of(db.getTaskRead12())));
// 	}
//
// 	@Test
// 	public void determineEmptySkills() {
// 		SCPerson person = new SCPerson();
// 		Set<Skill> skillsDone = taskCompletionService.determineEmptySkills(Collections.emptySet(),
// 				Collections.emptyList(), person, db.getEditionRL().getId());
// 		assertEquals((Set.of(db.getSkillProofOutline(), db.getSkillAssumption(),
// 				db.getSkillGeneralisationPractice(), db.getSkillDividingIntoCases(),
// 				db.getSkillCasesPractice(), db.getSkillContradictionPractice(),
// 				db.getSkillNegateImplications(), db.getSkillContrapositivePractice(),
// 				db.getSkillTransitiveProperty(), db.getSkillInductionPractice(),
// 				db.getSkillVariablesHidden())), skillsDone);
// 	}
//
// 	@Test
// 	public void addCompletedEmptySkills() {
// 		SCPerson person = new SCPerson();
// 		TaskCompletion completion1 = TaskCompletion.builder().id(1L)
// 				.person(person).task(db.getTaskRead12()).build();
// 		TaskCompletion completion2 = TaskCompletion.builder().id(2L)
// 				.person(person).task(db.getTaskDo12ae()).build();
// 		person.setTaskCompletions(Set.of(completion1, completion2));
// 		Set<Skill> skillsDone = new HashSet<>(Arrays.asList(db.getSkillImplication()));
// 		taskCompletionService.addCompletedEmptySkills(skillsDone,
// 				Set.of(db.getSkillProofOutline(), db.getSkillAssumption(),
// 						db.getSkillGeneralisationPractice(), db.getSkillDividingIntoCases(),
// 						db.getSkillCasesPractice(), db.getSkillContradictionPractice(),
// 						db.getSkillNegateImplications(), db.getSkillContrapositivePractice(),
// 						db.getSkillTransitiveProperty(), db.getSkillInductionPractice()),
// 				db.getSkillAssumption());
// 		assertEquals((Set.of(db.getSkillImplication(), db.getSkillAssumption())), skillsDone);
// 	}
// }
