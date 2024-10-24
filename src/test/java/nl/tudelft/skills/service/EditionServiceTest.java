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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionSummaryDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.test.CopyEditionTestDatabaseLoader;
import nl.tudelft.skills.test.TestDatabaseLoader;
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
	private final SkillRepository skillRepository;
	private final RegularTaskRepository regularTaskRepository;

	private final TestDatabaseLoader db;
	private final CopyEditionTestDatabaseLoader editionDb;
	private final LocalDateTime localDateTime;

	@Autowired
	public EditionServiceTest(EditionControllerApi editionApi, EditionRepository editionRepository,
			CircuitService circuitService, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, ModuleRepository moduleRepository,
			SubmoduleRepository submoduleRepository, AbstractSkillRepository abstractSkillRepository,
			SkillRepository skillRepository, RegularTaskRepository regularTaskRepository,
			TestDatabaseLoader db,
			CopyEditionTestDatabaseLoader editionDb) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.moduleRepository = moduleRepository;
		this.submoduleRepository = submoduleRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.skillRepository = skillRepository;
		this.regularTaskRepository = regularTaskRepository;

		this.db = db;
		this.editionDb = editionDb;
		this.localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);
		editionService = new EditionService(editionApi, editionRepository, circuitService,
				checkpointRepository, pathRepository, moduleRepository, submoduleRepository,
				abstractSkillRepository, skillRepository, regularTaskRepository);
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
	@Transactional
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
	@Transactional
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
	@Transactional
	public void testCopyEditionCheckpoints() {
		editionDb.initEditionFrom(db.getSkillAssumption());

		int amountBefore = checkpointRepository.findAll().size();
		Map<Checkpoint, Checkpoint> copies = editionService.copyEditionCheckpoints(editionDb.getFrom(),
				editionDb.getTo());
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getCheckpointFromA(),
				editionDb.getCheckpointFromB());
		assertThat(checkpointRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		SCEdition old = editionRepository.findByIdOrThrow(editionDb.getFrom().getId());
		assertThat(old).isEqualTo(editionDb.getFrom());
		assertThat(editionDb.getTo().getCheckpoints()).containsAll(copies.values());
		assertThat(old.getCheckpoints()).containsExactlyInAnyOrder(editionDb.getCheckpointFromA(),
				editionDb.getCheckpointFromB());

		testCheckpointEqualityHelper(editionDb.getCheckpointFromA(),
				copies.get(editionDb.getCheckpointFromA()),
				editionDb.getTo());
		testCheckpointEqualityHelper(editionDb.getCheckpointFromA(),
				copies.get(editionDb.getCheckpointFromA()),
				editionDb.getTo());
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
	@Transactional
	public void testCopyEditionPaths() {
		editionDb.initEditionFrom(db.getSkillAssumption());

		int amountBefore = pathRepository.findAll().size();
		Map<Path, Path> copies = editionService.copyEditionPaths(editionDb.getFrom(), editionDb.getTo());
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getPathFromA(),
				editionDb.getPathFromB());
		assertThat(editionDb.getTo().getPaths()).containsAll(copies.values());
		assertThat(pathRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		SCEdition old = editionRepository.findByIdOrThrow(editionDb.getFrom().getId());
		assertThat(old).isEqualTo(editionDb.getFrom());
		assertThat(old.getPaths()).containsExactlyInAnyOrder(editionDb.getPathFromA(),
				editionDb.getPathFromB());

		testPathEqualityHelper(editionDb.getPathFromA(), copies.get(editionDb.getPathFromA()),
				editionDb.getTo());
		testPathEqualityHelper(editionDb.getPathFromB(), copies.get(editionDb.getPathFromB()),
				editionDb.getTo());
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
	@Transactional
	public void testCopyEditionModules() {
		editionDb.initEditionFrom(db.getSkillAssumption());

		int amountBefore = moduleRepository.findAll().size();
		Map<SCModule, SCModule> copies = editionService.copyEditionModules(editionDb.getFrom(),
				editionDb.getTo());
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getModuleFromA(),
				editionDb.getModuleFromB());
		assertThat(editionDb.getTo().getModules()).containsAll(copies.values());
		assertThat(moduleRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous edition was not changed
		SCEdition old = editionRepository.findByIdOrThrow(editionDb.getFrom().getId());
		assertThat(old).isEqualTo(editionDb.getFrom());
		assertThat(old.getModules()).containsExactlyInAnyOrder(editionDb.getModuleFromA(),
				editionDb.getModuleFromB());

		testModuleEqualityHelper(editionDb.getModuleFromA(), copies.get(editionDb.getModuleFromA()),
				editionDb.getTo());
		testModuleEqualityHelper(editionDb.getModuleFromB(), copies.get(editionDb.getModuleFromB()),
				editionDb.getTo());
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
	@Transactional
	public void testCopyEditionSubmodules() {
		editionDb.initEditionFrom(db.getSkillAssumption());
		editionDb.initModules(false);

		int amountBefore = submoduleRepository.findAll().size();
		Map<SCModule, SCModule> moduleCopies = Map.of(editionDb.getModuleFromA(), editionDb.getModuleToA(),
				editionDb.getModuleFromB(), editionDb.getModuleToB());
		Map<Submodule, Submodule> copies = editionService.copyEditionSubmodules(moduleCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getSubmoduleFromA(),
				editionDb.getSubmoduleFromB());
		assertThat(editionDb.getModuleToA().getSubmodules()).containsAll(copies.values());
		assertThat(editionDb.getModuleToB().getSubmodules()).isEmpty();
		assertThat(submoduleRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous modules were not changed
		SCModule oldA = moduleRepository.findByIdOrThrow(editionDb.getModuleFromA().getId());
		assertThat(oldA).isEqualTo(editionDb.getModuleFromA());
		assertThat(oldA.getSubmodules()).containsExactlyInAnyOrder(editionDb.getSubmoduleFromA(),
				editionDb.getSubmoduleFromB());
		SCModule oldB = moduleRepository.findByIdOrThrow(editionDb.getModuleFromB().getId());
		assertThat(oldB).isEqualTo(editionDb.getModuleFromB());
		assertThat(oldB.getSubmodules()).isEmpty();

		testSubmoduleEqualityHelper(editionDb.getSubmoduleFromA(), copies.get(editionDb.getSubmoduleFromA()),
				editionDb.getModuleToA());
		testSubmoduleEqualityHelper(editionDb.getSubmoduleFromB(), copies.get(editionDb.getSubmoduleFromB()),
				editionDb.getModuleToA());
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
	@Transactional
	public void testCopyEditionSkills() {
		editionDb.initEditionFrom(db.getSkillAssumption());
		editionDb.initModules(false);
		editionDb.initSubmodules(false);
		editionDb.initCheckpoints(false);

		// Add an external skill linking to skill A
		ExternalSkill externalSkill = ExternalSkill.builder().skill(editionDb.getSkillFromA())
				.module(db.getModuleProofTechniques()).column(0).row(0).build();
		externalSkill = abstractSkillRepository.save(externalSkill);
		editionDb.getSkillFromA().getExternalSkills().add(externalSkill);
		db.getModuleProofTechniques().getExternalSkills().add(externalSkill);

		int amountBefore = skillRepository.findAll().size();
		Map<Submodule, Submodule> submoduleCopies = Map.of(editionDb.getSubmoduleFromA(),
				editionDb.getSubmoduleToA(),
				editionDb.getSubmoduleFromB(), editionDb.getSubmoduleToB());
		Map<Checkpoint, Checkpoint> checkpointCopies = Map.of(editionDb.getCheckpointFromA(),
				editionDb.getCheckpointToA(), editionDb.getCheckpointFromB(), editionDb.getCheckpointToB());
		Map<Skill, Skill> copies = editionService.copyAndLinkEditionSkills(submoduleCopies, checkpointCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getSkillFromA(),
				editionDb.getSkillFromB());
		assertThat(editionDb.getSubmoduleToA().getSkills()).containsAll(copies.values());
		assertThat(editionDb.getSubmoduleToB().getSkills()).isEmpty();
		assertThat(editionDb.getCheckpointToA().getSkills()).containsAll(copies.values());
		assertThat(editionDb.getCheckpointToB().getSkills()).isEmpty();
		assertThat(skillRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous submodules were not changed
		Submodule oldA = submoduleRepository.findByIdOrThrow(editionDb.getSubmoduleFromA().getId());
		assertThat(oldA).isEqualTo(editionDb.getSubmoduleFromA());
		assertThat(oldA.getSkills()).containsExactlyInAnyOrder(editionDb.getSkillFromA(),
				editionDb.getSkillFromB());
		Submodule oldB = submoduleRepository.findByIdOrThrow(editionDb.getSubmoduleFromB().getId());
		assertThat(oldB).isEqualTo(editionDb.getSubmoduleFromB());
		assertThat(oldB.getSkills()).isEmpty();

		// Safety check that the previous checkpoints were not changed
		Checkpoint oldCheckpointA = checkpointRepository
				.findByIdOrThrow(editionDb.getCheckpointFromA().getId());
		assertThat(oldCheckpointA).isEqualTo(editionDb.getCheckpointFromA());
		assertThat(oldCheckpointA.getSkills()).containsExactlyInAnyOrder(editionDb.getSkillFromA(),
				editionDb.getSkillFromB());
		Checkpoint oldCheckpointB = checkpointRepository
				.findByIdOrThrow(editionDb.getCheckpointFromB().getId());
		assertThat(oldCheckpointB).isEqualTo(editionDb.getCheckpointFromB());
		assertThat(oldCheckpointB.getSkills()).isEmpty();

		testSkillEqualityHelper(editionDb.getSkillFromA(), copies.get(editionDb.getSkillFromA()),
				editionDb.getSubmoduleToA(), editionDb.getCheckpointToA());
		testSkillEqualityHelper(editionDb.getSkillFromB(), copies.get(editionDb.getSkillFromB()),
				editionDb.getSubmoduleToA(), editionDb.getCheckpointToA());
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
	@Transactional
	public void testCopyEditionExternalSkills() {
		editionDb.initEditionFrom(db.getSkillAssumption());
		editionDb.initModules(false);
		editionDb.initSubmodules(false);
		editionDb.initCheckpoints(false);
		editionDb.initSkills(false);

		int amountBefore = abstractSkillRepository.findAll().size();
		Map<SCModule, SCModule> moduleCopies = Map.of(editionDb.getModuleFromA(), editionDb.getModuleToA(),
				editionDb.getModuleFromB(), editionDb.getModuleToB());
		Map<Skill, Skill> skillCopies = Map.of(editionDb.getSkillFromA(), editionDb.getSkillToA(),
				editionDb.getSkillFromB(), editionDb.getSkillToB());
		Map<ExternalSkill, ExternalSkill> copies = editionService.copyAndLinkEditionExternalSkills(
				moduleCopies, skillCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getExtSkillFromA(),
				editionDb.getExtSkillFromB());
		assertThat(abstractSkillRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous modules were not changed
		SCModule oldA = moduleRepository.findByIdOrThrow(editionDb.getModuleFromA().getId());
		assertThat(oldA).isEqualTo(editionDb.getModuleFromA());
		assertThat(oldA.getExternalSkills()).containsExactly(editionDb.getExtSkillFromB());
		SCModule oldB = moduleRepository.findByIdOrThrow(editionDb.getModuleFromB().getId());
		assertThat(oldB).isEqualTo(editionDb.getModuleFromB());
		assertThat(oldB.getExternalSkills()).containsExactly(editionDb.getExtSkillFromA());

		// Safety check that the previous skills were not changed
		Skill oldSkillA = skillRepository.findByIdOrThrow(editionDb.getSkillFromA().getId());
		assertThat(oldSkillA).isEqualTo(editionDb.getSkillFromA());
		assertThat(oldSkillA.getExternalSkills()).containsExactly(editionDb.getExtSkillFromA());
		Skill oldSkillB = skillRepository.findByIdOrThrow(editionDb.getSkillFromB().getId());
		assertThat(oldSkillB).isEqualTo(editionDb.getSkillFromB());
		assertThat(oldSkillB.getExternalSkills()).isEmpty();

		testExternalSkillEqualityHelper(editionDb.getExtSkillFromA(),
				copies.get(editionDb.getExtSkillFromA()),
				editionDb.getModuleToB(), editionDb.getSkillToA());
		testExternalSkillEqualityHelper(editionDb.getExtSkillFromB(),
				copies.get(editionDb.getExtSkillFromB()),
				editionDb.getModuleToA(), db.getSkillAssumption());

		assertThat(db.getSkillAssumption().getExternalSkills()).containsExactlyInAnyOrder(
				copies.get(editionDb.getExtSkillFromB()), editionDb.getExtSkillFromB());
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
		assertThat(moduleTo.getExternalSkills()).containsExactly(copy);

		// No parents/children should have been linked for now
		assertThat(copy.getParents()).isEmpty();
		assertThat(copy.getChildren()).isEmpty();

		// Safety check that the previous external skill was not changed
		assertThat(abstractSkillRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}

	@Test
	@Transactional
	public void testLinkParentsChildrenEditionSkills() {
		editionDb.initEditionFrom(db.getSkillAssumption());
		editionDb.initModules(false);
		editionDb.initSubmodules(false);
		editionDb.initCheckpoints(false);
		editionDb.initSkills(false);
		editionDb.initExternalSkillOtherEdition(false, db.getSkillAssumption());
		editionDb.initExternalSkillSameEdition(false);

		int amountBefore = skillRepository.findAll().size();
		Map<AbstractSkill, AbstractSkill> skillCopies = Map.of(editionDb.getSkillFromA(),
				editionDb.getSkillToA(),
				editionDb.getSkillFromB(), editionDb.getSkillToB(), editionDb.getExtSkillFromA(),
				editionDb.getExtSkillToA(), editionDb.getExtSkillFromB(), editionDb.getExtSkillToB());
		editionService.linkParentsChildrenSkills(skillCopies);

		// Assert that the map still contains the correct skills
		assertThat(skillCopies.keySet()).containsExactlyInAnyOrder(editionDb.getSkillFromA(),
				editionDb.getSkillFromB(), editionDb.getExtSkillFromA(), editionDb.getExtSkillFromB());
		assertThat(skillCopies.get(editionDb.getSkillFromA())).isEqualTo(editionDb.getSkillToA());
		assertThat(skillCopies.get(editionDb.getSkillFromB())).isEqualTo(editionDb.getSkillToB());
		assertThat(skillCopies.get(editionDb.getExtSkillFromA())).isEqualTo(editionDb.getExtSkillToA());
		assertThat(skillCopies.get(editionDb.getExtSkillFromB())).isEqualTo(editionDb.getExtSkillToB());
		assertThat(skillRepository.findAll()).hasSize(amountBefore);

		// Assert on the parent/child relationships
		assertThat(editionDb.getExtSkillToA().getParents()).isEmpty();
		assertThat(editionDb.getExtSkillToA().getChildren()).isEmpty();
		assertThat(editionDb.getExtSkillToB().getParents()).containsExactly(editionDb.getSkillToB());
		assertThat(editionDb.getExtSkillToB().getChildren()).isEmpty();
		assertThat(editionDb.getSkillToA().getParents()).isEmpty();
		assertThat(editionDb.getSkillToA().getChildren()).isEmpty();
		assertThat(editionDb.getSkillToB().getParents()).isEmpty();
		assertThat(editionDb.getSkillToB().getChildren()).containsExactly(editionDb.getExtSkillToB());

		// Safety check that the previous skills were not changed
		AbstractSkill oldSkillA = abstractSkillRepository.findByIdOrThrow(editionDb.getSkillFromA().getId());
		assertThat(oldSkillA).isEqualTo(editionDb.getSkillFromA());
		assertThat(oldSkillA.getChildren()).isEmpty();
		assertThat(oldSkillA.getParents()).isEmpty();
		AbstractSkill oldSkillB = abstractSkillRepository.findByIdOrThrow(editionDb.getSkillFromB().getId());
		assertThat(oldSkillB).isEqualTo(editionDb.getSkillFromB());
		assertThat(oldSkillB.getChildren()).containsExactly(editionDb.getExtSkillFromB());
		assertThat(oldSkillB.getParents()).isEmpty();
		AbstractSkill oldExtSkillA = abstractSkillRepository
				.findByIdOrThrow(editionDb.getExtSkillFromA().getId());
		assertThat(oldExtSkillA.getParents()).isEmpty();
		assertThat(oldExtSkillA.getChildren()).isEmpty();
		AbstractSkill oldExtSkillB = abstractSkillRepository
				.findByIdOrThrow(editionDb.getExtSkillFromB().getId());
		assertThat(oldExtSkillB).isEqualTo(editionDb.getExtSkillFromB());
		assertThat(oldExtSkillB.getParents()).containsExactly(editionDb.getSkillFromB());
		assertThat(oldExtSkillB.getChildren()).isEmpty();
	}

	@Test
	@Transactional
	public void testCopyEditionTasks() {
		editionDb.initEditionFrom(db.getSkillAssumption());
		editionDb.initModules(false);
		editionDb.initSubmodules(false);
		editionDb.initCheckpoints(false);
		editionDb.initPaths(false);
		editionDb.initSkills(false);
		editionDb.initExternalSkillOtherEdition(false, db.getSkillAssumption());
		editionDb.initExternalSkillSameEdition(false);
		editionDb.initParentChild(false);

		int amountBefore = regularTaskRepository.findAll().size();
		Map<Skill, Skill> skillCopies = Map.of(editionDb.getSkillFromA(), editionDb.getSkillToA(),
				editionDb.getSkillFromB(), editionDb.getSkillToB());
		Map<Path, Path> pathCopies = Map.of(editionDb.getPathFromA(), editionDb.getPathToA(),
				editionDb.getPathFromB(), editionDb.getPathToB());
		Map<Task, Task> copies = editionService.copyAndLinkEditionTasks(skillCopies,
				pathCopies);
		assertThat(copies.keySet()).containsExactlyInAnyOrder(editionDb.getTaskFromA(),
				editionDb.getTaskFromB());
		assertThat(regularTaskRepository.findAll()).hasSize(amountBefore + 2);

		// Safety check that the previous skills were not changed
		Skill oldA = skillRepository.findByIdOrThrow(editionDb.getSkillFromA().getId());
		assertThat(oldA).isEqualTo(editionDb.getSkillFromA());
		assertThat(oldA.getTasks()).containsExactly(editionDb.getTaskFromA());
		Skill oldB = skillRepository.findByIdOrThrow(editionDb.getSkillFromB().getId());
		assertThat(oldB).isEqualTo(editionDb.getSkillFromB());
		assertThat(oldB.getRequiredTasks()).containsExactly(editionDb.getTaskFromA());
		assertThat(oldB.getTasks()).containsExactly(editionDb.getTaskFromB());

		testTaskEqualityHelper(editionDb.getTaskFromA(), copies.get(editionDb.getTaskFromA()),
				editionDb.getSkillToA());
		testTaskEqualityHelper(editionDb.getTaskFromB(), copies.get(editionDb.getTaskFromB()),
				editionDb.getSkillToB());

		assertThat(editionDb.getSkillToB().getRequiredTasks())
				.containsExactly(copies.get(editionDb.getTaskFromA()));
		assertThat(editionDb.getSkillToA().getRequiredTasks()).isEmpty();
		assertThat(copies.get(editionDb.getTaskFromA()).getRequiredFor())
				.containsExactly(editionDb.getSkillToB());
		assertThat(copies.get(editionDb.getTaskFromB()).getRequiredFor()).isEmpty();
		assertThat(copies.get(editionDb.getTaskFromA()).getPaths()).containsExactly(editionDb.getPathToA());
		assertThat(copies.get(editionDb.getTaskFromB()).getPaths()).isEmpty();
		assertThat(editionDb.getPathToA().getTasks()).containsExactly(copies.get(editionDb.getTaskFromA()));
		assertThat(editionDb.getPathToB().getTasks()).isEmpty();
	}

	private void testTaskEqualityHelper(Task initial, Task copy, Skill skillTo) {
		assertThat(copy).isNotNull();
		assertThat(regularTaskRepository.findByIdOrThrow(copy.getId())).isEqualTo(copy);
		// TODO This requires more adjustments.
		if (initial instanceof RegularTask && copy instanceof RegularTask) {
			assertThat(((RegularTask) copy).getName()).isEqualTo(((RegularTask) initial).getName());
			assertThat(((RegularTask) copy).getType()).isEqualTo(((RegularTask) initial).getType());
			assertThat(((RegularTask) copy).getTime()).isEqualTo(((RegularTask) initial).getTime());
			assertThat(((RegularTask) copy).getLink()).isEqualTo(((RegularTask) initial).getLink());
		}
		assertThat(copy.getIdx()).isEqualTo(initial.getIdx());
		assertThat(skillTo.getTasks()).containsExactly(copy);

		// Safety check that the previous task was not changed
		assertThat(regularTaskRepository.findByIdOrThrow(initial.getId())).isEqualTo(initial);
	}
}
