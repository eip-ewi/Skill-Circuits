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
import nl.tudelft.skills.dto.create.*;
import nl.tudelft.skills.dto.id.*;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.RegularTaskViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.*;

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
	private final RegularTaskRepository regularTaskRepository;
	private final TaskRepository taskRepository;
	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final SkillService skillService;
	private final TaskCompletionService taskCompletionService;
	private final TaskCompletionRepository taskCompletionRepository;
	private final ClickedLinkService clickedLinkService;
	private final ClickedLinkRepository clickedLinkRepository;
	private final PersonRepository personRepository;
	private final HttpSession session;
	private final EditionRepository editionRepository;
	private final PathPreferenceRepository pathPreferenceRepository;
	private final PersonService personService;
	private final RoleControllerApi roleApi;

	@Autowired
	public SkillControllerTest(SkillRepository skillRepository, RegularTaskRepository regularTaskRepository,
			TaskRepository taskRepository,
			SkillService skillService, SubmoduleRepository submoduleRepository,
			ExternalSkillRepository externalSkillRepository,
			AbstractSkillRepository abstractSkillRepository, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, TaskCompletionService taskCompletionService,
			PersonService personService, ClickedLinkService clickedLinkService,
			ClickedLinkRepository clickedLinkRepository, TaskCompletionRepository taskCompletionRepository,
			EditionRepository editionRepository, RoleControllerApi roleApi, PersonRepository personRepository,
			PathPreferenceRepository pathPreferenceRepository) {
		this.submoduleRepository = submoduleRepository;
		this.session = mock(HttpSession.class);
		this.moduleService = mock(ModuleService.class);
		this.externalSkillRepository = externalSkillRepository;
		this.regularTaskRepository = regularTaskRepository;
		this.taskRepository = taskRepository;
		this.clickedLinkService = clickedLinkService;
		this.clickedLinkRepository = clickedLinkRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.skillService = skillService;
		this.taskCompletionService = taskCompletionService;
		this.personRepository = personRepository;
		this.skillRepository = skillRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.editionRepository = editionRepository;
		this.personService = personService;
		this.pathPreferenceRepository = pathPreferenceRepository;
		this.roleApi = roleApi;

		this.skillController = new SkillController(skillRepository, externalSkillRepository,
				abstractSkillRepository, taskRepository, regularTaskRepository, submoduleRepository,
				checkpointRepository,
				pathRepository,
				personRepository, skillService, moduleService, taskCompletionService, clickedLinkService,
				personService, session);
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

		// TODO fix deprecated API usage.
		// This fixes flaky tests by ensuring the right beans are used.
		SkillController skc = new SkillController(SpringContext.getBean(SkillRepository.class),
				externalSkillRepository,
				abstractSkillRepository, taskRepository, regularTaskRepository, submoduleRepository,
				SpringContext.getBean(CheckpointRepository.class), pathRepository,
				personRepository, skillService, moduleService, taskCompletionService, clickedLinkService,
				personService, session);

		skc.createSkill(null, dto, mock(Model.class));

		assertTrue(skillRepository.findAll().stream()
				.anyMatch(s -> s.getName().equals("New skill with new checkpoint")));
		assertThat(skillRepository.findAll().stream()
				.filter(s -> s.getName().equals("New skill with new checkpoint")).findFirst().get()
				.getCheckpoint().getName()).isEqualTo("New Checkpoint");
		assertThat(checkpointRepository.findAll().stream()
				.filter(c -> c.getName().equals("New Checkpoint")).findFirst().get()
				.getSkills()).anyMatch(s -> s.getName().equals("New skill with new checkpoint"));
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
				abstractSkillRepository, taskRepository, regularTaskRepository,
				submoduleRepository, checkpointRepository, pathRepository,
				personRepository, mockSkillService, moduleService, taskCompletionService, clickedLinkService,
				personService, session);

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
				abstractSkillRepository, taskRepository, regularTaskRepository,
				submoduleRepository, checkpointRepository, pathRepository,
				personRepository, mockSkillService, moduleService, taskCompletionService, clickedLinkService,
				personService, session);

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
		assertThat(model.getAttribute("tasksAdded")).isEqualTo(Set.of());
		assertThat(model.getAttribute("skillsModified")).isEqualTo(Set.of());

		// The skill view should be unmodified (all tasks with their initial visibility, meaning all are visible)
		ModuleLevelSkillViewDTO view = View.convert(skill, ModuleLevelSkillViewDTO.class);
		assertThat(model.getAttribute("block")).isEqualTo(view);
	}

	@Test
	@WithUserDetails("username")
	void testGetSkillModified() {
		// General setup for test
		SCEdition edition = db.getEditionRL();
		SCPerson person = db.getPerson();
		Skill skill = db.getSkillVariables();
		RegularTask taskDo = db.getTaskDo10a();
		RegularTask taskRead = db.getTaskRead10();
		edition.setVisible(true);
		mockRole(roleApi, "STUDENT");

		// Create new path
		Path explorerPath = Path.builder().name("Explorer").edition(edition)
				.tasks(Set.of(taskDo)).build();
		edition.getPaths().add(explorerPath);

		// Set preferred path
		Path pathfinderPath = db.getPathFinderPath();
		PathPreference pathPreference = PathPreference.builder().person(person).path(pathfinderPath)
				.edition(edition).build();
		person.getPathPreferences().add(pathPreference);
		pathPreferenceRepository.save(pathPreference);
		personRepository.save(person);

		// Add tasks to paths
		pathfinderPath.getTasks().add(taskRead);
		taskRead.getPaths().add(pathfinderPath);
		taskDo.getPaths().add(explorerPath);

		// "Remove" taskRead10 and add taskDo10a
		person.getSkillsModified().add(skill);
		person.getTasksAdded().add(taskDo);

		// Set taskRead10 to be completed
		TaskCompletion completion = TaskCompletion.builder().person(person).task(taskDo).build();
		person.getTaskCompletions().add(completion);
		taskDo.getCompletedBy().add(completion);

		// Save all changes to the db
		pathRepository.save(pathfinderPath);
		pathRepository.save(explorerPath);
		taskCompletionRepository.save(completion);
		editionRepository.save(edition);
		regularTaskRepository.save(taskRead);
		regularTaskRepository.save(taskDo);
		personRepository.save(person);

		// Get authenticated person
		Person authPerson = ((LabradorUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		// Call method
		skillController.getSkill(authPerson, skill.getId(), model);

		// Removed task should be invisible
		ModuleLevelSkillViewDTO view = View.convert(skill, ModuleLevelSkillViewDTO.class);
		view.getTasks().stream().filter(t -> t.getId().equals(taskRead.getId()))
				.findFirst().get().setVisible(false);
		RegularTaskViewDTO taskView = view.getTasks().stream()
				.filter(t -> t instanceof RegularTaskViewDTO && t.getId().equals(taskDo.getId()))
				.findFirst().map(v -> (RegularTaskViewDTO) v).get();
		taskView.getTaskInfo().setCompleted(true);
		assertThat(model.getAttribute("block")).isEqualTo(view);

		// Assert on added model attributes concerning paths and added tasks/modified skills
		assertThat(model.getAttribute("selectedPathId")).isEqualTo(pathfinderPath.getId());
		assertThat(model.getAttribute("tasksAdded")).isEqualTo(Set.of(
				View.convert(taskDo, RegularTaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(Set.of(view));
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
				abstractSkillRepository, taskRepository, regularTaskRepository,
				submoduleRepository, checkpointRepository, pathRepository,
				personRepository, mockSkillService, moduleService, taskCompletionService, clickedLinkService,
				personService, session);

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
