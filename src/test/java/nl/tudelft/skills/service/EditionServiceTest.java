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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.view.TaskStatsDTO;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import reactor.core.publisher.Flux;

@Transactional
@SpringBootTest()
public class EditionServiceTest {

	private final EditionService editionService;
	private TaskRepository taskRepository;
	private RoleControllerApi roleControllerApi;

	private PathPreferenceRepository pathPreferenceRepository;
	private ClickedLinkRepository clickedLinkRepository;

	@Autowired
	public EditionServiceTest(EditionControllerApi editionApi, EditionRepository editionRepository,
			PersonControllerApi personControllerApi,
			SCPersonService scPersonService,
			PersonRepository personRepository, DTOConverter dtoConverter) {

		this.taskRepository = mock(TaskRepository.class);
		this.roleControllerApi = mock(RoleControllerApi.class);
		this.pathPreferenceRepository = mock(PathPreferenceRepository.class);
		this.clickedLinkRepository = mock(ClickedLinkRepository.class);

		editionService = new EditionService(editionApi, personControllerApi, scPersonService,
				this.taskRepository, this.clickedLinkRepository, this.pathPreferenceRepository,
				this.roleControllerApi,
				editionRepository, personRepository, dtoConverter);
	}

	@Test
	public void emptyTaskStats() {
		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong())).thenReturn(List.of());
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);
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

	@Test
	public void noActivityTaskStats() {
		TaskInfo read12Info = TaskInfo.builder().id(1L).name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillBuilder()).taskInfo(read12Info)
				.build();
		read12Info.setTask(taskRead12);
		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"No checkpoint", "Logic Basics", "Proof Techniques", 0, 0, 0, 0, 0)));
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
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

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
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

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
				.completedBy(Set.of(TaskCompletion.builder().person(new SCPerson()).build())).build();
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
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

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

		PathPreference pathPreference1 = PathPreference.builder().person(SCPerson.builder().id(1L).build())
				.build();
		PathPreference pathPreference2 = PathPreference.builder().person(SCPerson.builder().id(2L).build())
				.build();
		PathPreference pathPreference3 = PathPreference.builder().person(SCPerson.builder().id(3L).build())
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
		RoleDetailsDTO role4 = new RoleDetailsDTO();
		role4.setType(RoleDetailsDTO.TypeEnum.STUDENT);
		role4.setPerson(new PersonSummaryDTO().id(4L));

		when(taskRepository.findAllBySkillSubmoduleModuleEditionId(anyLong()))
				.thenReturn(List.of(taskRead12));
		when(roleControllerApi.getRolesById(Set.of(1L), Set.of(1L, 2L)))
				.thenReturn(Flux.just(role1, role2, role3));
		when(roleControllerApi.getRolesById(Set.of(1L), Set.of(3L))).thenReturn(Flux.just(role4));
		when(pathPreferenceRepository.findAllByPathId(1L))
				.thenReturn(List.of(pathPreference1, pathPreference2));
		when(pathPreferenceRepository.findAllByPathId(2L)).thenReturn(List.of(pathPreference3));
		when(clickedLinkRepository.getByTask(any())).thenReturn(
				List.of());

		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

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
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

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
		List<TaskStatsDTO> taskStats = editionService.teacherStatsTaskLevel(1L);

		assertEquals(1, taskStats.size());
		assertThat(taskStats).isEqualTo(List.of(new TaskStatsDTO(1L, "Read chapter 1.2", "Implication",
				"No checkpoint", "Logic Basics", "Proof Techniques", 2, 0, 3, 2, 1)));
	}

}
