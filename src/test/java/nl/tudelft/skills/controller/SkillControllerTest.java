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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.*;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.CheckpointCreateDTO;
import nl.tudelft.skills.dto.create.ExternalSkillCreateDTO;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.create.TaskCreateDTO;
import nl.tudelft.skills.dto.id.*;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.ClickedLinkService;
import nl.tudelft.skills.service.ModuleService;
import nl.tudelft.skills.service.SkillService;
import nl.tudelft.skills.service.TaskCompletionService;

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
	private final ClickedLinkService clickedLinkService;
	private final ClickedLinkRepository clickedLinkRepository;
	private final PersonRepository personRepository;
	private final ModuleRepository moduleRepository;
	private final HttpSession session;
	private final EditionRepository editionRepository;
	private final RoleControllerApi roleApi;

	@Autowired
	public SkillControllerTest(SkillRepository skillRepository, TaskRepository taskRepository,
			SkillService skillService, SubmoduleRepository submoduleRepository,
			ExternalSkillRepository externalSkillRepository,
			AbstractSkillRepository abstractSkillRepository, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, TaskCompletionService taskCompletionService,
			ClickedLinkService clickedLinkService, ClickedLinkRepository clickedLinkRepository,
			TaskCompletionRepository taskCompletionRepository, EditionRepository editionRepository,
			RoleControllerApi roleApi, PersonRepository personRepository, ModuleRepository moduleRepository) {
		this.submoduleRepository = submoduleRepository;
		this.session = mock(HttpSession.class);
		this.moduleService = mock(ModuleService.class);
		this.externalSkillRepository = externalSkillRepository;
		this.taskRepository = taskRepository;
		this.clickedLinkService = clickedLinkService;
		this.clickedLinkRepository = clickedLinkRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.skillService = skillService;
		this.taskCompletionService = taskCompletionService;
		this.personRepository = personRepository;
		this.moduleRepository = moduleRepository;
		this.skillRepository = skillRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.editionRepository = editionRepository;
		this.roleApi = roleApi;

		this.skillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository, submoduleRepository, moduleRepository,
				checkpointRepository, pathRepository, personRepository, skillService,
				moduleService, taskCompletionService, clickedLinkService, session);
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
				submoduleRepository, moduleRepository, SpringContext.getBean(CheckpointRepository.class),
				pathRepository, personRepository, skillService,
				moduleService, taskCompletionService, clickedLinkService, session);

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
		//Check that ClickedLinks are deleted
		assertThat(clickedLinkRepository.findAll()).hasSize(1);
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
	@WithUserDetails("username")
	void redirectToExternalSkillVisibleToStudent() {
		// Use a mocked version of the skill service because of the complexity
		// of the recentActiveEditionForSkillOrLatest method
		SkillService mockSkillService = mock(SkillService.class);
		SkillController innerSkillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository, submoduleRepository, moduleRepository,
				checkpointRepository,
				pathRepository, personRepository, mockSkillService, moduleService, taskCompletionService,
				clickedLinkService, session);

		// Save an external skill and mock the response
		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());
		when(mockSkillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(),
				externalSkill)).thenReturn(db.getSkillAssumption());

		// Make edition visible
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		// Mock role of user
		mockRole(roleApi, "STUDENT");

		// Get authenticated person
		Person authPerson = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Assert on result
		Long moduleId = db.getModuleProofTechniques().getId();
		Long skillId = db.getSkillAssumption().getId();
		assertThat(innerSkillController.redirectToExternalSkill(externalSkill.getId(), authPerson))
				.isEqualTo("redirect:/module/" + moduleId + "#block-" + skillId + "-name");
	}

	@Test
	@WithUserDetails("teacher")
	void redirectToExternalSkillVisibleToTeacher() {
		// Use a mocked version of the skill service because of the complexity
		// of the recentActiveEditionForSkillOrLatest method
		SkillService mockSkillService = mock(SkillService.class);
		SkillController innerSkillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository, submoduleRepository, moduleRepository,
				checkpointRepository,
				pathRepository, personRepository, mockSkillService, moduleService, taskCompletionService,
				clickedLinkService,
				session);

		// Save an external skill and mock the response
		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());
		when(mockSkillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(),
				externalSkill)).thenReturn(db.getSkillAssumption());

		// Mock role of user
		mockRole(roleApi, "TEACHER");

		// Get authenticated person
		Person authPerson = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Assert on result
		Long moduleId = db.getModuleProofTechniques().getId();
		Long skillId = db.getSkillAssumption().getId();
		assertThat(innerSkillController.redirectToExternalSkill(externalSkill.getId(), authPerson))
				.isEqualTo("redirect:/module/" + moduleId + "#block-" + skillId + "-name");
	}

	@Test
	@WithUserDetails("username")
	void redirectToExternalSkillNotAuthorizedStudent() throws Exception {
		// Save an external skill
		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Mock role of user
		mockRole(roleApi, "STUDENT");

		// Assert on result, should be unauthorized since the external skill is not visible
		mvc.perform(get("/skill/external/" + externalSkill.getId()).with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithAnonymousUser
	void redirectToExternalSkillNotAuthenticated() throws Exception {
		// Save an external skill
		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());

		// Assert on result, it will throw an error since there is no authenticated person
		// (Which is needed as a parameter to the method)
		mvc.perform(get("/skill/external/" + externalSkill.getId()).with(csrf()))
				.andExpect(status().is4xxClientError());
	}

	@Test
	@WithUserDetails("username")
	void testGetSkillUnmodified() {
		// Setup for test
		db.getEditionRL().setVisible(true);
		editionRepository.save(db.getEditionRL());
		mockRole(roleApi, "STUDENT");
		Skill skill = db.getSkillVariables();

		// Get authenticated person
		Person authPerson = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Call method
		skillController.getSkill(authPerson, skill.getId(), model);

		// Assert on added model attributes concerning paths and added tasks/modified skills
		assertThat(model.getAttribute("selectedPathId")).isNull();
		assertThat(model.getAttribute("tasksAdded")).isEqualTo(List.of());
		assertThat(model.getAttribute("skillsModified")).isEqualTo(List.of());

		// The skill view should be unmodified (all tasks with their initial visibility, meaning all are visible)
		ModuleLevelSkillViewDTO view = View.convert(skill, ModuleLevelSkillViewDTO.class);
		assertThat(model.getAttribute("block")).isEqualTo(view);
	}

	@Test
	@WithUserDetails("username")
	void testGetSkillModified() {
		// General setup for test
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);
		mockRole(roleApi, "STUDENT");
		Skill skill = db.getSkillVariables();

		// Create new path
		Path explorerPath = Path.builder().name("Explorer").edition(edition)
				.tasks(Set.of(db.getTaskDo10a())).build();
		pathRepository.save(explorerPath);
		edition.getPaths().add(explorerPath);
		editionRepository.save(edition);

		// Set preferred path
		Path pathfinderPath = db.getPathFinderPath();
		when(moduleService.getDefaultOrPreferredPath(db.getPerson().getId(), edition.getId()))
				.thenReturn(pathfinderPath);

		// Add tasks to paths
		pathfinderPath.getTasks().add(db.getTaskRead10());
		pathRepository.save(pathfinderPath);
		db.getTaskRead10().getPaths().add(pathfinderPath);
		db.getTaskDo10a().getPaths().add(explorerPath);
		taskRepository.save(db.getTaskRead10());
		taskRepository.save(db.getTaskDo10a());

		// "Remove" taskRead10 and add taskDo10a
		db.getPerson().getSkillsModified().add(skill);
		db.getPerson().getTasksAdded().add(db.getTaskDo10a());
		personRepository.save(db.getPerson());

		// Get authenticated person
		Person authPerson = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Call method
		skillController.getSkill(authPerson, skill.getId(), model);

		// Removed task should be invisible
		ModuleLevelSkillViewDTO view = View.convert(skill, ModuleLevelSkillViewDTO.class);
		view.getTasks().stream().filter(t -> t.getId().equals(db.getTaskRead10().getId()))
				.findFirst().get().setVisible(false);
		assertThat(model.getAttribute("block")).isEqualTo(view);

		// Assert on added model attributes concerning paths and added tasks/modified skills
		assertThat(model.getAttribute("selectedPathId")).isEqualTo(pathfinderPath.getId());
		assertThat(model.getAttribute("tasksAdded")).isEqualTo(List.of(
				View.convert(db.getTaskDo10a(), TaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(List.of(view));
	}

	@Test
	@WithUserDetails("username")
	void getSkillNotAuthorized() throws Exception {
		// A student should not be able to see a skill in an unpublished edition
		mockRole(roleApi, "STUDENT");
		mvc.perform(get("/skill/{id}", db.getSkillVariables().getId()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("username")
	void redirectToExternalSkillNoValidSkill() {
		// Use a mocked version of the skill service because of the complexity
		// of the recentActiveEditionForSkillOrLatest method
		SkillService mockSkillService = mock(SkillService.class);
		SkillController innerSkillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository, submoduleRepository, moduleRepository,
				checkpointRepository,
				pathRepository, personRepository, mockSkillService, moduleService, taskCompletionService,
				clickedLinkService, session);

		// Save an external skill and mock the response
		Skill linkedSkill = db.createSkillInEditionHelper(db.getEditionRL().getId() + 1, false);
		ExternalSkill externalSkill = db.createExternalSkill(linkedSkill);
		when(mockSkillService.recentActiveEditionForSkillOrLatest(db.getPerson().getId(),
				externalSkill)).thenReturn(null);

		// Make edition visible
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		// Mock role of user
		mockRole(roleApi, "STUDENT");

		// Get authenticated person
		Person authPerson = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Assert on result, should redirect to the external skills page, and not the linked skills page
		Long moduleId = externalSkill.getModule().getId();
		assertThat(innerSkillController.redirectToExternalSkill(externalSkill.getId(), authPerson))
				.isEqualTo("redirect:/module/" + moduleId);
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
		mvc.perform(get("/skill/{id}", db.getSkillVariables().getId()))
				.andExpect(status().isUnauthorized());
	}
}
