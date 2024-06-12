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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CheckpointServiceTest {

	private final TestDatabaseLoader db;
	private final CheckpointService checkpointService;
	private final CheckpointRepository checkpointRepository;
	private final ModuleRepository moduleRepository;
	private final SkillRepository skillRepository;
	private final SubmoduleRepository submoduleRepository;
	private final EditionRepository editionRepository;

	@Autowired
	public CheckpointServiceTest(TestDatabaseLoader testDatabaseLoader,
			CheckpointRepository checkpointRepository,
			CheckpointService checkpointService, ModuleRepository moduleRepository,
			SkillRepository skillRepository, SubmoduleRepository submoduleRepository,
			EditionRepository editionRepository) {
		this.db = testDatabaseLoader;
		this.checkpointService = checkpointService;
		this.checkpointRepository = checkpointRepository;
		this.moduleRepository = moduleRepository;
		this.skillRepository = skillRepository;
		this.submoduleRepository = submoduleRepository;
		this.editionRepository = editionRepository;
	}

	@Test
	public void testFindNextCheckpointInModuleLast() {
		Optional<Checkpoint> nextCheckpoint = checkpointService.findNextCheckpointInModule(
				db.getCheckpointLectureTwo(),
				db.getModuleProofTechniques());
		assertThat(nextCheckpoint).isEmpty();
	}

	@Test
	public void testFindNextCheckpointInModuleNoSkillsInModule() {
		SCModule module = SCModule.builder().name("Module").build();
		Optional<Checkpoint> nextCheckpoint = checkpointService.findNextCheckpointInModule(
				db.getCheckpointLectureTwo(),
				module);
		assertThat(nextCheckpoint).isEmpty();
	}

	@Test
	public void testFindNextCheckpointInModuleNoSkillsInCheckpoint() {
		Checkpoint emptyCheckpoint = Checkpoint.builder().name("Empty").build();
		checkpointRepository.save(emptyCheckpoint);
		Optional<Checkpoint> nextCheckpoint = checkpointService.findNextCheckpointInModule(
				emptyCheckpoint,
				db.getModuleProofTechniques());
		assertThat(nextCheckpoint).isEmpty();
	}

	@Test
	public void testFindNextCheckpointInModuleNonEmpty() {
		// Create a "disconnected" skill in the lowest row, with new checkpoint
		Checkpoint checkpoint = Checkpoint.builder().edition(db.getEditionRL()).name("Checkpoint")
				.deadline(LocalDateTime.now()).build();
		Skill skill = Skill.builder().name("Skill").row(20).column(1).checkpoint(checkpoint)
				.submodule(db.getSubmoduleCases()).build();
		db.getSubmoduleCases().getSkills().add(skill);
		checkpointRepository.save(checkpoint);
		skillRepository.save(skill);
		checkpoint.getSkills().add(skill);
		checkpointRepository.save(checkpoint);
		submoduleRepository.save(db.getSubmoduleCases());

		// Even with no skill connection, the next checkpoint after lecture 2 should be the new one
		Optional<Checkpoint> lastCheckpoint = checkpointService.findNextCheckpointInModule(
				db.getCheckpointLectureTwo(),
				db.getModuleProofTechniques());
		assertThat(lastCheckpoint).isNotEmpty();
		assertThat(lastCheckpoint).hasValue(checkpoint);

		// Even with three checkpoints, the next checkpoint after lecture 1 should be lecture 2
		// This also checks that the connection of skills does not play a role
		Optional<Checkpoint> middleCheckpoint = checkpointService.findNextCheckpointInModule(
				db.getCheckpointLectureOne(),
				db.getModuleProofTechniques());
		assertThat(middleCheckpoint).isNotEmpty();
		assertThat(middleCheckpoint).hasValue(db.getCheckpointLectureTwo());
	}

	@Test
	public void testDeleteSkillsFalse() {
		Set<Skill> skillsBefore = db.getCheckpointLectureTwo().getSkills();
		boolean retVal = checkpointService.deleteSkillsFromCheckpoint(db.getCheckpointLectureTwo(),
				Set.of(db.getSkillDividingIntoCases()), db.getModuleProofTechniques().getId());

		assertThat(retVal).isFalse();
		assertThat(db.getSkillDividingIntoCases().getCheckpoint()).isEqualTo(db.getCheckpointLectureTwo());
		assertThat(db.getCheckpointLectureTwo().getSkills()).containsAll(skillsBefore);
	}

	@Test
	public void testDeleteSkillsTrue() {
		Set<Skill> skillsBeforeOne = db.getCheckpointLectureOne().getSkills();
		Set<Skill> skillsBeforeTwo = db.getCheckpointLectureTwo().getSkills();
		boolean retVal = checkpointService.deleteSkillsFromCheckpoint(db.getCheckpointLectureOne(),
				Set.of(db.getSkillNegation()), db.getModuleProofTechniques().getId());

		assertThat(retVal).isTrue();
		assertThat(db.getSkillNegation().getCheckpoint()).isEqualTo(db.getCheckpointLectureTwo());
		skillsBeforeOne.remove(db.getSkillNegation());
		skillsBeforeTwo.add(db.getSkillNegation());
		assertThat(db.getCheckpointLectureOne().getSkills()).containsAll(skillsBeforeOne);
		assertThat(db.getCheckpointLectureTwo().getSkills()).containsAll(skillsBeforeTwo);
	}

	@Test
	public void testDeleteSkillsTrueAlsoDeleteCheckpoint() {
		Long id = db.getCheckpointLectureOne().getId();
		Set<Skill> skillsBeforeOne = db.getCheckpointLectureOne().getSkills();
		Set<Skill> skillsBeforeTwo = db.getCheckpointLectureTwo().getSkills();
		boolean retVal = checkpointService.deleteSkillsFromCheckpoint(db.getCheckpointLectureOne(),
				skillsBeforeOne, db.getModuleProofTechniques().getId());

		assertThat(retVal).isTrue();
		for (Skill skill : skillsBeforeOne) {
			assertThat(skill.getCheckpoint()).isEqualTo(db.getCheckpointLectureTwo());
		}
		assertThat(checkpointRepository.existsById(id)).isFalse();
		skillsBeforeTwo.addAll(skillsBeforeOne);
		assertThat(db.getCheckpointLectureTwo().getSkills()).containsAll(skillsBeforeTwo);
	}

	@Test
	public void testDeleteCheckpointSuccessful() {
		// Module 1: checkpointA, checkpointB
		// Module 2: checkpointA, checkpointB

		Checkpoint checkpointA = db.getCheckpointLectureOne();
		Checkpoint checkpointB = db.getCheckpointLectureTwo();

		db.createSkillsInNewModuleHelper(checkpointA, checkpointB);

		boolean retVal = checkpointService.deleteCheckpoint(checkpointA);
		assertThat(retVal).isTrue();
		assertThat(checkpointRepository.existsById(checkpointA.getId())).isFalse();
		checkpointB.getSkills().addAll(checkpointA.getSkills());
		assertThat(checkpointRepository.findByIdOrThrow(checkpointB.getId()).getSkills())
				.containsAll(checkpointB.getSkills());
	}

	@Test
	public void testDeleteCheckpointLastInOne() {
		// Module 1: checkpointA, checkpointB
		// Module 2: checkpointB, checkpointA

		Checkpoint checkpointA = db.getCheckpointLectureOne();
		Checkpoint checkpointB = db.getCheckpointLectureTwo();

		db.createSkillsInNewModuleHelper(checkpointB, checkpointA);

		boolean retVal = checkpointService.deleteCheckpoint(checkpointA);
		assertThat(retVal).isFalse();
		assertThat(checkpointRepository.findByIdOrThrow(checkpointA.getId()).getSkills())
				.containsAll(checkpointA.getSkills());
		assertThat(checkpointRepository.findByIdOrThrow(checkpointB.getId()).getSkills())
				.containsAll(checkpointB.getSkills());
	}

	@Test
	public void testDeleteCheckpointLastInMultiple() {
		// Module 1: checkpointB, checkpointA
		// Module 2: checkpointB, checkpointA

		Checkpoint checkpointA = db.getCheckpointLectureTwo();
		Checkpoint checkpointB = db.getCheckpointLectureOne();

		db.createSkillsInNewModuleHelper(checkpointB, checkpointA);

		boolean retVal = checkpointService.deleteCheckpoint(checkpointA);
		assertThat(retVal).isFalse();
		assertThat(checkpointRepository.findByIdOrThrow(checkpointA.getId()).getSkills())
				.containsAll(checkpointA.getSkills());
		assertThat(checkpointRepository.findByIdOrThrow(checkpointB.getId()).getSkills())
				.containsAll(checkpointB.getSkills());
	}
}
