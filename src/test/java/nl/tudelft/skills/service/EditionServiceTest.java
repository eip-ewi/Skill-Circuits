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
		assertThat(editionRepository.findByIdOrThrow(from.getId())).isEqualTo(from);

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
		Task task = Task.builder().skill(skill).build();
		skill.getTasks().add(task);
		pathB.getTasks().add(task);

		from.getPaths().addAll(Set.of(pathA, pathB));

		int amountBefore = pathRepository.findAll().size();
		Map<Path, Path> copies = editionService.copyEditionPaths(from, to);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(pathA, pathB);
		assertThat(pathRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		assertThat(editionRepository.findByIdOrThrow(from.getId())).isEqualTo(from);

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

		int amountBefore = moduleRepository.findAll().size();
		Map<SCModule, SCModule> copies = editionService.copyEditionModules(from, to);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(moduleA, moduleB);
		assertThat(moduleRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		assertThat(editionRepository.findByIdOrThrow(from.getId())).isEqualTo(from);

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
		assertThat(submoduleRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the editions were not changed
		assertThat(editionRepository.findByIdOrThrow(from.getId())).isEqualTo(from);
		assertThat(editionRepository.findByIdOrThrow(to.getId())).isEqualTo(to);
		// Safety check that the previous module was not changed
		assertThat(moduleRepository.findByIdOrThrow(moduleFrom.getId())).isEqualTo(moduleFrom);

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

		// Safety check that the previous module was not changed
		assertThat(moduleRepository.findByIdOrThrow(moduleTo.getId())).isEqualTo(moduleTo);
	}

}
