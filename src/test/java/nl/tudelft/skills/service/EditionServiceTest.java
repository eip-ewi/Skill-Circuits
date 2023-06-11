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
package nl.tudelft.skills.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionSummaryDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class EditionServiceTest {

	private final EditionControllerApi editionApi;
	private final EditionService editionService;
	private final EditionRepository editionRepository;
	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final ModuleRepository moduleRepository;
	private final SubmoduleRepository submoduleRepository;
	private final AbstractSkillRepository abstractSkillRepository;
	private final AchievementRepository achievementRepository;
	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;

	private final TestDatabaseLoader db;
	private final LocalDateTime localDateTime;

	@Autowired
	public EditionServiceTest(EditionControllerApi editionApi, EditionRepository editionRepository,
			CircuitService circuitService, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, ModuleRepository moduleRepository,
			SubmoduleRepository submoduleRepository, AbstractSkillRepository abstractSkillRepository,
			AchievementRepository achievementRepository, SkillRepository skillRepository,
			TaskRepository taskRepository, TestDatabaseLoader db) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.moduleRepository = moduleRepository;
		this.submoduleRepository = submoduleRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.achievementRepository = achievementRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;

		this.db = db;
		this.localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);
		editionService = new EditionService(editionApi, editionRepository, circuitService,
				checkpointRepository, pathRepository, moduleRepository, submoduleRepository,
				abstractSkillRepository, achievementRepository, skillRepository, taskRepository);
	}

	@Test
	public void getCourseView() {
		EditionDetailsDTO editionDetailsDTO = new EditionDetailsDTO().id(1L).name("edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(localDateTime);

		when(editionApi.getEditionById(anyLong())).thenReturn(Mono.just(editionDetailsDTO));
		when(editionApi.getAllEditionsByCourse(anyLong()))
				.thenReturn(Flux.fromIterable(List.of(editionDetailsDTO)));

		EditionLevelEditionViewDTO editionView = editionService.getEditionView(1L);
		assertThat(editionView.getName()).isEqualTo("edition");

		// Should contain no edition in the previous editions of the course view
		assertThat(editionView.getCourse())
				.isEqualTo(new EditionLevelCourseViewDTO(2L, "course", List.of()));
	}

	@Test
	public void getCourseViewPreviousEditionExists() {
		// Return and mock three editions, one older and one newer one than the one for which
		// the method is called
		EditionDetailsDTO checkEditionDetails = new EditionDetailsDTO().id(1L).name("edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(localDateTime);
		EditionDetailsDTO olderEdition = new EditionDetailsDTO().id(2L).name("older edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(localDateTime.minusDays(10));
		EditionDetailsDTO newerEdition = new EditionDetailsDTO().id(3L).name("newer edition")
				.course(new CourseSummaryDTO().id(2L).name("course"))
				.startDate(localDateTime.plusDays(10));

		when(editionApi.getEditionById(anyLong())).thenReturn(Mono.just(checkEditionDetails));
		when(editionApi.getAllEditionsByCourse(anyLong()))
				.thenReturn(Flux.fromIterable(List.of(checkEditionDetails, olderEdition, newerEdition)));

		EditionLevelEditionViewDTO editionView = editionService.getEditionView(1L);
		assertThat(editionView.getName()).isEqualTo("edition");

		// Should only contain the older edition in the previous editions of the course view
		EditionLevelEditionSummaryDTO olderEditionSummary = new EditionLevelEditionSummaryDTO(2L,
				"older edition");
		assertThat(editionView.getCourse())
				.isEqualTo(new EditionLevelCourseViewDTO(2L, "course", List.of(olderEditionSummary)));
	}

	@Test
	public void getOrCreateSCEditionCreatesNew() {
		Long editionId = 1L;
		assertThat(editionRepository.findById(editionId)).isNotPresent();

		SCEdition edition = editionService.getOrCreateSCEdition(editionId);

		assertThat(editionRepository.findById(editionId)).isPresent().contains(edition);
	}

	@Test
	public void getDefaultPath() {
		Long editionId = 1L;
		editionService.getOrCreateSCEdition(editionId);

		assertNull(editionService.getDefaultPath(editionId));
	}

	@Test
	public void getPaths() {
		Long editionId = 1L;
		editionService.getOrCreateSCEdition(editionId);

		assertTrue(editionService.getPaths(editionId).isEmpty());
	}

	@Test
	public void testIsEditionEmpty() {
		Long idInUse = db.getEditionRL().getId();

		// Test empty edition
		SCEdition emptyEdition = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		assertTrue(editionService.isEditionEmpty(emptyEdition.getId()));

		// Test with db edition (contains skills, tasks etc.)
		assertFalse(editionService.isEditionEmpty(idInUse));

		// Test with edition containing a module
		SCEdition editionWithModule = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());
		SCModule module = moduleRepository.save(SCModule.builder()
				.edition(editionWithModule).build());
		editionWithModule.getModules().add(module);
		assertFalse(editionService.isEditionEmpty(editionWithModule.getId()));

		// Test with edition containing a checkpoint
		SCEdition editionWithCheckpoint = editionRepository.save(SCEdition.builder().id(idInUse + 3).build());
		Checkpoint checkpoint = checkpointRepository.save(Checkpoint.builder()
				.edition(editionWithCheckpoint).build());
		editionWithCheckpoint.getCheckpoints().add(checkpoint);
		assertFalse(editionService.isEditionEmpty(editionWithCheckpoint.getId()));

		// Test with edition containing a path
		SCEdition editionWithPath = editionRepository.save(SCEdition.builder().id(idInUse + 4).build());
		Path path = pathRepository.save(Path.builder().edition(editionWithPath).build());
		editionWithPath.getPaths().add(path);
		assertFalse(editionService.isEditionEmpty(editionWithPath.getId()));
	}

	@Test
	public void testCopyToEditionInvalid() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition emptyEdition = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());

		// Test with non-empty db edition (contains skills, tasks etc.)
		assertThat(editionService.copyEdition(idInUse, idInUse)).isNull();
		assertThat(editionRepository.findAll()).containsExactlyInAnyOrder(db.getEditionRL(), emptyEdition);

		// Test with id that does not exist
		assertThrows(ResourceNotFoundException.class, () -> editionService.copyEdition(idInUse + 1,
				idInUse + 2));
		assertThat(editionRepository.findAll()).containsExactlyInAnyOrder(db.getEditionRL(), emptyEdition);
		assertThrows(ResourceNotFoundException.class, () -> editionService.copyEdition(idInUse + 2,
				idInUse + 1));
		assertThat(editionRepository.findAll()).containsExactlyInAnyOrder(db.getEditionRL(), emptyEdition);
	}

	@Test
	public void testCopyEditionCheckpoints() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add three checkpoints to the previous edition, with different properties
		Checkpoint checkpointA = checkpointRepository.save(Checkpoint.builder()
				.edition(from).name("Checkpoint A").deadline(localDateTime).build());
		Checkpoint checkpointB = checkpointRepository.save(Checkpoint.builder()
				.edition(from).name("Checkpoint B").deadline(localDateTime.plusDays(5)).build());
		Checkpoint checkpointC = checkpointRepository.save(Checkpoint.builder()
				.edition(from).name("Checkpoint C").deadline(localDateTime.minusDays(5)).build());

		// Add a skill to checkpoint B, to assure that this does not affect functionality of the method
		SCModule module = SCModule.builder().name("Module").edition(from).build();
		module = moduleRepository.save(module);
		Submodule submodule = Submodule.builder().module(module).name("Submodule").column(0).row(0).build();
		submodule = submoduleRepository.save(submodule);
		module.getSubmodules().add(submodule);
		Skill skill = Skill.builder().submodule(submodule).checkpoint(checkpointB).name("Skill").column(0)
				.row(0)
				.build();
		skill = skillRepository.save(skill);
		submodule.getSkills().add(skill);
		checkpointB.getSkills().add(skill);

		from.getCheckpoints().addAll(Set.of(checkpointA, checkpointB, checkpointC));

		int amountBefore = checkpointRepository.findAll().size();
		Map<Checkpoint, Checkpoint> copies = editionService.copyEditionCheckpoints(from, to);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(checkpointA, checkpointB, checkpointC);
		assertThat(checkpointRepository.findAll()).hasSize(amountBefore + 3);

		// Safety check that the previous edition was not changed
		SCEdition old = editionRepository.findByIdOrThrow(from.getId());
		assertThat(old).isEqualTo(from);
		assertThat(to.getCheckpoints()).containsAll(copies.values());
		assertThat(old.getCheckpoints()).containsExactlyInAnyOrder(checkpointA, checkpointB, checkpointC);

		testCheckpointEqualityHelper(checkpointA, copies.get(checkpointA), to);
		testCheckpointEqualityHelper(checkpointB, copies.get(checkpointB), to);
		testCheckpointEqualityHelper(checkpointC, copies.get(checkpointC), to);
	}

	private void testCheckpointEqualityHelper(Checkpoint initial, Checkpoint copy, SCEdition to) {
		assertThat(copy).isNotNull();
		assertThat(checkpointRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getName()).isEqualTo(initial.getName());
		assertThat(copy.getDeadline()).isEqualTo(initial.getDeadline());
		assertThat(copy.getEdition()).isEqualTo(to);
		assertThat(to.getCheckpoints()).contains(copy);
		assertThat(to.getCheckpoints()).doesNotContain(initial);

		// No skills should have been added for now
		assertThat(copy.getSkills()).isEmpty();

		// Safety check that the previous checkpoint was not changed
		assertThat(checkpointRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	public void testCopyEditionPaths() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add two paths to the previous edition, with different properties
		Path pathA = pathRepository.save(Path.builder().name("Path A").edition(from).build());
		from.getPaths().add(pathA);
		Path pathB = pathRepository.save(Path.builder().name("Path B").edition(from).build());
		from.getPaths().add(pathA);

		// Add a task to path B, to assure that this does not affect functionality of the method
		SCModule module = SCModule.builder().name("Module").edition(from).build();
		module = moduleRepository.save(module);
		Submodule submodule = Submodule.builder().module(module).name("Submodule").column(0).row(0).build();
		module.getSubmodules().add(submodule);
		submodule = submoduleRepository.save(submodule);
		Checkpoint checkpoint = Checkpoint.builder().edition(from).name("Checkpoint").deadline(localDateTime)
				.build();
		checkpoint = checkpointRepository.save(checkpoint);
		from.getCheckpoints().add(checkpoint);
		Skill skill = Skill.builder().submodule(submodule).checkpoint(checkpoint).name("Skill").column(0)
				.row(0)
				.build();
		skill = skillRepository.save(skill);
		submodule.getSkills().add(skill);
		checkpoint.getSkills().add(skill);
		Task task = Task.builder().name("Task").skill(skill).build();
		task = taskRepository.save(task);
		skill.getTasks().add(task);
		pathB.getTasks().add(task);

		from.getPaths().addAll(Set.of(pathA, pathB));

		int amountBefore = pathRepository.findAll().size();
		Map<Path, Path> copies = editionService.copyEditionPaths(from, to);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(pathA, pathB);
		assertThat(to.getPaths()).containsAll(copies.values());
		assertThat(pathRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		SCEdition old = editionRepository.findByIdOrThrow(from.getId());
		assertThat(old).isEqualTo(from);
		assertThat(old.getPaths()).containsExactlyInAnyOrder(pathA, pathB);

		testPathEqualityHelper(pathA, copies.get(pathA), to);
		testPathEqualityHelper(pathB, copies.get(pathB), to);
	}

	private void testPathEqualityHelper(Path initial, Path copy, SCEdition to) {
		assertThat(copy).isNotNull();
		assertThat(pathRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getName()).isEqualTo(initial.getName());
		assertThat(copy.getEdition()).isEqualTo(to);
		assertThat(to.getPaths()).contains(copy);
		assertThat(to.getPaths()).doesNotContain(initial);

		// No tasks should have been added for now
		assertThat(copy.getTasks()).isEmpty();

		// Safety check that the previous path was not changed
		assertThat(pathRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	public void testCopyEditionModules() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add two modules to the previous edition, with different properties
		SCModule moduleA = moduleRepository.save(SCModule.builder().name("Module A").edition(from).build());
		from.getModules().add(moduleA);
		SCModule moduleB = moduleRepository.save(SCModule.builder().name("Module B").edition(from).build());
		from.getModules().add(moduleB);

		// Add a submodule to module A, and an external skill to module B, to ensure this does not break
		// functionality of this method
		Submodule submoduleA = Submodule.builder().module(moduleA).name("Submodule").column(0).row(0).build();
		submoduleA = submoduleRepository.save(submoduleA);
		moduleA.getSubmodules().add(submoduleA);

		ExternalSkill externalSkill = ExternalSkill.builder().skill(db.getSkillAssumption()).module(moduleB)
				.row(0).column(0).build();
		externalSkill = abstractSkillRepository.save(externalSkill);
		moduleB.getExternalSkills().add(externalSkill);
		db.getSkillAssumption().getExternalSkills().add(externalSkill);

		int amountBefore = moduleRepository.findAll().size();
		Map<SCModule, SCModule> copies = editionService.copyEditionModules(from, to);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(moduleA, moduleB);
		assertThat(to.getModules()).containsAll(copies.values());
		assertThat(moduleRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		SCEdition old = editionRepository.findByIdOrThrow(from.getId());
		assertThat(old).isEqualTo(from);
		assertThat(old.getModules()).containsExactlyInAnyOrder(moduleA, moduleB);

		testModuleEqualityHelper(moduleA, copies.get(moduleA), to);
		testModuleEqualityHelper(moduleB, copies.get(moduleB), to);
	}

	private void testModuleEqualityHelper(SCModule initial, SCModule copy, SCEdition to) {
		assertThat(copy).isNotNull();
		assertThat(moduleRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getName()).isEqualTo(initial.getName());
		assertThat(copy.getEdition()).isEqualTo(to);
		assertThat(to.getModules()).contains(copy);
		assertThat(to.getModules()).doesNotContain(initial);

		// No submodules or external skills should have been added for now
		assertThat(copy.getSubmodules()).isEmpty();
		assertThat(copy.getExternalSkills()).isEmpty();

		// Safety check that the previous module was not changed
		assertThat(moduleRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	public void testCopyEditionSubmodules() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add a module to each edition
		SCModule moduleFrom = moduleRepository
				.save(SCModule.builder().name("Module from").edition(from).build());
		from.getModules().add(moduleFrom);
		SCModule moduleTo = moduleRepository.save(SCModule.builder().name("Module to").edition(from).build());
		to.getModules().add(moduleTo);

		// Add two submodule to moduleFrom, with different properties
		Submodule submoduleA = Submodule.builder().module(moduleFrom).name("Submodule A").column(0).row(0)
				.build();
		submoduleA = submoduleRepository.save(submoduleA);
		moduleFrom.getSubmodules().add(submoduleA);
		// Add a skill to second submodule, to ensure this does not break functionality of this method
		Submodule submoduleB = Submodule.builder().module(moduleFrom).name("Submodule B").column(0).row(0)
				.build();
		submoduleB = submoduleRepository.save(submoduleB);
		moduleFrom.getSubmodules().add(submoduleB);
		Checkpoint checkpoint = Checkpoint.builder().edition(from).name("Checkpoint").deadline(localDateTime)
				.build();
		checkpoint = checkpointRepository.save(checkpoint);
		from.getCheckpoints().add(checkpoint);
		Skill skill = Skill.builder().submodule(submoduleB).checkpoint(checkpoint).name("Skill").column(0)
				.row(0)
				.build();
		skill = skillRepository.save(skill);
		submoduleB.getSkills().add(skill);
		checkpoint.getSkills().add(skill);

		int amountBefore = submoduleRepository.findAll().size();
		Map<SCModule, SCModule> moduleCopies = Map.of(moduleFrom, moduleTo);
		Map<Submodule, Submodule> copies = editionService.copyEditionSubmodules(moduleCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(submoduleA, submoduleB);
		assertThat(moduleTo.getSubmodules()).containsAll(copies.values());
		assertThat(submoduleRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous module was not changed
		SCModule old = moduleRepository.findByIdOrThrow(moduleFrom.getId());
		assertThat(old).isEqualTo(moduleFrom);
		assertThat(old.getSubmodules()).containsExactlyInAnyOrder(submoduleA, submoduleB);

		testSubmoduleEqualityHelper(submoduleA, copies.get(submoduleA), moduleTo);
		testSubmoduleEqualityHelper(submoduleB, copies.get(submoduleB), moduleTo);
	}

	private void testSubmoduleEqualityHelper(Submodule initial, Submodule copy, SCModule moduleTo) {
		assertThat(copy).isNotNull();
		assertThat(submoduleRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getName()).isEqualTo(initial.getName());
		assertThat(copy.getModule()).isEqualTo(moduleTo);
		assertThat(copy.getRow()).isEqualTo(initial.getRow());
		assertThat(copy.getColumn()).isEqualTo(initial.getColumn());

		// No skills should have been added for now
		assertThat(copy.getSkills()).isEmpty();

		// Safety check that the previous submodule was not changed
		assertThat(submoduleRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	public void testCopyEditionSkills() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add a module to each edition
		SCModule moduleFrom = moduleRepository
				.save(SCModule.builder().name("Module from").edition(from).build());
		from.getModules().add(moduleFrom);
		SCModule moduleTo = moduleRepository.save(SCModule.builder().name("Module to").edition(from).build());
		to.getModules().add(moduleTo);

		// Add submodule to each module
		Submodule submoduleFrom = Submodule.builder().module(moduleFrom).name("Submodule from").column(0)
				.row(0)
				.build();
		submoduleFrom = submoduleRepository.save(submoduleFrom);
		moduleFrom.getSubmodules().add(submoduleFrom);
		Submodule submoduleTo = Submodule.builder().module(moduleFrom).name("Submodule to").column(0).row(0)
				.build();
		submoduleTo = submoduleRepository.save(submoduleTo);
		moduleTo.getSubmodules().add(submoduleTo);

		// Add two checkpoints to each edition
		Checkpoint checkpointFromA = Checkpoint.builder().edition(from).name("Checkpoint from A")
				.deadline(localDateTime).build();
		checkpointFromA = checkpointRepository.save(checkpointFromA);
		from.getCheckpoints().add(checkpointFromA);
		Checkpoint checkpointFromB = Checkpoint.builder().edition(from).name("Checkpoint from B")
				.deadline(localDateTime).build();
		checkpointFromB = checkpointRepository.save(checkpointFromB);
		from.getCheckpoints().add(checkpointFromB);
		Checkpoint checkpointToA = Checkpoint.builder().edition(to).name("Checkpoint to A")
				.deadline(localDateTime)
				.build();
		checkpointToA = checkpointRepository.save(checkpointToA);
		to.getCheckpoints().add(checkpointToA);
		Checkpoint checkpointToB = Checkpoint.builder().edition(to).name("Checkpoint to B")
				.deadline(localDateTime)
				.build();
		checkpointToB = checkpointRepository.save(checkpointToB);
		to.getCheckpoints().add(checkpointToB);

		// Add skills to the submodule, with different properties, and some linked tasks and an external skill to
		// assure that it does not break functionality
		Skill skillA = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointFromA).name("Skill A")
				.column(0)
				.row(0).build();
		skillA = skillRepository.save(skillA);
		submoduleFrom.getSkills().add(skillA);
		checkpointFromA.getSkills().add(skillA);
		Skill skillB = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointFromB).name("Skill B")
				.column(0)
				.row(0).build();
		skillB = skillRepository.save(skillB);
		submoduleFrom.getSkills().add(skillB);
		checkpointFromB.getSkills().add(skillB);
		// Add task to skill B
		Task task = Task.builder().skill(skillB).name("Task").build();
		task = taskRepository.save(task);
		// Make task required for skill A
		skillB.getTasks().add(task);
		skillA.getRequiredTasks().add(task);
		// Add an external skill linking to skill A
		ExternalSkill externalSkill = ExternalSkill.builder().skill(skillA)
				.module(db.getModuleProofTechniques())
				.column(0).row(0).build();
		externalSkill = abstractSkillRepository.save(externalSkill);
		skillA.getExternalSkills().add(externalSkill);
		db.getModuleProofTechniques().getExternalSkills().add(externalSkill);

		int amountBefore = skillRepository.findAll().size();
		Map<Submodule, Submodule> submoduleCopies = Map.of(submoduleFrom, submoduleTo);
		Map<Checkpoint, Checkpoint> checkpointCopies = Map.of(checkpointFromA, checkpointToA, checkpointFromB,
				checkpointToB);
		Map<Skill, Skill> copies = editionService.copyAndLinkEditionSkills(submoduleCopies, checkpointCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(skillA, skillB);
		assertThat(submoduleTo.getSkills()).containsAll(copies.values());
		assertThat(skillRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous submodule was not changed
		Submodule old = submoduleRepository.findByIdOrThrow(submoduleFrom.getId());
		assertThat(old).isEqualTo(submoduleFrom);
		assertThat(old.getSkills()).containsExactlyInAnyOrder(skillA, skillB);

		testSkillEqualityHelper(skillA, copies.get(skillA), submoduleTo, checkpointToA);
		testSkillEqualityHelper(skillB, copies.get(skillB), submoduleTo, checkpointToB);
	}

	private void testSkillEqualityHelper(Skill initial, Skill copy, Submodule submoduleTo,
			Checkpoint checkpointTo) {
		assertThat(copy).isNotNull();
		assertThat(abstractSkillRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getName()).isEqualTo(initial.getName());
		assertThat(copy.isEssential()).isEqualTo(initial.isEssential());
		assertThat(copy.isHidden()).isEqualTo(initial.isHidden());
		assertThat(copy.getSubmodule()).isEqualTo(submoduleTo);
		assertThat(copy.getCheckpoint()).isEqualTo(checkpointTo);
		assertThat(copy.getRow()).isEqualTo(initial.getRow());
		assertThat(copy.getColumn()).isEqualTo(initial.getColumn());

		// No tasks/external skills should have been added for now, and no parents/children were linked
		assertThat(copy.getTasks()).isEmpty();
		assertThat(copy.getRequiredTasks()).isEmpty();
		assertThat(copy.getExternalSkills()).isEmpty();
		assertThat(copy.getParents()).isEmpty();
		assertThat(copy.getChildren()).isEmpty();

		// Check that the previous/next editions skills were added correctly
		assertThat(initial.getFutureEditionSkills()).containsExactly(copy);
		assertThat(copy.getPreviousEditionSkill()).isEqualTo(initial);

		// Safety check that the previous skill was not changed
		assertThat(abstractSkillRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	public void testCopyEditionExternalSkills() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add a module to each edition
		SCModule moduleFrom = moduleRepository
				.save(SCModule.builder().name("Module from").edition(from).build());
		from.getModules().add(moduleFrom);
		SCModule moduleTo = moduleRepository.save(SCModule.builder().name("Module to").edition(from).build());
		to.getModules().add(moduleTo);

		// Add submodule to each module
		Submodule submoduleFrom = Submodule.builder().module(moduleFrom).name("Submodule from").column(0)
				.row(0)
				.build();
		submoduleFrom = submoduleRepository.save(submoduleFrom);
		moduleFrom.getSubmodules().add(submoduleFrom);
		Submodule submoduleTo = Submodule.builder().module(moduleFrom).name("Submodule to").column(0).row(0)
				.build();
		submoduleTo = submoduleRepository.save(submoduleTo);
		moduleTo.getSubmodules().add(submoduleTo);

		// Add a checkpoint to each edition
		Checkpoint checkpointFrom = Checkpoint.builder().edition(from).name("Checkpoint from")
				.deadline(localDateTime).build();
		checkpointFrom = checkpointRepository.save(checkpointFrom);
		from.getCheckpoints().add(checkpointFrom);
		Checkpoint checkpointTo = Checkpoint.builder().edition(to).name("Checkpoint to")
				.deadline(localDateTime)
				.build();
		checkpointTo = checkpointRepository.save(checkpointTo);
		to.getCheckpoints().add(checkpointTo);

		// Add a skill to each submodule
		Skill skillFrom = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointFrom)
				.name("Skill from").column(0)
				.row(0).build();
		skillFrom = skillRepository.save(skillFrom);
		submoduleFrom.getSkills().add(skillFrom);
		checkpointFrom.getSkills().add(skillFrom);
		Skill skillTo = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointTo).name("Skill to")
				.column(0)
				.row(0).build();
		skillTo = skillRepository.save(skillTo);
		submoduleTo.getSkills().add(skillTo);
		checkpointTo.getSkills().add(skillTo);

		// Add two external skills: One linking to a skill in the same edition (here in the same module, the ext. skill
		// is in, which would not be a situation in reality), and one linking to a module in another edition
		ExternalSkill externalSkillA = ExternalSkill.builder().skill(skillFrom).module(moduleFrom).column(0)
				.row(0)
				.build();
		externalSkillA = abstractSkillRepository.save(externalSkillA);
		skillFrom.getExternalSkills().add(externalSkillA);
		moduleFrom.getExternalSkills().add(externalSkillA);
		ExternalSkill externalSkillB = ExternalSkill.builder().skill(db.getSkillAssumption())
				.module(moduleFrom)
				.column(0).row(0).build();
		externalSkillB = abstractSkillRepository.save(externalSkillB);
		db.getSkillAssumption().getExternalSkills().add(externalSkillB);
		moduleFrom.getExternalSkills().add(externalSkillB);

		int amountBefore = abstractSkillRepository.findAll().size();
		Map<SCModule, SCModule> moduleCopies = Map.of(moduleFrom, moduleTo);
		Map<Skill, Skill> skillCopies = Map.of(skillFrom, skillTo);
		Map<ExternalSkill, ExternalSkill> copies = editionService.copyAndLinkEditionExternalSkills(
				moduleCopies,
				skillCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(externalSkillA, externalSkillB);
		assertThat(moduleTo.getExternalSkills()).containsAll(copies.values());
		assertThat(abstractSkillRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous module was not changed
		SCModule old = moduleRepository.findByIdOrThrow(moduleFrom.getId());
		assertThat(old).isEqualTo(moduleFrom);
		assertThat(old.getExternalSkills()).containsExactlyInAnyOrder(externalSkillA, externalSkillB);

		testExternalSkillEqualityHelper(externalSkillA, copies.get(externalSkillA), moduleTo, skillTo);
		testExternalSkillEqualityHelper(externalSkillB, copies.get(externalSkillB), moduleTo,
				db.getSkillAssumption());
	}

	private void testExternalSkillEqualityHelper(ExternalSkill initial, ExternalSkill copy, SCModule moduleTo,
			Skill shouldLinkTo) {
		assertThat(copy).isNotNull();
		assertThat(abstractSkillRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getRow()).isEqualTo(initial.getRow());
		assertThat(copy.getColumn()).isEqualTo(initial.getColumn());
		assertThat(copy.getModule()).isEqualTo(moduleTo);
		assertThat(copy.getSkill()).isEqualTo(shouldLinkTo);
		assertThat(shouldLinkTo.getExternalSkills()).contains(copy);

		// No parents/children should have been linked for now
		assertThat(copy.getParents()).isEmpty();
		assertThat(copy.getChildren()).isEmpty();

		// Safety check that the previous external skill was not changed
		assertThat(abstractSkillRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	public void testLinkParentsChildrenEditionSkills() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add a module to each edition
		SCModule moduleFrom = moduleRepository
				.save(SCModule.builder().name("Module from").edition(from).build());
		from.getModules().add(moduleFrom);
		SCModule moduleTo = moduleRepository.save(SCModule.builder().name("Module to").edition(from).build());
		to.getModules().add(moduleTo);

		// Add submodule to each module
		Submodule submoduleFrom = Submodule.builder().module(moduleFrom).name("Submodule from").column(0)
				.row(0).build();
		submoduleFrom = submoduleRepository.save(submoduleFrom);
		moduleFrom.getSubmodules().add(submoduleFrom);
		Submodule submoduleTo = Submodule.builder().module(moduleFrom).name("Submodule to").column(0).row(0)
				.build();
		submoduleTo = submoduleRepository.save(submoduleTo);
		moduleTo.getSubmodules().add(submoduleTo);

		// Add a checkpoint to each edition
		Checkpoint checkpointFrom = Checkpoint.builder().edition(from).name("Checkpoint from")
				.deadline(localDateTime).build();
		checkpointFrom = checkpointRepository.save(checkpointFrom);
		from.getCheckpoints().add(checkpointFrom);
		Checkpoint checkpointTo = Checkpoint.builder().edition(to).name("Checkpoint to")
				.deadline(localDateTime)
				.build();
		checkpointTo = checkpointRepository.save(checkpointTo);
		to.getCheckpoints().add(checkpointTo);

		// Add an external skill to the modules and a skill to the submodules, one being the parent of the other
		Skill skillFrom = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointFrom)
				.name("Skill from").column(0).row(0).build();
		skillFrom = skillRepository.save(skillFrom);
		submoduleFrom.getSkills().add(skillFrom);
		checkpointFrom.getSkills().add(skillFrom);
		Skill skillTo = Skill.builder().submodule(submoduleTo).checkpoint(checkpointTo).name("Skill to")
				.column(0).row(0).build();
		skillTo = skillRepository.save(skillTo);
		submoduleTo.getSkills().add(skillTo);
		checkpointTo.getSkills().add(skillTo);
		// Add external skill
		ExternalSkill externalSkillFrom = ExternalSkill.builder().skill(db.getSkillAssumption())
				.module(moduleFrom).column(0).row(0).build();
		externalSkillFrom = abstractSkillRepository.save(externalSkillFrom);
		db.getSkillAssumption().getExternalSkills().add(externalSkillFrom);
		moduleFrom.getExternalSkills().add(externalSkillFrom);
		ExternalSkill externalSkillTo = ExternalSkill.builder().skill(db.getSkillAssumption())
				.module(moduleFrom).column(0).row(0).build();
		externalSkillTo = abstractSkillRepository.save(externalSkillTo);
		db.getSkillAssumption().getExternalSkills().add(externalSkillTo);
		moduleTo.getExternalSkills().add(externalSkillTo);
		// Link parent/child relationship
		externalSkillFrom.getParents().add(skillFrom);
		skillFrom.getChildren().add(externalSkillFrom);

		int amountBefore = skillRepository.findAll().size();
		Map<AbstractSkill, AbstractSkill> skillCopies = Map.of(skillFrom, skillTo, externalSkillFrom,
				externalSkillTo);
		editionService.linkParentsChildrenSkills(skillCopies);
		assertThat(skillCopies.keySet()).containsExactlyInAnyOrder(skillFrom, externalSkillFrom);
		assertThat(skillCopies.get(skillFrom)).isEqualTo(skillTo);
		assertThat(skillCopies.get(externalSkillFrom)).isEqualTo(externalSkillTo);
		assertThat(skillRepository.findAll()).hasSize(amountBefore);
		assertThat(externalSkillTo.getParents()).containsExactly(skillTo);
		assertThat(skillTo.getChildren()).containsExactly(externalSkillTo);
		assertThat(externalSkillTo.getChildren()).isEmpty();
		assertThat(skillTo.getParents()).isEmpty();

		// Safety check that the previous skills were not changed
		AbstractSkill oldSkill = abstractSkillRepository.findByIdOrThrow(skillFrom.getId());
		assertThat(oldSkill).isEqualTo(skillFrom);
		assertThat(oldSkill.getChildren()).containsExactly(externalSkillFrom);
		assertThat(oldSkill.getParents()).isEmpty();
		AbstractSkill oldExtSkill = abstractSkillRepository.findByIdOrThrow(externalSkillFrom.getId());
		assertThat(oldExtSkill).isEqualTo(externalSkillFrom);
		assertThat(oldExtSkill.getParents()).containsExactly(skillFrom);
		assertThat(oldExtSkill.getChildren()).isEmpty();
	}

	@Test
	public void testCopyEditionTasks() {
		Long idInUse = db.getEditionRL().getId();
		SCEdition from = editionRepository.save(SCEdition.builder().id(idInUse + 1).build());
		SCEdition to = editionRepository.save(SCEdition.builder().id(idInUse + 2).build());

		// Add a module to each edition
		SCModule moduleFrom = moduleRepository
				.save(SCModule.builder().name("Module from").edition(from).build());
		from.getModules().add(moduleFrom);
		SCModule moduleTo = moduleRepository.save(SCModule.builder().name("Module to").edition(from).build());
		to.getModules().add(moduleTo);

		// Add submodule to each module
		Submodule submoduleFrom = Submodule.builder().module(moduleFrom).name("Submodule from").column(0)
				.row(0)
				.build();
		submoduleFrom = submoduleRepository.save(submoduleFrom);
		moduleFrom.getSubmodules().add(submoduleFrom);
		Submodule submoduleTo = Submodule.builder().module(moduleFrom).name("Submodule to").column(0).row(0)
				.build();
		submoduleTo = submoduleRepository.save(submoduleTo);
		moduleTo.getSubmodules().add(submoduleTo);

		// Add a checkpoint to each edition
		Checkpoint checkpointFrom = Checkpoint.builder().edition(from).name("Checkpoint from")
				.deadline(localDateTime).build();
		checkpointFrom = checkpointRepository.save(checkpointFrom);
		from.getCheckpoints().add(checkpointFrom);
		Checkpoint checkpointTo = Checkpoint.builder().edition(to).name("Checkpoint to")
				.deadline(localDateTime)
				.build();
		checkpointTo = checkpointRepository.save(checkpointTo);
		to.getCheckpoints().add(checkpointTo);

		// Add two skills to each submodule
		Skill skillFromA = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointFrom)
				.name("Skill from A").column(0).row(0).build();
		skillFromA = skillRepository.save(skillFromA);
		submoduleFrom.getSkills().add(skillFromA);
		checkpointFrom.getSkills().add(skillFromA);
		Skill skillFromB = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointFrom)
				.name("Skill from B").column(0).row(0).build();
		skillFromB = skillRepository.save(skillFromB);
		submoduleFrom.getSkills().add(skillFromB);
		checkpointFrom.getSkills().add(skillFromB);

		Skill skillToA = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointTo).name("Skill to A")
				.column(0).row(0).build();
		skillToA = skillRepository.save(skillToA);
		submoduleTo.getSkills().add(skillToA);
		checkpointTo.getSkills().add(skillToA);
		Skill skillToB = Skill.builder().submodule(submoduleFrom).checkpoint(checkpointTo).name("Skill to B")
				.column(0).row(0).build();
		skillToB = skillRepository.save(skillToB);
		submoduleTo.getSkills().add(skillToB);
		checkpointTo.getSkills().add(skillToB);

		// Add a path to each edition
		Path pathFrom = pathRepository.save(Path.builder().name("Path from").edition(from).build());
		from.getPaths().add(pathFrom);
		Path pathTo = pathRepository.save(Path.builder().name("Path to").edition(to).build());
		to.getPaths().add(pathTo);

		// Add one task in the path, and one task without path in the different skills, one task being required for the
		// other skill
		Task taskA = Task.builder().skill(skillFromA).name("Task A").paths(Set.of(pathFrom)).build();
		taskA = taskRepository.save(taskA);
		pathFrom.getTasks().add(taskA);
		// Make task required for skill B
		skillFromA.getTasks().add(taskA);
		skillFromB.getRequiredTasks().add(taskA);
		taskA.getRequiredFor().add(skillFromB);
		Task taskB = Task.builder().skill(skillFromB).name("Task B").build();
		taskB = taskRepository.save(taskB);
		skillFromB.getTasks().add(taskB);

		int amountBefore = taskRepository.findAll().size();
		Map<Skill, Skill> skillCopies = Map.of(skillFromA, skillToA, skillFromB, skillToB);
		Map<Path, Path> pathCopies = Map.of(pathFrom, pathTo);
		Map<Task, Task> copies = editionService.copyAndLinkEditionTasks(skillCopies, pathCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(taskA, taskB);
		assertThat(taskRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous skills were not changed
		Skill oldA = skillRepository.findByIdOrThrow(skillFromA.getId());
		assertThat(oldA).isEqualTo(skillFromA);
		assertThat(oldA.getTasks()).containsExactly(taskA);
		Skill oldB = skillRepository.findByIdOrThrow(skillFromB.getId());
		assertThat(oldB).isEqualTo(skillFromB);
		assertThat(oldB.getRequiredTasks()).containsExactly(taskA);
		assertThat(oldB.getTasks()).containsExactly(taskB);

		testTaskEqualityHelper(taskA, copies.get(taskA), skillToA);
		testTaskEqualityHelper(taskB, copies.get(taskB), skillToB);

		assertThat(skillToB.getRequiredTasks()).containsExactly(copies.get(taskA));
		assertThat(skillToA.getRequiredTasks()).isEmpty();
		assertThat(copies.get(taskA).getRequiredFor()).containsExactly(skillToB);
		assertThat(copies.get(taskB).getRequiredFor()).isEmpty();
		assertThat(copies.get(taskA).getPaths()).containsExactly(pathTo);
		assertThat(copies.get(taskB).getPaths()).isEmpty();
		assertThat(pathTo.getTasks()).containsExactly(copies.get(taskA));
	}

	private void testTaskEqualityHelper(Task initial, Task copy, Skill skillTo) {
		assertThat(copy).isNotNull();
		assertThat(taskRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		assertThat(copy.getName()).isEqualTo(initial.getName());
		assertThat(copy.getType()).isEqualTo(initial.getType());
		assertThat(copy.getTime()).isEqualTo(initial.getTime());
		assertThat(copy.getLink()).isEqualTo(initial.getLink());
		assertThat(copy.getIdx()).isEqualTo(initial.getIdx());
		assertThat(skillTo.getTasks()).containsExactly(copy);

		// Safety check that the previous task was not changed
		assertThat(taskRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}
}
