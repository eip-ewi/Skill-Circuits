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
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.*;

import javax.servlet.http.HttpSession;

import nl.tudelft.librador.SpringContext;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.CheckpointCreateDTO;
import nl.tudelft.skills.dto.create.ExternalSkillCreateDTO;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.create.TaskCreateDTO;
import nl.tudelft.skills.dto.id.*;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskType;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.service.ModuleService;
import nl.tudelft.skills.service.SkillService;
import nl.tudelft.skills.service.TaskCompletionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SkillControllerTest extends ControllerTest {

	private final SkillController skillController;
	private final AbstractSkillRepository abstractSkillRepository;
	private final SkillRepository skillRepository;
	private final ModuleService moduleService;
	private final SubmoduleRepository submoduleRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final TaskRepository taskRepository;
	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final SkillService skillService;
	private final TaskCompletionService taskCompletionService;
	private final TaskCompletionRepository taskCompletionRepository;
	private final HttpSession session;

	@Autowired
	public SkillControllerTest(SkillRepository skillRepository, TaskRepository taskRepository,
			SkillService skillService, SubmoduleRepository submoduleRepository,
			ExternalSkillRepository externalSkillRepository,
			AbstractSkillRepository abstractSkillRepository, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, TaskCompletionService taskCompletionService,
			TaskCompletionRepository taskCompletionRepository) {
		this.submoduleRepository = submoduleRepository;
		this.session = mock(HttpSession.class);
		this.moduleService = mock(ModuleService.class);
		this.externalSkillRepository = externalSkillRepository;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.skillService = skillService;
		this.taskCompletionService = taskCompletionService;
		this.skillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository,
				submoduleRepository, checkpointRepository, pathRepository, skillService, moduleService,
				taskCompletionService, session);
		this.skillRepository = skillRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
	}

	@Test
	void createSkill() {
		var dto = SkillCreateDTO.builder()
				.name("New Skill")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.checkpoint(new CheckpointIdDTO(db.getCheckpointLectureOne().getId()))
				.requiredTaskIds(Collections.emptyList())
				.column(10).row(11).newItems(new ArrayList<>()).build();

		skillController.createSkill(null, dto, mock(Model.class));

		assertTrue(skillRepository.findAll().stream().anyMatch(s -> s.getName().equals("New Skill")));
	}

	@Test
	void createSkillWithTasks() {
		TaskCreateDTO taskDto = TaskCreateDTO.builder().name("New Task").type(TaskType.EXERCISE)
				.skill(new SkillIdDTO())
				.index(1).time(1).build();
		var dto = SkillCreateDTO.builder()
				.name("New Skill")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.checkpoint(new CheckpointIdDTO(db.getCheckpointLectureOne().getId()))
				.requiredTaskIds(Collections.emptyList())
				.column(10).row(11).newItems(new ArrayList<>()).build();
		dto.setNewItems(List.of(taskDto));
		skillController.createSkill(null, dto, mock(Model.class));

		Optional<Skill> skill = skillRepository.findAll().stream()
				.filter(s -> s.getName().equals("New Skill"))
				.findFirst();
		assertTrue(skill.isPresent());

		Optional<Task> task = taskRepository.findAll().stream().filter(t -> t.getName().equals("New Task"))
				.findFirst();
		assertTrue(task.isPresent());

		assertThat(task.get().getSkill()).isEqualTo(skill.get());
		assertThat(skill.get().getTasks()).containsExactly(task.get());
	}

	@Test
	void createSkillWithNewCheckpoint() {
		var dto = SkillCreateDTO.builder()
				.name("New skill with new checkpoint")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.checkpointCreate(CheckpointCreateDTO.builder()
						.edition(new SCEditionIdDTO(
								db.getSubmoduleCases().getModule().getEdition().getId()))
						.deadline(LocalDateTime.of(2022, 1, 1, 0, 0, 0))
						.name("New Checkpoint").build())
				.requiredTaskIds(Collections.emptyList())
				.column(10).row(11).newItems(new ArrayList<>()).build();

		// This fixes flaky tests by ensuring the right beans are used.
		SkillController skc = new SkillController(SpringContext.getBean(SkillRepository.class),
				externalSkillRepository,
				abstractSkillRepository, taskRepository,
				submoduleRepository, SpringContext.getBean(CheckpointRepository.class), pathRepository,
				skillService,
				moduleService, taskCompletionService, session);

		skc.createSkill(null, dto, mock(Model.class));

		assertTrue(skillRepository.findAll().stream()
				.anyMatch(s -> s.getName().equals("New skill with new checkpoint")));
		assertThat(skillRepository.findAll().stream()
				.filter(s -> s.getName().equals("New skill with new checkpoint")).findFirst().get()
				.getCheckpoint().getName()).isEqualTo("New Checkpoint");
	}

	@Test
	void createExternalSkill() {
		var dto = ExternalSkillCreateDTO.builder()
				.module(new SCModuleIdDTO(db.getModuleProofTechniques().getId()))
				.skill(new SkillIdDTO(db.getSkillAssumption().getId()))
				.column(10).row(11).build();

		skillController.createSkill(null, dto, mock(Model.class));

		assertTrue(abstractSkillRepository.findAll().stream().anyMatch(s -> s instanceof ExternalSkill es
				&& skillRepository.findByIdOrThrow(es.getSkill().getId()).getName().equals("Assumption")));
	}

	@Test
	void patchSkill() {
		Long skillId = db.getSkillVariables().getId();
		Task old = db.getTaskRead10();
		TaskCreateDTO taskAdded = TaskCreateDTO.builder().name("New Task").type(TaskType.EXERCISE)
				.skill(new SkillIdDTO(skillId)).index(1).time(1).build();
		TaskPatchDTO oldTask = TaskPatchDTO.builder().name(old.getName()).type(old.getType())
				.id(old.getId()).index(old.getIdx()).time(old.getTime()).build();

		// Add a task and remove a task
		skillController.patchSkill(SkillPatchDTO.builder()
				.id(skillId)
				.name("Updated")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.items(List.of(oldTask))
				.newItems(List.of(taskAdded))
				.removedItems(Set.of(db.getTaskDo10a().getId()))
				.build(), model);

		Skill skill = db.getSkillVariables();
		assertThat(skill.getName()).isEqualTo("Updated");
		assertThat(submoduleRepository.findByIdOrThrow(skill.getSubmodule().getId())).isEqualTo(
				submoduleRepository.findByIdOrThrow(db.getSubmoduleCases().getId()));

		// Check task related properties
		Optional<Task> task = taskRepository.findAll().stream().filter(t -> t.getName().equals("New Task"))
				.findFirst();
		assertTrue(task.isPresent());
		assertThat(skill.getTasks()).containsExactlyInAnyOrder(db.getTaskRead10(), task.get());
		assertThat(task.get().getSkill()).isEqualTo(skill);

		// Check that all TaskCompletion related information remains the same
		assertThat(taskCompletionRepository.findAll()).hasSize(4);
		assertThat(db.getPerson().getTaskCompletions()).hasSize(4);
		assertThat(db.getTaskRead12().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskDo12ae().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskRead11().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskDo11ad().getCompletedBy()).hasSize(1);
	}

	@Test
	void patchSkillDeletesTask() {
		Long taskId = db.getTaskRead11().getId();

		skillController.patchSkill(SkillPatchDTO.builder()
				.id(db.getSkillNegation().getId())
				.name("Updated")
				.removedItems(Set.of(taskId))
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.build(), model);

		Skill skill = db.getSkillNegation();
		assertThat(skill.getName()).isEqualTo("Updated");
		assertThat(submoduleRepository.findByIdOrThrow(skill.getSubmodule().getId())).isEqualTo(
				submoduleRepository.findByIdOrThrow(db.getSubmoduleCases().getId()));

		// Check that the Task and its TaskCompletions were deleted
		assertThat(taskRepository.findById(taskId)).isEmpty();
		assertThat(taskCompletionRepository.findAll()).hasSize(3);
		assertThat(db.getPerson().getTaskCompletions()).hasSize(3);
		assertThat(taskRepository.findById(taskId)).isEmpty();
		// Check that all other TaskCompletions remain the same
		assertThat(db.getTaskRead12().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskDo12ae().getCompletedBy()).hasSize(1);
		assertThat(db.getTaskDo11ad().getCompletedBy()).hasSize(1);
	}

	@Test
	void updateSkillPosition() {
		skillController.updateSkillPosition(db.getSkillVariables().getId(), SkillPositionPatchDTO.builder()
				.column(10)
				.row(11)
				.build());

		Skill skill = db.getSkillVariables();
		assertThat(skill.getColumn()).isEqualTo(10);
		assertThat(skill.getRow()).isEqualTo(11);
	}

	@Test
	void deleteSkill() {
		Long id = db.getSkillVariables().getId();
		skillController.deleteSkill(id, "block");
		assertThat(skillRepository.existsById(id)).isFalse();
	}

	@Test
	void connectSkill() {
		assertThat(db.getSkillImplication().getChildren())
				.doesNotContain(db.getSkillProofOutline());
		skillController.connectSkill(db.getSkillImplication().getId(), db.getSkillProofOutline().getId());
		assertThat(db.getSkillImplication().getChildren())
				.contains(db.getSkillProofOutline());
	}

	@Test
	void disconnectSkill() {
		assertThat(db.getSkillImplication().getChildren())
				.contains(db.getSkillAssumption());
		skillController.disconnectSkill(db.getSkillImplication().getId(), db.getSkillAssumption().getId());
		assertThat(db.getSkillImplication().getChildren())
				.doesNotContain(db.getSkillAssumption());
	}

	@Test
	void endpointsAreProtected() throws Exception {
		mvc.perform(patch("/skill/{id}", db.getSkillVariables().getId()))
				.andExpect(status().isForbidden());
		mvc.perform(post("/skill"))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/skill?id=1"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/skill/connect/1/2"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/skill/disconnect/1/2"))
				.andExpect(status().isForbidden());
	}

}
