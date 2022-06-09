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
package nl.tudelft.skills.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import nl.tudelft.skills.model.Badge;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.SCCourse;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.BadgeRepository;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.InventoryRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class TestDatabaseLoader {

	/**
	 * ID constants.
	 */
	private final Long COURSE_ID = 30L;
	private final Long EDITION_ID = 69L;

	/**
	 * Repositories.
	 */
	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private EditionRepository editionRepository;
	@Autowired
	private ModuleRepository moduleRepository;
	@Autowired
	private SubmoduleRepository submoduleRepository;
	@Autowired
	private SkillRepository skillRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private BadgeRepository badgeRepository;
	@Autowired
	private InventoryRepository inventoryRepository;
	@Autowired
	private PathRepository pathRepository;
	@Autowired
	private CheckpointRepository checkpointRepository;

	/**
	 * Test models.
	 */
	private SCCourse courseRL = SCCourse.builder().build();

	private SCEdition editionRL2021 = SCEdition.builder().build();

	private SCModule moduleProofTechniques = SCModule.builder().name("Proof Techniques")
			.build();

	private Submodule submoduleLogicBasics = Submodule.builder().name("Logic Basics")
			.row(0).column(0).build();
	private Submodule submoduleGeneralisation = Submodule.builder()
			.name("Generalisation").row(1).column(0).build();
	private Submodule submoduleCases = Submodule.builder().name("Cases").row(0)
			.column(1).build();
	private Submodule submoduleContradiction = Submodule.builder().name("Contradiction")
			.row(1).column(1).build();
	private Submodule submoduleContrapositive = Submodule.builder()
			.name("Contrapositive").row(0).column(2).build();
	private Submodule submoduleInduction = Submodule.builder().name("Induction").row(1)
			.column(2).build();

	private Checkpoint checkpointLectureOne = Checkpoint.builder().name("Lecture 1")
			.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT))).build();
	private Checkpoint checkpointLectureTwo = Checkpoint.builder().name("Lecture 2")
			.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 49), LocalTime.MIDNIGHT))).build();

	private Skill skillImplication = Skill.builder().name("Implication").row(0)
			.column(0).build();
	private Skill skillNegation = Skill.builder().name("Negation").row(0).column(2)
			.build();
	private Skill skillVariables = Skill.builder().name("Variables").row(0).column(4)
			.build();
	private Skill skillProofOutline = Skill.builder().name("Proof Outline").row(1)
			.column(3).build();
	private Skill skillAssumption = Skill.builder().name("Assumption").row(1).column(1)
			.build();
	private Skill skillGeneralisationPractice = Skill.builder()
			.name("Generalisation Practice").row(2).column(3).build();
	private Skill skillDividingIntoCases = Skill.builder().name("Dividing into Cases")
			.row(2).column(1).build();
	private Skill skillCasesPractice = Skill.builder().name("Cases Practice").row(3)
			.column(2).build();
	private Skill skillContradictionPractice = Skill.builder()
			.name("Contradiction Practice").row(3).column(3).build();
	private Skill skillNegateImplications = Skill.builder().name("Negate Implications")
			.row(4).column(1).build();
	private Skill skillContrapositivePractice = Skill.builder()
			.name("Contrapositive Practice").row(5).column(1).build();
	private Skill skillTransitiveProperty = Skill.builder().name("Transitive Property")
			.row(3).column(0).build();
	private Skill skillInductionPractice = Skill.builder().name("Induction Practice")
			.row(6).column(2).build();

	private Task taskRead12 = Task.builder().name("Read chapter 1.2").time(1).build();
	private Task taskDo12ae = Task.builder().name("Do exercise 1.2a-e").time(3).build();
	private Task taskRead11 = Task.builder().name("Read chapter 1.1").time(5).build();
	private Task taskDo11ad = Task.builder().name("Do exercise 1.1a-d").time(7).build();
	private Task taskRead10 = Task.builder().name("Read chapter 1.0").time(13).build();
	private Task taskDo10a = Task.builder().name("Do exercise 1.0a").time(20).build();

	private Badge badge1 = Badge.builder().name("Badge 1").inventories(new HashSet<>()).build();
	private Badge badge2 = Badge.builder().name("Badge 2").inventories(new HashSet<>()).build();

	private Inventory inventory = Inventory.builder().build();

	private SCPerson person = SCPerson.builder().id(TestUserDetailsService.id).build();

	public SCCourse getCourseRL() {
		return courseRepository.findByIdOrThrow(courseRL.getId());
	}

	public SCEdition getEditionRL() {
		return editionRepository.findByIdOrThrow(editionRL2021.getId());
	}

	public SCModule getModuleProofTechniques() {
		return moduleRepository.findByIdOrThrow(moduleProofTechniques.getId());
	}

	public Submodule getSubmoduleLogicBasics() {
		return submoduleRepository.findByIdOrThrow(submoduleLogicBasics.getId());
	}

	public Submodule getSubmoduleGeneralisation() {
		return submoduleRepository.findByIdOrThrow(submoduleGeneralisation.getId());
	}

	public Submodule getSubmoduleCases() {
		return submoduleRepository.findByIdOrThrow(submoduleCases.getId());
	}

	public Submodule getSubmoduleContradiction() {
		return submoduleRepository.findByIdOrThrow(submoduleContradiction.getId());
	}

	public Submodule getSubmoduleContrapositive() {
		return submoduleRepository.findByIdOrThrow(submoduleContrapositive.getId());
	}

	public Submodule getSubmoduleInduction() {
		return submoduleRepository.findByIdOrThrow(submoduleInduction.getId());
	}

	public Checkpoint getCheckpointLectureOne() {
		return checkpointRepository.findByIdOrThrow(checkpointLectureOne.getId());
	}

	public Checkpoint getCheckpointLectureTwo() {
		return checkpointRepository.findByIdOrThrow(checkpointLectureTwo.getId());
	}

	public Skill getSkillImplication() {
		return skillRepository.findByIdOrThrow(skillImplication.getId());
	}

	public Skill getSkillNegation() {
		return skillRepository.findByIdOrThrow(skillNegation.getId());
	}

	public Skill getSkillVariables() {
		return skillRepository.findByIdOrThrow(skillVariables.getId());
	}

	public Skill getSkillProofOutline() {
		return skillRepository.findByIdOrThrow(skillProofOutline.getId());
	}

	public Skill getSkillAssumption() {
		return skillRepository.findByIdOrThrow(skillAssumption.getId());
	}

	public Skill getSkillGeneralisationPractice() {
		return skillRepository.findByIdOrThrow(skillGeneralisationPractice.getId());
	}

	public Skill getSkillDividingIntoCases() {
		return skillRepository.findByIdOrThrow(skillDividingIntoCases.getId());
	}

	public Skill getSkillCasesPractice() {
		return skillRepository.findByIdOrThrow(skillCasesPractice.getId());
	}

	public Skill getSkillContradictionPractice() {
		return skillRepository.findByIdOrThrow(skillContradictionPractice.getId());
	}

	public Skill getSkillNegateImplications() {
		return skillRepository.findByIdOrThrow(skillNegateImplications.getId());
	}

	public Skill getSkillContrapositivePractice() {
		return skillRepository.findByIdOrThrow(skillContrapositivePractice.getId());
	}

	public Skill getSkillTransitiveProperty() {
		return skillRepository.findByIdOrThrow(skillTransitiveProperty.getId());
	}

	public Skill getSkillInductionPractice() {
		return skillRepository.findByIdOrThrow(skillInductionPractice.getId());
	}

	public Task getTaskRead12() {
		return taskRepository.findByIdOrThrow(taskRead12.getId());
	}

	public Task getTaskDo12ae() {
		return taskRepository.findByIdOrThrow(taskDo12ae.getId());
	}

	public Task getTaskRead11() {
		return taskRepository.findByIdOrThrow(taskRead11.getId());
	}

	public Task getTaskDo11ad() {
		return taskRepository.findByIdOrThrow(taskDo11ad.getId());
	}

	public Task getTaskRead10() {
		return taskRepository.findByIdOrThrow(taskRead10.getId());
	}

	public Task getTaskDo10a() {
		return taskRepository.findByIdOrThrow(taskDo10a.getId());
	}

	public Badge getBadge1() {
		return badgeRepository.findByIdOrThrow(badge1.getId());
	}

	public Badge getBadge2() {
		return badgeRepository.findByIdOrThrow(badge2.getId());
	}

	public Inventory getInventory() {
		return inventoryRepository.findByIdOrThrow(inventory.getId());
	}

	public SCPerson getPerson() {
		return personRepository.findByIdOrThrow(person.getId());
	}

	@PostConstruct
	private void init() {
		initCourse();
		initEdition();
		initModule();
		initSubmodule();
		initCheckpoint();
		initSkill();
		initTask();
		initPerson();
		initBadges();
		initInventory();
	}

	private void initCourse() {
		courseRL.setId(COURSE_ID);
		courseRL = courseRepository.save(courseRL);
	}

	private void initEdition() {
		editionRL2021.setId(EDITION_ID);
		editionRL2021 = editionRepository.save(editionRL2021);
	}

	private void initModule() {
		moduleProofTechniques.setEdition(editionRL2021);
		moduleProofTechniques = moduleRepository.save(moduleProofTechniques);
	}

	private void initSubmodule() {
		submoduleLogicBasics.setModule(moduleProofTechniques);
		submoduleLogicBasics = submoduleRepository.save(submoduleLogicBasics);
		submoduleGeneralisation.setModule(moduleProofTechniques);
		submoduleGeneralisation = submoduleRepository.save(submoduleGeneralisation);
		submoduleCases.setModule(moduleProofTechniques);
		submoduleCases = submoduleRepository.save(submoduleCases);
		submoduleContradiction.setModule(moduleProofTechniques);
		submoduleContradiction = submoduleRepository.save(submoduleContradiction);
		submoduleContrapositive.setModule(moduleProofTechniques);
		submoduleContrapositive = submoduleRepository.save(submoduleContrapositive);
		submoduleInduction.setModule(moduleProofTechniques);
		submoduleInduction = submoduleRepository.save(submoduleInduction);
	}

	private void initCheckpoint() {
		checkpointLectureOne.setEdition(this.getEditionRL());
		checkpointLectureOne = checkpointRepository.save(checkpointLectureOne);

		checkpointLectureTwo.setEdition(this.getEditionRL());
		checkpointLectureTwo = checkpointRepository.save(checkpointLectureTwo);
	}

	private void initSkill() {
		skillImplication.setSubmodule(submoduleLogicBasics);
		skillImplication.setCheckpoint(checkpointLectureOne);
		skillImplication = skillRepository.save(skillImplication);
		skillNegation.setSubmodule(submoduleLogicBasics);
		skillNegation.setCheckpoint(checkpointLectureOne);
		skillNegation = skillRepository.save(skillNegation);
		skillVariables.setSubmodule(submoduleLogicBasics);
		skillVariables.setCheckpoint(checkpointLectureOne);
		skillVariables = skillRepository.save(skillVariables);
		skillProofOutline.setSubmodule(submoduleGeneralisation);
		skillProofOutline.setCheckpoint(checkpointLectureOne);
		skillProofOutline = skillRepository.save(skillProofOutline);
		skillAssumption.setSubmodule(submoduleGeneralisation);
		skillAssumption.setCheckpoint(checkpointLectureOne);
		skillAssumption.setParents(Set.of(skillImplication));
		skillAssumption = skillRepository.save(skillAssumption);
		skillGeneralisationPractice.setSubmodule(submoduleGeneralisation);
		skillGeneralisationPractice
				.setParents(Set.of(skillAssumption, skillProofOutline, skillVariables));
		skillGeneralisationPractice.setCheckpoint(checkpointLectureTwo);
		skillGeneralisationPractice = skillRepository.save(skillGeneralisationPractice);
		skillDividingIntoCases.setSubmodule(submoduleCases);
		skillDividingIntoCases.setCheckpoint(checkpointLectureTwo);
		skillDividingIntoCases = skillRepository.save(skillDividingIntoCases);
		skillCasesPractice.setSubmodule(submoduleCases);
		skillCasesPractice.setCheckpoint(checkpointLectureTwo);
		skillCasesPractice.setParents(Set.of(skillProofOutline, skillDividingIntoCases));
		skillCasesPractice = skillRepository.save(skillCasesPractice);
		skillContradictionPractice.setSubmodule(submoduleContradiction);
		skillContradictionPractice.setCheckpoint(checkpointLectureTwo);
		skillContradictionPractice.setParents(Set.of(skillProofOutline, skillNegation));
		skillContradictionPractice = skillRepository.save(skillContradictionPractice);
		skillNegateImplications.setSubmodule(submoduleContrapositive);
		skillNegateImplications.setCheckpoint(checkpointLectureTwo);
		skillNegateImplications.setParents(Set.of(skillNegation, skillImplication));
		skillNegateImplications = skillRepository.save(skillNegateImplications);
		skillContrapositivePractice.setSubmodule(submoduleContrapositive);
		skillContrapositivePractice.setCheckpoint(checkpointLectureTwo);
		skillContrapositivePractice.setParents(Set.of(skillProofOutline, skillNegateImplications));
		skillContrapositivePractice = skillRepository.save(skillContrapositivePractice);
		skillTransitiveProperty.setSubmodule(submoduleInduction);
		skillTransitiveProperty.setCheckpoint(checkpointLectureTwo);
		skillTransitiveProperty.setParents(Set.of(skillImplication));
		skillTransitiveProperty = skillRepository.save(skillTransitiveProperty);
		skillInductionPractice.setSubmodule(submoduleInduction);
		skillInductionPractice.setCheckpoint(checkpointLectureTwo);
		skillInductionPractice
				.setParents(Set.of(skillTransitiveProperty, skillProofOutline, skillDividingIntoCases));
		skillInductionPractice = skillRepository.save(skillInductionPractice);

		checkpointLectureOne = checkpointRepository.save(checkpointLectureOne);
		checkpointLectureTwo = checkpointRepository.save(checkpointLectureTwo);
	}

	private void initTask() {
		taskRead12.setSkill(skillImplication);
		taskDo12ae.setSkill(skillImplication);
		taskRead12 = taskRepository.save(taskRead12);
		taskDo12ae = taskRepository.save(taskDo12ae);

		taskRead11.setSkill(skillNegation);
		taskDo11ad.setSkill(skillNegation);
		taskRead11 = taskRepository.save(taskRead11);
		taskDo11ad = taskRepository.save(taskDo11ad);

		taskRead10.setSkill(skillVariables);
		taskDo10a.setSkill(skillVariables);
		taskRead10 = taskRepository.save(taskRead10);
		taskDo10a = taskRepository.save(taskDo10a);
	}

	private void initPerson() {
		person.setTasksCompleted(Set.of(taskDo11ad, taskRead12, taskDo12ae, taskRead11));
		person = personRepository.save(person);
	}

	private void initInventory() {
		inventory.setPersonId(this.getPerson().getId());
		inventory = inventoryRepository.save(inventory);

		person.setInventory(inventory);
		personRepository.save(person);
	}

	private void initBadges() {
		badge1 = badgeRepository.save(badge1);
		badge2 = badgeRepository.save(badge2);
	}
}
