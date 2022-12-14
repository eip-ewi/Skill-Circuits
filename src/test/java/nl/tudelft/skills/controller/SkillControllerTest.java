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
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpSession;

import nl.tudelft.librador.SpringContext;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.CheckpointCreateDTO;
import nl.tudelft.skills.dto.create.ExternalSkillCreateDTO;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.id.*;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.service.ModuleService;
import nl.tudelft.skills.service.SkillService;

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
	private final HttpSession session;

	@Autowired
	public SkillControllerTest(SkillRepository skillRepository, TaskRepository taskRepository,
			SkillService skillService, SubmoduleRepository submoduleRepository,
			ExternalSkillRepository externalSkillRepository,
			AbstractSkillRepository abstractSkillRepository, CheckpointRepository checkpointRepository,
			PathRepository pathRepository) {
		this.submoduleRepository = submoduleRepository;
		this.session = mock(HttpSession.class);
		this.moduleService = mock(ModuleService.class);
		this.externalSkillRepository = externalSkillRepository;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.skillService = skillService;
		this.skillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository,
				submoduleRepository, checkpointRepository, pathRepository, skillService, moduleService,
				session);
		this.skillRepository = skillRepository;
		this.abstractSkillRepository = abstractSkillRepository;
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
				moduleService, session);

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
		skillController.patchSkill(SkillPatchDTO.builder()
				.id(db.getSkillVariables().getId())
				.name("Updated")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.build(), model);

		Skill skill = db.getSkillVariables();
		assertThat(skill.getName()).isEqualTo("Updated");
		assertThat(submoduleRepository.findByIdOrThrow(skill.getSubmodule().getId())).isEqualTo(
				submoduleRepository.findByIdOrThrow(db.getSubmoduleCases().getId()));
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
