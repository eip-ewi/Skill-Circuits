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
package nl.tudelft.skills;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
@Profile("dev")
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection") // Can be disabled as this class is not tested.
public class DevDatabaseLoader {

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
	private RegularTaskRepository regularTaskRepository;
	@Autowired
	private CheckpointRepository checkpointRepository;
	@Autowired
	private BadgeRepository badgeRepository;
	@Autowired
	private InventoryRepository inventoryRepository;
	@Autowired
	private CourseControllerApi courseControllerApi;
	@Autowired
	private EditionControllerApi editionApi;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private PathRepository pathRepository;
	@Autowired
	private RoleControllerApi roleControllerApi;

	private SCPerson person = SCPerson.builder().id(1L).build();

	private EditionDetailsDTO editionOOPDetails;
	private EditionDetailsDTO editionADSDetails;

	private CourseDetailsDTO courseOOPDetails;
	private CourseDetailsDTO courseADSDetails;
	private SCEdition scEditionOOP;
	private SCEdition scEditionADS;

	private Path pathFinderPath;
	private Path explorerPath;

	private SCCourse scCourseOOP;
	private SCCourse scCourseADS;

	private SCModule moduleProofTechniques;
	private SCModule modulePropositionalLogic;
	private SCModule moduleSimple;

	private Submodule submoduleLogicBasics;
	private Submodule submoduleGeneralisation;
	private Submodule submoduleCases;
	private Submodule submoduleContradiction;
	private Submodule submoduleContrapositive;
	private Submodule submoduleInduction;
	private Submodule submoduleSimple;

	private Skill skillImplication;
	private Skill skillNegation;

	private Skill skillVariables;
	private Skill skillProofOutline;
	private Skill skillGeneralisationPractice;
	private Skill skillAssumption;

	private Skill skillDividingIntoCases;
	private Skill skillCasesPractice;

	private Skill skillContradictionPractice;

	private Skill skillNegateImplications;
	private Skill skillContrapositivePractice;

	private Skill skillTransitiveProperty;
	private Skill skillInductionPractice;

	private Skill skillSimpleA;
	private Skill skillSimpleB;
	private Skill skillSimpleC;
	private Skill skillSimpleD;
	private Skill skillSimpleE;
	private Skill skillSimpleF;

	private Inventory inventory;

	private Checkpoint checkpointLectureOne;
	private Checkpoint checkpointLectureTwo;
	private Checkpoint checkpointSimple;

	private Badge badge;

	@PostConstruct
	private void init() {
		courseOOPDetails = courseControllerApi.getCourseById(1L).block();
		// Find the most recent edition to add the modules to (which should be the active one)
		// This should not produce exceptions if there is at least one edition (and courseOOPDetails != null)
		editionOOPDetails = editionApi.getAllEditionsByCourse(courseOOPDetails.getId()).collectList().block()
				.stream().max(Comparator.comparing(EditionDetailsDTO::getEndDate)).get();

		courseADSDetails = courseControllerApi.getCourseById(2L).block();
		editionADSDetails = editionApi.getAllEditionsByCourse(courseADSDetails.getId()).blockFirst();

		initCourse();
		initEdition();
		initPaths();
		initModules();
		initSubmodules();
		initCheckpoints();
		initSkills();
		initTasks();
		initBadges();
		initPerson();
	}

	private void initPerson() {
		Inventory inventory = Inventory.builder().build();
		person.setInventory(inventory);
		inventory.setPerson(person);

		person = personRepository.save(person);
		// Add cseTeacher1 also as teacher for ADS

		editionApi.getAllEditionsByCourse(scCourseADS.getId()).toStream()
				.forEach(e -> roleControllerApi.addRole(new RoleCreateDTO()
						.edition(new EditionIdDTO().id(e.getId()))
						.type(RoleCreateDTO.TypeEnum.TEACHER).person(new PersonIdDTO().id(3L))).block());

	}

	private void initCourse() {
		scCourseOOP = courseRepository.save(SCCourse.builder()
				.id(editionOOPDetails.getCourse().getId())
				.build());

		scCourseADS = courseRepository.save(SCCourse.builder()
				.id(editionADSDetails.getCourse().getId())
				.build());
	}

	private void initEdition() {
		scEditionOOP = editionRepository.save(SCEdition.builder()
				.id(editionOOPDetails.getId())
				.build());
		scEditionADS = editionRepository.save(SCEdition.builder()
				.id(editionADSDetails.getId())
				.build());
	}

	private void initPaths() {
		pathFinderPath = pathRepository.save(Path.builder()
				.edition(scEditionOOP)
				.name("Pathfinder")
				.build());
		explorerPath = pathRepository.save(Path.builder()
				.edition(scEditionOOP)
				.name("Explorer")
				.build());
	}

	private void initModules() {
		moduleProofTechniques = moduleRepository.save(SCModule.builder()
				.name("Proof Techniques")
				.edition(scEditionOOP)
				.build());
		modulePropositionalLogic = moduleRepository.save(SCModule.builder()
				.name("Propositional Logic")
				.edition(scEditionOOP)
				.build());
		moduleSimple = moduleRepository.save(SCModule.builder()
				.name("Simple Module")
				.edition(scEditionOOP)
				.build());
	}

	private void initSubmodules() {
		submoduleLogicBasics = submoduleRepository.save(Submodule.builder()
				.name("Logic Basics")
				.module(moduleProofTechniques)
				.row(0)
				.column(0)
				.build());
		submoduleGeneralisation = submoduleRepository.save(Submodule.builder()
				.name("Generalisation")
				.module(moduleProofTechniques)
				.row(1)
				.column(1)
				.build());
		submoduleCases = submoduleRepository.save(Submodule.builder()
				.name("Cases")
				.module(moduleProofTechniques)
				.row(2)
				.column(2)
				.build());
		submoduleContradiction = submoduleRepository.save(Submodule.builder()
				.name("Contradiction")
				.module(moduleProofTechniques)
				.row(3)
				.column(1)
				.build());
		submoduleContrapositive = submoduleRepository.save(Submodule.builder()
				.name("Contrapositive")
				.module(moduleProofTechniques)
				.row(2)
				.column(3)
				.build());
		submoduleInduction = submoduleRepository.save(Submodule.builder()
				.name("Induction")
				.module(moduleProofTechniques)
				.row(3)
				.column(0)
				.build());
		submoduleSimple = submoduleRepository.save(Submodule.builder()
				.name("Simple Module")
				.module(moduleSimple)
				.row(0)
				.column(3)
				.build());
	}

	private void initSkills() {
		skillImplication = skillRepository.save(Skill.builder()
				.name("Implication")
				.submodule(submoduleLogicBasics)
				.row(0).column(0)
				.checkpoint(checkpointLectureOne)
				.build());
		skillNegation = skillRepository.save(Skill.builder()
				.name("Negation")
				.submodule(submoduleLogicBasics)
				.row(0).column(2)
				.checkpoint(checkpointLectureOne)
				.build());
		skillVariables = skillRepository.save(Skill.builder()
				.name("Variables")
				.submodule(submoduleLogicBasics)
				.checkpoint(checkpointLectureOne)
				.row(0).column(4)
				.build());

		skillProofOutline = skillRepository.save(Skill.builder()
				.name("Proof Outline")
				.submodule(submoduleGeneralisation)
				.checkpoint(checkpointLectureOne)
				.row(1).column(3)
				.build());
		skillAssumption = skillRepository.save(Skill.builder()
				.name("Assumption")
				.submodule(submoduleGeneralisation)
				.row(1).column(1)
				.checkpoint(checkpointLectureOne)
				.parents(Set.of(skillImplication))
				.build());
		skillGeneralisationPractice = skillRepository.save(Skill.builder()
				.name("Generalisation Practice")
				.submodule(submoduleGeneralisation)
				.row(2).column(3)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillAssumption, skillProofOutline, skillVariables))
				.build());

		skillDividingIntoCases = skillRepository.save(Skill.builder()
				.name("Dividing into Cases")
				.submodule(submoduleCases)
				.row(2).column(1)
				.checkpoint(checkpointLectureTwo)
				.build());
		skillCasesPractice = skillRepository.save(Skill.builder()
				.name("Cases Practice")
				.submodule(submoduleCases)
				.row(3).column(2)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillGeneralisationPractice, skillDividingIntoCases))
				.build());

		skillContradictionPractice = skillRepository.save(Skill.builder()
				.name("Contradiction Practice")
				.submodule(submoduleContradiction)
				.row(3).column(3)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillGeneralisationPractice, skillNegation))
				.build());

		skillTransitiveProperty = skillRepository.save(Skill.builder()
				.name("Transitive Property")
				.submodule(submoduleInduction)
				.row(3).column(0)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillImplication))
				.build());

		skillNegateImplications = skillRepository.save(Skill.builder()
				.name("Negate Implications")
				.submodule(submoduleContrapositive)
				.row(4).column(1)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillTransitiveProperty, skillCasesPractice))
				.build());
		skillContrapositivePractice = skillRepository.save(Skill.builder()
				.name("Contrapositive Practice")
				.submodule(submoduleContrapositive)
				.row(5).column(1)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillNegateImplications, skillContradictionPractice))
				.build());

		skillInductionPractice = skillRepository.save(Skill.builder()
				.name("Induction Practice")
				.submodule(submoduleInduction)
				.row(6).column(2)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillContradictionPractice, skillContrapositivePractice))
				.build());

		skillSimpleA = skillRepository.save(Skill.builder()
				.name("Skill A")
				.submodule(submoduleSimple)
				.row(0).column(1)
				.checkpoint(checkpointSimple)
				.build());
		skillSimpleB = skillRepository.save(Skill.builder()
				.name("Skill B")
				.submodule(submoduleSimple)
				.row(1).column(0)
				.parents(Set.of(skillSimpleA))
				.checkpoint(checkpointSimple)
				.build());
		skillSimpleC = skillRepository.save(Skill.builder()
				.name("Skill C")
				.submodule(submoduleSimple)
				.row(1).column(2)
				.parents(Set.of(skillSimpleA))
				.checkpoint(checkpointSimple)
				.build());
		skillSimpleD = skillRepository.save(Skill.builder()
				.name("Skill D")
				.submodule(submoduleSimple)
				.row(2).column(0)
				.parents(Set.of(skillSimpleB))
				.checkpoint(checkpointSimple)
				.build());
		skillSimpleE = skillRepository.save(Skill.builder()
				.name("Skill E")
				.submodule(submoduleSimple)
				.row(2).column(1)
				.parents(Set.of(skillSimpleB))
				.checkpoint(checkpointSimple)
				.build());
		skillSimpleF = skillRepository.save(Skill.builder()
				.name("Skill F")
				.submodule(submoduleSimple)
				.row(2).column(2)
				.parents(Set.of(skillSimpleB))
				.checkpoint(checkpointSimple)
				.build());
	}

	private void initTasks() {
		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 1.2").skill(skillImplication).time(7)
						.type(TaskType.READING).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 1.2a-e").skill(skillImplication).time(10)
						.type(TaskType.EXERCISE).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository.save(RegularTask.builder().name("Read chapter 1.1").skill(skillNegation)
				.time(10)
				.link("https://docs.oracle.com/en/java/javase/17/docs/api/index.html").type(TaskType.READING)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 1.1a-d").skill(skillNegation).time(10).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 1.0").skill(skillVariables).time(10)
						.type(TaskType.READING).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 1.0a").skill(skillVariables).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.0").skill(skillProofOutline).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository.save(RegularTask.builder().name("Do exercise 2.0a-f").skill(skillProofOutline)
				.time(10)
				.type(TaskType.COLLABORATION).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository.save(
				RegularTask.builder().name("Watch lecture 1").skill(skillProofOutline).time(10)
						.type(TaskType.VIDEO)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.1").skill(skillAssumption).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 2.1a-g").skill(skillAssumption).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.2").skill(skillGeneralisationPractice)
						.time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 2.2a-b").skill(skillGeneralisationPractice)
						.time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("TA Check 1").skill(skillGeneralisationPractice).time(10)
						.type(TaskType.QUIZ).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.3").skill(skillDividingIntoCases).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Watch lecture 2").skill(skillDividingIntoCases).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 2.3a-d").skill(skillCasesPractice).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.4").skill(skillContradictionPractice)
						.time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Watch lecture 3").skill(skillContradictionPractice).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("TA Check 2").skill(skillContradictionPractice).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.5").skill(skillNegateImplications).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Watch video 1").skill(skillContrapositivePractice).time(10)
						.link("https://www.youtube.com/watch?v=dQw4w9WgXcQ").build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 2.5a").skill(skillContrapositivePractice)
						.time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Read chapter 2.5").skill(skillTransitiveProperty).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 2.5a").skill(skillTransitiveProperty).time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Watch video 2: dominos").skill(skillTransitiveProperty)
						.time(10)
						.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Watch lecture 4").skill(skillInductionPractice).time(10)
						.type(TaskType.VIDEO).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Do exercise 2.5b-d").skill(skillInductionPractice).time(10)
						.type(TaskType.EXERCISE).build());
		regularTaskRepository.save(RegularTask.builder().name("Read").skill(skillInductionPractice).time(10)
				.type(TaskType.READING).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Project 1").skill(skillInductionPractice).time(10)
						.type(TaskType.COLLABORATION).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Test yourself!").skill(skillInductionPractice).time(10)
						.type(TaskType.QUIZ).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Implement DFS").skill(skillInductionPractice).time(10)
						.type(TaskType.IMPLEMENTATION).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Experiment with run time").skill(skillInductionPractice)
						.time(10)
						.type(TaskType.EXPERIMENT).build());

		regularTaskRepository.save(RegularTask.builder().name("Task 1").skill(skillSimpleA).time(10).build());
		regularTaskRepository.save(RegularTask.builder().name("Task 2").skill(skillSimpleA).time(10).build());

		regularTaskRepository.save(RegularTask.builder().name("Task 3").skill(skillSimpleB).time(10).build());
		regularTaskRepository.save(RegularTask.builder().name("Task 4").skill(skillSimpleB).time(10).build());

		regularTaskRepository.save(RegularTask.builder().name("Task 5").skill(skillSimpleC).time(10).build());
		regularTaskRepository.save(RegularTask.builder().name("Task 6").skill(skillSimpleC).time(10).build());

		regularTaskRepository.save(RegularTask.builder().name("Task 7").skill(skillSimpleD).time(10).build());
		regularTaskRepository.save(RegularTask.builder().name("Task 8").skill(skillSimpleD).time(10).build());

		regularTaskRepository.save(RegularTask.builder().name("Task 9").skill(skillSimpleE).time(10).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Task 10").skill(skillSimpleE).time(10).build());

		regularTaskRepository
				.save(RegularTask.builder().name("Task 11").skill(skillSimpleF).time(10).build());
		regularTaskRepository
				.save(RegularTask.builder().name("Task 12").skill(skillSimpleF).time(10).build());
	}

	private void initCheckpoints() {
		checkpointLectureOne = checkpointRepository.save(Checkpoint.builder()
				.name("Lecture 1")
				.edition(scEditionOOP)
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT))
				.build());

		checkpointLectureTwo = checkpointRepository.save(Checkpoint.builder()
				.name("Lecture 2")
				.edition(scEditionOOP)
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 49), LocalTime.MIDNIGHT))
				.build());

		checkpointSimple = checkpointRepository.save(Checkpoint.builder()
				.name("Simple")
				.edition(scEditionOOP)
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 53), LocalTime.MIDNIGHT))
				.build());
	}

	private void initBadges() {
		badge = badgeRepository
				.save(Badge.builder().name("Your first badge!").build());
	}

}
