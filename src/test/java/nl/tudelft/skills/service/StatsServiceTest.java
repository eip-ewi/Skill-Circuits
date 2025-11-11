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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.skills.dto.stats.StudentStatsDTO;
import nl.tudelft.skills.dto.stats.TaskStatsDTO;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import reactor.core.publisher.Flux;

@Transactional
@SpringBootTest()
public class StatsServiceTest {

	private final StatsService statsService;
	private final TaskRepository taskRepository;
	private final RoleControllerApi roleControllerApi;

	private final PathPreferenceRepository pathPreferenceRepository;
	private final ClickedLinkRepository clickedLinkRepository;
	private final PersonRepository personRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final PersonControllerApi personControllerApi;

	public StatsServiceTest() {
		this.taskRepository = mock(TaskRepository.class);
		this.roleControllerApi = mock(RoleControllerApi.class);
		this.pathPreferenceRepository = mock(PathPreferenceRepository.class);
		this.clickedLinkRepository = mock(ClickedLinkRepository.class);
		this.personRepository = mock(PersonRepository.class);
		this.taskCompletionRepository = mock(TaskCompletionRepository.class);
		this.personControllerApi = mock(PersonControllerApi.class);

		statsService = new StatsService(
				this.taskRepository, this.clickedLinkRepository, this.pathPreferenceRepository,
				this.taskCompletionRepository,
				this.roleControllerApi,
				this.personRepository, this.personControllerApi);
	}

	@Test
	public void emptyTaskStats() {
		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong())).thenReturn(List.of());
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);
		assertThat(taskStats).isEmpty();
	}

	private Skill skillBuilder() {
		SCModule moduleProofTechniques = SCModule.builder()
				.name("Proof Techniques")
				.build();
		Submodule submoduleLogicBasics = Submodule.builder()
				.name("Logic Basics")
				.module(moduleProofTechniques)
				.column(0)
				.build();

		return Skill.builder()
				.name("Implication")
				.submodule(submoduleLogicBasics)
				.column(0)
				.build();
	}

	private Skill skillWithCheckpointBuilder(String checkpointName, LocalDateTime deadline) {
		Checkpoint checkpoint = Checkpoint.builder().name(checkpointName).deadline(deadline).build();
		Skill skill = skillBuilder();
		skill.setCheckpoint(checkpoint);
		return skill;
	}

	@Test
	public void noActivityTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder()
				.skill(skillWithCheckpointBuilder("Checkpoint 1", LocalDateTime.of(2025, 1, 1, 10, 10)))
				.taskInfo(read12Info)
				.build();
		read12Info.setTask(taskRead12);
		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"Checkpoint 1", "Logic Basics", "Proof Techniques", 0, 0, 0, 0, 0)));
	}

	@Test
	public void noParentTaskInfoTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.build();
		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Unknown", "Unknown",
				"Unknown", "Unknown", 0, 0, 0, 0, 0)));
	}

	@Test
	public void multipleTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.build();
		read12Info.setTask(taskRead12);

		TaskInfo do12aeInfo = TaskInfo.builder().id(2L).name("Do exercise 1.2a-e")
				.time(10).type(TaskType.EXERCISE).build();
		TaskInfo do11adInfo = TaskInfo.builder().id(3L).name("Do exercise 1.1a-d").time(10).build();
		ChoiceTask choiceTask = ChoiceTask.builder().skill(skillBuilder())
				.tasks(List.of(do12aeInfo, do11adInfo))
				.build();
		do12aeInfo.setChoiceTask(choiceTask);
		do11adInfo.setChoiceTask(choiceTask);

		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12, choiceTask));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(3, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(
				new TaskStatsDTO(1L, "Read chapter 1.2", "Implication", "No checkpoint", "Logic Basics",
						"Proof Techniques", 0, 0, 0, 0, 0),
				new TaskStatsDTO(2L, "Do exercise 1.2a-e", "Implication", "No checkpoint", "Logic Basics",
						"Proof Techniques", 0, 0, 0, 0, 0),
				new TaskStatsDTO(3L, "Do exercise 1.1a-d", "Implication", "No checkpoint", "Logic Basics",
						"Proof Techniques", 0, 0, 0, 0, 0)));

	}

	@Test
	public void completionTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING)
				.completedBy(
						Set.of(TaskCompletion.builder().person(SCPerson.builder().id(1L).build()).build(),
								TaskCompletion.builder().person(SCPerson.builder().id(2L).build()).build(),
								TaskCompletion.builder().person(SCPerson.builder().id(3L).build()).build()))
				.build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.build();
		read12Info.setTask(taskRead12);

		RoleDetailsDTO role1 = new RoleDetailsDTO();
		role1.setType(RoleDetailsDTO.TypeEnum.TEACHER);
		role1.setPerson(new PersonSummaryDTO().id(1L));
		RoleDetailsDTO role2 = new RoleDetailsDTO();
		role2.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role2.setPerson(new PersonSummaryDTO().id(2L));
		RoleDetailsDTO role3 = new RoleDetailsDTO();
		role3.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role3.setPerson(new PersonSummaryDTO().id(3L));

		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());
		when(roleControllerApi.getRolesById(anySet(), anySet())).thenReturn(Flux.just(role1, role2, role3));
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"No checkpoint", "Logic Basics", "Proof Techniques", 2, 0, 0, 0, 0)));
	}

	@Test
	public void onPathTaskStats() {
		Path pathFinderPath = Path.builder().id(1L).build();
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.paths(Set.of(pathFinderPath))
				.build();
		read12Info.setTask(taskRead12);

		PathPreference pathPreference1 = PathPreference.builder().path(pathFinderPath)
				.person(SCPerson.builder().id(1L).build())
				.build();
		PathPreference pathPreference2 = PathPreference.builder().path(pathFinderPath)
				.person(SCPerson.builder().id(2L).build())
				.build();
		PathPreference pathPreference3 = PathPreference.builder().path(pathFinderPath)
				.person(SCPerson.builder().id(3L).build())
				.build();

		RoleDetailsDTO role1 = new RoleDetailsDTO();
		role1.setType(RoleDetailsDTO.TypeEnum.TEACHER);
		role1.setPerson(new PersonSummaryDTO().id(1L));
		RoleDetailsDTO role2 = new RoleDetailsDTO();
		role2.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role2.setPerson(new PersonSummaryDTO().id(2L));
		RoleDetailsDTO role3 = new RoleDetailsDTO();
		role3.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role3.setPerson(new PersonSummaryDTO().id(3L));

		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(roleControllerApi.getRolesById(Set.of(1L), Set.of(1L, 2L, 3L)))
				.thenReturn(Flux.just(role1, role2, role3));
		when(pathPreferenceRepository.findAllByPathId(1L))
				.thenReturn(List.of(pathPreference1, pathPreference2, pathPreference3));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());

		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"No checkpoint", "Logic Basics", "Proof Techniques", 0, 2, 0, 0, 0)));
	}

	@Test
	public void clickedLinkTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.build();
		read12Info.setTask(taskRead12);

		ClickedLink clickedLink1person1 = ClickedLink.builder().person(SCPerson.builder().id(1L).build())
				.build();
		ClickedLink clickedLink2person1 = ClickedLink.builder().person(SCPerson.builder().id(1L).build())
				.build();
		ClickedLink clickedLink3person2 = ClickedLink.builder().person(SCPerson.builder().id(2L).build())
				.build();
		ClickedLink clickedLink4person3 = ClickedLink.builder().person(SCPerson.builder().id(3L).build())
				.build();

		RoleDetailsDTO role1 = new RoleDetailsDTO();
		role1.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role1.setPerson(new PersonSummaryDTO().id(1L));
		RoleDetailsDTO role2 = new RoleDetailsDTO();
		role2.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role2.setPerson(new PersonSummaryDTO().id(2L));
		RoleDetailsDTO role3 = new RoleDetailsDTO();
		role3.setType(RoleDetailsDTO.TypeEnum.TEACHER);
		role3.setPerson(new PersonSummaryDTO().id(3L));

		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of(clickedLink1person1, clickedLink2person1, clickedLink3person2, clickedLink4person3));
		when(roleControllerApi.getRolesById(Set.of(1L), Set.of(1L, 2L, 3L)))
				.thenReturn(Flux.just(role1, role2, role3));
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"No checkpoint", "Logic Basics", "Proof Techniques", 0, 0, 3, 2, 0)));
	}

	@Test
	public void clickedLinkCompletedTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						TaskCompletion.builder().person(SCPerson.builder().id(1L).build()).build(),
						TaskCompletion.builder().person(SCPerson.builder().id(3L).build()).build(),
						TaskCompletion.builder().person(SCPerson.builder().id(4L).build()).build()))
				.build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.build();
		read12Info.setTask(taskRead12);

		ClickedLink clickedLink1person1 = ClickedLink.builder().person(SCPerson.builder().id(1L).build())
				.build();
		ClickedLink clickedLink2person1 = ClickedLink.builder().person(SCPerson.builder().id(1L).build())
				.build();
		ClickedLink clickedLink3person2 = ClickedLink.builder().person(SCPerson.builder().id(2L).build())
				.build();
		ClickedLink clickedLink4person3 = ClickedLink.builder().person(SCPerson.builder().id(3L).build())
				.build();

		RoleDetailsDTO role1 = new RoleDetailsDTO();
		role1.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role1.setPerson(new PersonSummaryDTO().id(1L));
		RoleDetailsDTO role2 = new RoleDetailsDTO();
		role2.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role2.setPerson(new PersonSummaryDTO().id(2L));
		RoleDetailsDTO role3 = new RoleDetailsDTO();
		role3.setType(RoleDetailsDTO.TypeEnum.TEACHER);
		role3.setPerson(new PersonSummaryDTO().id(3L));
		RoleDetailsDTO role4 = new RoleDetailsDTO();
		role4.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role4.setPerson(new PersonSummaryDTO().id(4L));

		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of(clickedLink1person1, clickedLink2person1, clickedLink3person2, clickedLink4person3));
		when(roleControllerApi.getRolesById(Set.of(1L), Set.of(1L, 2L, 3L)))
				.thenReturn(Flux.just(role1, role2, role3));
		when(roleControllerApi.getRolesById(Set.of(1L), Set.of(1L, 3L, 4L)))
				.thenReturn(Flux.just(role1, role3, role4));
		List<TaskStatsDTO> taskStats = statsService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"No checkpoint", "Logic Basics", "Proof Techniques", 2, 0, 3, 2, 1)));
	}

	@Test
	public void noStudentsStudentStats() {
		when(personControllerApi.getPeopleByEditionAndRoleType(1L, "STUDENT")).thenReturn(Flux.just());
		List<StudentStatsDTO> studentStats = statsService.teacherStatsStudentLevel(1L);

		assertThat(studentStats).isEqualTo(List.of());
	}

	@Test
	public void noCompletionStudentStats() {
		SCPerson student = SCPerson.builder().id(1L).build();
		PersonSummaryDTO personSummary = new PersonSummaryDTO();
		personSummary.setUsername("student");
		personSummary.setId(1L);

		when(personControllerApi.getPeopleByEditionAndRoleType(1L, "STUDENT"))
				.thenReturn(Flux.just(personSummary));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(student));
		when(taskCompletionRepository.findAllByPersonAndEditionId(any(), anyLong())).thenReturn(Set.of());

		List<StudentStatsDTO> studentStats = statsService.teacherStatsStudentLevel(1L);

		assertThat(studentStats).isEqualTo(
				List.of(new StudentStatsDTO(1L, "student", "Path not chosen", 0, null, "No activity",
						"No checkpoint started")));
	}

	@Test
	public void pathChosenStudentStats() {
		Path path1 = Path.builder().id(1L).name("Path 1").build();
		Path path2 = Path.builder().id(2L).name("Path 2").build();

		PathPreference pathPreference1 = PathPreference.builder().id(1L).path(path1)
				.edition(SCEdition.builder().id(1L).build())
				.build();
		PathPreference pathPreference2 = PathPreference.builder().id(2L).path(path2)
				.edition(SCEdition.builder().id(2L).build())
				.build();
		SCPerson student = SCPerson.builder().id(2L).pathPreferences(Set.of(pathPreference2, pathPreference1))
				.build();

		PersonSummaryDTO personSummary = new PersonSummaryDTO();
		personSummary.setUsername("student");
		personSummary.setId(2L);

		when(personControllerApi.getPeopleByEditionAndRoleType(1L, "STUDENT"))
				.thenReturn(Flux.just(personSummary));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(student));
		when(taskCompletionRepository.findAllByPersonAndEditionId(any(), anyLong())).thenReturn(Set.of());

		List<StudentStatsDTO> studentStats = statsService.teacherStatsStudentLevel(1L);

		assertThat(studentStats).isEqualTo(
				List.of(new StudentStatsDTO(2L, "student", "Path 1", 0, null, "No activity",
						"No checkpoint started")));
	}

	@Test
	public void multipleCompletionStudentStats() {
		SCPerson student = SCPerson.builder().id(1L).build();

		TaskCompletion taskCompletion1 = TaskCompletion.builder().person(student)
				.timestamp(LocalDateTime.of(2024, 11, 1, 10, 10)).build();
		TaskInfo taskInfo1 = TaskInfo.builder().id(1L).name("Task 1")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						taskCompletion1))
				.build();
		RegularTask task1 = RegularTask.builder()
				.skill(skillWithCheckpointBuilder("Checkpoint 1", LocalDateTime.of(2025, 1, 1, 10, 10)))
				.taskInfo(taskInfo1)
				.build();
		taskInfo1.setTask(task1);
		taskCompletion1.setTask(taskInfo1);

		TaskCompletion taskCompletion2 = TaskCompletion.builder().person(student)
				.timestamp(LocalDateTime.of(2024, 8, 1, 10, 10)).build();
		TaskInfo taskInfo2 = TaskInfo.builder().id(2L).name("Task 2")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						taskCompletion2))
				.build();
		ChoiceTask task2 = ChoiceTask.builder()
				.skill(skillWithCheckpointBuilder("Checkpoint 2", LocalDateTime.of(2024, 1, 1, 10, 10)))
				.tasks(List.of(taskInfo2))
				.build();
		taskInfo2.setChoiceTask(task2);
		taskCompletion2.setTask(taskInfo2);

		TaskCompletion taskCompletion3 = TaskCompletion.builder().person(student)
				.timestamp(LocalDateTime.of(2025, 2, 1, 10, 10)).build();
		TaskInfo taskInfo3 = TaskInfo.builder().id(3L).name("Task 3")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						taskCompletion3))
				.build();
		ChoiceTask task3 = ChoiceTask.builder().skill(skillBuilder()).tasks(List.of(taskInfo3))
				.build();
		taskInfo3.setChoiceTask(task3);
		taskCompletion3.setTask(taskInfo3);

		PersonSummaryDTO personSummary = new PersonSummaryDTO();
		personSummary.setUsername("student");
		personSummary.setId(1L);

		when(personControllerApi.getPeopleByEditionAndRoleType(1L, "STUDENT"))
				.thenReturn(Flux.just(personSummary));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(student));
		when(taskCompletionRepository.findAllByPersonAndEditionId(any(), anyLong()))
				.thenReturn(Set.of(taskCompletion1, taskCompletion2, taskCompletion3));

		List<StudentStatsDTO> studentStats = statsService.teacherStatsStudentLevel(1L);

		assertThat(studentStats).isEqualTo(List.of(new StudentStatsDTO(1L, "student", "Path not chosen", 3,
				LocalDateTime.of(2025, 2, 1, 10, 10), "Task 3", "Checkpoint 1")));
	}

	@Test
	public void multipleStudentsStudentStats() {
		SCPerson student1 = SCPerson.builder().id(1L).build();
		SCPerson student2 = SCPerson.builder().id(2L).build();

		TaskCompletion taskCompletion1 = TaskCompletion.builder().person(student1)
				.timestamp(LocalDateTime.of(2024, 11, 1, 10, 10)).build();
		TaskInfo taskInfo1 = TaskInfo.builder().id(1L).name("Task 1")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						taskCompletion1))
				.build();
		RegularTask task1 = RegularTask.builder()
				.skill(skillWithCheckpointBuilder("Checkpoint 1", LocalDateTime.of(2025, 1, 1, 10, 10)))
				.taskInfo(taskInfo1)
				.build();
		taskInfo1.setTask(task1);
		taskCompletion1.setTask(taskInfo1);

		TaskCompletion taskCompletion2 = TaskCompletion.builder().person(student1)
				.timestamp(LocalDateTime.of(2025, 8, 1, 10, 10)).build();
		TaskInfo taskInfo2 = TaskInfo.builder().id(2L).name("Task 2")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						taskCompletion2))
				.build();
		ChoiceTask task2 = ChoiceTask.builder()
				.skill(skillWithCheckpointBuilder("Checkpoint 2", LocalDateTime.of(2024, 1, 1, 10, 10)))
				.tasks(List.of(taskInfo2))
				.build();
		taskInfo2.setChoiceTask(task2);
		taskCompletion2.setTask(taskInfo2);

		TaskCompletion taskCompletion3 = TaskCompletion.builder().person(student1)
				.timestamp(LocalDateTime.of(2025, 2, 1, 10, 10)).build();
		TaskCompletion taskCompletion4 = TaskCompletion.builder().person(student2)
				.timestamp(LocalDateTime.of(2024, 4, 1, 10, 10)).build();

		TaskInfo taskInfo3 = TaskInfo.builder().id(3L).name("Task 3")
				.time(7).type(TaskType.READING).completedBy(Set.of(
						taskCompletion3, taskCompletion4))
				.build();
		ChoiceTask task3 = ChoiceTask.builder().skill(skillBuilder()).tasks(List.of(taskInfo3))
				.build();
		taskInfo3.setChoiceTask(task3);
		taskCompletion3.setTask(taskInfo3);
		taskCompletion4.setTask(taskInfo3);

		PersonSummaryDTO personSummary1 = new PersonSummaryDTO();
		personSummary1.setUsername("student1");
		personSummary1.setId(1L);
		PersonSummaryDTO personSummary2 = new PersonSummaryDTO();
		personSummary2.setUsername("student2");
		personSummary2.setId(2L);

		when(personControllerApi.getPeopleByEditionAndRoleType(1L, "STUDENT"))
				.thenReturn(Flux.just(personSummary1, personSummary2));
		when(personRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(student1, student2));
		when(taskCompletionRepository.findAllByPersonAndEditionId(student1, 1L))
				.thenReturn(Set.of(taskCompletion1, taskCompletion2, taskCompletion3));
		when(taskCompletionRepository.findAllByPersonAndEditionId(student2, 1L))
				.thenReturn(Set.of(taskCompletion4));

		List<StudentStatsDTO> studentStats = statsService.teacherStatsStudentLevel(1L);

		assertThat(studentStats).isEqualTo(List.of(new StudentStatsDTO(1L, "student1", "Path not chosen", 3,
				LocalDateTime.of(2025, 8, 1, 10, 10), "Task 2", "Checkpoint 1"),
				new StudentStatsDTO(2L, "student2", "Path not chosen", 1,
						LocalDateTime.of(2024, 4, 1, 10, 10), "Task 3",
						"No checkpoint started")));
	}

}
