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
	private TaskInfoRepository taskInfoRepository;
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
		TaskInfo read12Info = TaskInfo.builder().name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillImplication).taskInfo(read12Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read12Info.setTask(taskRead12);
		regularTaskRepository.save(taskRead12);

		TaskInfo do12aeInfo = TaskInfo.builder().name("Do exercise 1.2a-e")
				.time(10).type(TaskType.EXERCISE).build();
		RegularTask taskDo12ae = RegularTask.builder().skill(skillImplication).taskInfo(do12aeInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do12aeInfo.setTask(taskDo12ae);
		regularTaskRepository.save(taskDo12ae);

		TaskInfo read11Info = TaskInfo.builder().name("Read chapter 1.1").time(10)
				.link("https://docs.oracle.com/en/java/javase/17/docs/api/index.html").type(TaskType.READING)
				.build();
		RegularTask taskRead11 = RegularTask.builder().skill(skillNegation).taskInfo(read11Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read11Info.setTask(taskRead11);
		regularTaskRepository.save(taskRead11);

		TaskInfo do11adInfo = TaskInfo.builder().name("Do exercise 1.1a-d").time(10).build();
		RegularTask taskDo11ad = RegularTask.builder().skill(skillNegation).taskInfo(do11adInfo).build();
		do11adInfo.setTask(taskDo11ad);
		regularTaskRepository.save(taskDo11ad);

		TaskInfo read10Info = TaskInfo.builder().name("Read chapter 1.0").time(10).type(TaskType.READING)
				.build();
		RegularTask taskRead10 = RegularTask.builder().skill(skillVariables).taskInfo(read10Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read10Info.setTask(taskRead10);
		regularTaskRepository.save(taskRead10);

		TaskInfo do10aInfo = TaskInfo.builder().name("Do exercise 1.0a").time(10).build();
		RegularTask taskDo10a = RegularTask.builder().skill(skillVariables).taskInfo(do10aInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do10aInfo.setTask(taskDo10a);
		regularTaskRepository.save(taskDo10a);

		TaskInfo read20Info = TaskInfo.builder().name("Read chapter 2.0").time(10).build();
		RegularTask taskRead20 = RegularTask.builder().skill(skillProofOutline).taskInfo(read20Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read20Info.setTask(taskRead20);
		regularTaskRepository.save(taskRead20);

		TaskInfo do20afInfo = TaskInfo.builder().name("Do exercise 2.0a-f").time(10)
				.type(TaskType.COLLABORATION).build();
		RegularTask taskDo20af = RegularTask.builder().skill(skillProofOutline).taskInfo(do20afInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do20afInfo.setTask(taskDo20af);
		regularTaskRepository.save(taskDo20af);

		TaskInfo watch1Info = TaskInfo.builder().name("Watch lecture 1").time(10).type(TaskType.VIDEO)
				.build();
		RegularTask taskWatch1 = RegularTask.builder().skill(skillProofOutline).taskInfo(watch1Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		watch1Info.setTask(taskWatch1);
		regularTaskRepository.save(taskWatch1);

		TaskInfo read21Info = TaskInfo.builder().name("Read chapter 2.1").time(10).build();
		RegularTask taskRead21 = RegularTask.builder().skill(skillAssumption).taskInfo(read21Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read21Info.setTask(taskRead21);
		regularTaskRepository.save(taskRead21);

		TaskInfo do21agInfo = TaskInfo.builder().name("Do exercise 2.1a-g").time(10).build();
		RegularTask taskDo21ag = RegularTask.builder().skill(skillAssumption).taskInfo(do21agInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do21agInfo.setTask(taskDo21ag);
		regularTaskRepository.save(taskDo21ag);

		TaskInfo read22Info = TaskInfo.builder().name("Read chapter 2.2").time(10).build();
		RegularTask taskRead22 = RegularTask.builder().skill(skillGeneralisationPractice).taskInfo(read22Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read22Info.setTask(taskRead22);
		regularTaskRepository.save(taskRead22);

		TaskInfo do22abInfo = TaskInfo.builder().name("Do exercise 2.2a-b").time(10).build();
		RegularTask taskDo22ab = RegularTask.builder().skill(skillGeneralisationPractice).taskInfo(do22abInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do22abInfo.setTask(taskDo22ab);
		regularTaskRepository.save(taskDo22ab);

		TaskInfo taCheck1Info = TaskInfo.builder().name("TA Check 1").time(10).type(TaskType.QUIZ).build();
		RegularTask taskTacheck1 = RegularTask.builder().skill(skillGeneralisationPractice)
				.taskInfo(taCheck1Info).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		taCheck1Info.setTask(taskTacheck1);
		regularTaskRepository.save(taskTacheck1);

		TaskInfo read23Info = TaskInfo.builder().name("Read chapter 2.3").time(10).build();
		RegularTask taskRead23 = RegularTask.builder().skill(skillDividingIntoCases).taskInfo(read23Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read23Info.setTask(taskRead23);
		regularTaskRepository.save(taskRead23);

		TaskInfo watch2Info = TaskInfo.builder().name("Watch lecture 2").time(10).build();
		RegularTask taskWatch2 = RegularTask.builder().skill(skillDividingIntoCases).taskInfo(watch2Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		watch2Info.setTask(taskWatch2);
		regularTaskRepository.save(taskWatch2);

		TaskInfo do23adInfo = TaskInfo.builder().name("Do exercise 2.3a-d").time(10).build();
		RegularTask taskDo23ad = RegularTask.builder().skill(skillCasesPractice).taskInfo(do23adInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do23adInfo.setTask(taskDo23ad);
		regularTaskRepository.save(taskDo23ad);

		TaskInfo read24Info = TaskInfo.builder().name("Read chapter 2.4").time(10).build();
		RegularTask taskRead24 = RegularTask.builder().skill(skillContradictionPractice).taskInfo(read24Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read24Info.setTask(taskRead24);
		regularTaskRepository.save(taskRead24);

		TaskInfo watch3Info = TaskInfo.builder().name("Watch lecture 3").time(10).build();
		RegularTask taskWatch3 = RegularTask.builder().skill(skillContradictionPractice).taskInfo(watch3Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		watch3Info.setTask(taskWatch3);
		regularTaskRepository.save(taskWatch3);

		TaskInfo taCheck2Info = TaskInfo.builder().name("TA Check 2").time(10).build();
		RegularTask taskTacheck2 = RegularTask.builder().skill(skillContradictionPractice)
				.taskInfo(taCheck2Info).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		taCheck2Info.setTask(taskTacheck2);
		regularTaskRepository.save(taskTacheck2);

		TaskInfo read25Info = TaskInfo.builder().name("Read chapter 2.5").time(10).build();
		RegularTask taskRead25 = RegularTask.builder().skill(skillNegateImplications).taskInfo(read25Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read25Info.setTask(taskRead25);
		regularTaskRepository.save(taskRead25);

		TaskInfo watchVideo1Info = TaskInfo.builder().name("Watch video 1").time(10)
				.link("https://www.youtube.com/watch?v=dQw4w9WgXcQ").build();
		RegularTask taskWatchvideo1 = RegularTask.builder().skill(skillContrapositivePractice)
				.taskInfo(watchVideo1Info).build();
		watchVideo1Info.setTask(taskWatchvideo1);
		regularTaskRepository.save(taskWatchvideo1);

		TaskInfo do25aInfo = TaskInfo.builder().name("Do exercise 2.5a").time(10).build();
		RegularTask taskDo25a = RegularTask.builder().skill(skillContrapositivePractice).taskInfo(do25aInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do25aInfo.setTask(taskDo25a);
		regularTaskRepository.save(taskDo25a);

		TaskInfo read25Info2 = TaskInfo.builder().name("Read chapter 2.5").time(10).build();
		RegularTask taskRead252 = RegularTask.builder().skill(skillTransitiveProperty).taskInfo(read25Info2)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read25Info2.setTask(taskRead252);
		regularTaskRepository.save(taskRead252);

		TaskInfo do25aInfo2 = TaskInfo.builder().name("Do exercise 2.5a").time(10).build();
		RegularTask taskDo25a2 = RegularTask.builder().skill(skillTransitiveProperty).taskInfo(do25aInfo2)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do25aInfo2.setTask(taskDo25a2);
		regularTaskRepository.save(taskDo25a2);

		TaskInfo watchVideo2Info = TaskInfo.builder().name("Watch video 2: dominos").time(10).build();
		RegularTask taskWatchvideo2 = RegularTask.builder().skill(skillTransitiveProperty)
				.taskInfo(watchVideo2Info).paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		watchVideo2Info.setTask(taskWatchvideo2);
		regularTaskRepository.save(taskWatchvideo2);

		TaskInfo watch4Info = TaskInfo.builder().name("Watch lecture 4").time(10).type(TaskType.VIDEO)
				.build();
		RegularTask taskWatch4 = RegularTask.builder().skill(skillInductionPractice).taskInfo(watch4Info)
				.build();
		watch4Info.setTask(taskWatch4);
		regularTaskRepository.save(taskWatch4);

		TaskInfo do25bdInfo = TaskInfo.builder().name("Do exercise 2.5b-d").time(10).type(TaskType.EXERCISE)
				.build();
		RegularTask taskDo25bd = RegularTask.builder().skill(skillInductionPractice).taskInfo(do25bdInfo)
				.build();
		do25bdInfo.setTask(taskDo25bd);
		regularTaskRepository.save(taskDo25bd);

		TaskInfo readInfo = TaskInfo.builder().name("Read").time(10).type(TaskType.READING).build();
		RegularTask taskRead = RegularTask.builder().skill(skillInductionPractice).taskInfo(readInfo).build();
		readInfo.setTask(taskRead);
		regularTaskRepository.save(taskRead);

		TaskInfo project1Info = TaskInfo.builder().name("Project 1").time(10).type(TaskType.COLLABORATION)
				.build();
		RegularTask taskProject1 = RegularTask.builder().skill(skillInductionPractice).taskInfo(project1Info)
				.build();
		project1Info.setTask(taskProject1);
		regularTaskRepository.save(taskProject1);

		TaskInfo testYourselfInfo = TaskInfo.builder().name("Test yourself!").time(10).type(TaskType.QUIZ)
				.build();
		RegularTask taskTestyourself = RegularTask.builder().skill(skillInductionPractice)
				.taskInfo(testYourselfInfo).build();
		testYourselfInfo.setTask(taskTestyourself);
		regularTaskRepository.save(taskTestyourself);

		TaskInfo implementDFSInfo = TaskInfo.builder().name("Implement DFS").time(10)
				.type(TaskType.IMPLEMENTATION).build();
		RegularTask taskImplementdfs = RegularTask.builder().skill(skillInductionPractice)
				.taskInfo(implementDFSInfo).build();
		implementDFSInfo.setTask(taskImplementdfs);
		regularTaskRepository.save(taskImplementdfs);

		TaskInfo experimentRunInfo = TaskInfo.builder().name("Experiment with run time").time(10)
				.type(TaskType.EXPERIMENT).build();
		RegularTask taskExperimentrun = RegularTask.builder().skill(skillInductionPractice)
				.taskInfo(experimentRunInfo).build();
		experimentRunInfo.setTask(taskExperimentrun);
		regularTaskRepository.save(taskExperimentrun);

		TaskInfo task1Info = TaskInfo.builder().name("Task 1").time(10).build();
		RegularTask taskTask1 = RegularTask.builder().skill(skillSimpleA).taskInfo(task1Info).build();
		task1Info.setTask(taskTask1);
		regularTaskRepository.save(taskTask1);

		TaskInfo task2Info = TaskInfo.builder().name("Task 2").time(10).build();
		RegularTask taskTask2 = RegularTask.builder().skill(skillSimpleA).taskInfo(task2Info).build();
		task2Info.setTask(taskTask2);
		regularTaskRepository.save(taskTask2);

		TaskInfo task3Info = TaskInfo.builder().name("Task 3").time(10).build();
		RegularTask taskTask3 = RegularTask.builder().skill(skillSimpleB).taskInfo(task3Info).build();
		task3Info.setTask(taskTask3);
		regularTaskRepository.save(taskTask3);

		TaskInfo task4Info = TaskInfo.builder().name("Task 4").time(10).build();
		RegularTask taskTask4 = RegularTask.builder().skill(skillSimpleB).taskInfo(task4Info).build();
		task4Info.setTask(taskTask4);
		regularTaskRepository.save(taskTask4);

		TaskInfo task5Info = TaskInfo.builder().name("Task 5").time(10).build();
		RegularTask taskTask5 = RegularTask.builder().skill(skillSimpleC).taskInfo(task5Info).build();
		task5Info.setTask(taskTask5);
		regularTaskRepository.save(taskTask5);

		TaskInfo task6Info = TaskInfo.builder().name("Task 6").time(10).build();
		RegularTask taskTask6 = RegularTask.builder().skill(skillSimpleC).taskInfo(task6Info).build();
		task6Info.setTask(taskTask6);
		regularTaskRepository.save(taskTask6);

		TaskInfo task7Info = TaskInfo.builder().name("Task 7").time(10).build();
		RegularTask taskTask7 = RegularTask.builder().skill(skillSimpleD).taskInfo(task7Info).build();
		task7Info.setTask(taskTask7);
		regularTaskRepository.save(taskTask7);

		TaskInfo task8Info = TaskInfo.builder().name("Task 8").time(10).build();
		RegularTask taskTask8 = RegularTask.builder().skill(skillSimpleD).taskInfo(task8Info).build();
		task8Info.setTask(taskTask8);
		regularTaskRepository.save(taskTask8);

		TaskInfo task9Info = TaskInfo.builder().name("Task 9").time(10).build();
		RegularTask taskTask9 = RegularTask.builder().skill(skillSimpleE).taskInfo(task9Info).build();
		task9Info.setTask(taskTask9);
		regularTaskRepository.save(taskTask9);

		TaskInfo task10Info = TaskInfo.builder().name("Task 10").time(10).build();
		RegularTask taskTask10 = RegularTask.builder().skill(skillSimpleE).taskInfo(task10Info).build();
		task10Info.setTask(taskTask10);
		regularTaskRepository.save(taskTask10);

		TaskInfo task11Info = TaskInfo.builder().name("Task 11").time(10).build();
		RegularTask taskTask11 = RegularTask.builder().skill(skillSimpleF).taskInfo(task11Info).build();
		task11Info.setTask(taskTask11);
		regularTaskRepository.save(taskTask11);

		TaskInfo task12Info = TaskInfo.builder().name("Task 12").time(10).build();
		RegularTask taskTask12 = RegularTask.builder().skill(skillSimpleF).taskInfo(task12Info).build();
		task12Info.setTask(taskTask12);
		regularTaskRepository.save(taskTask12);
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
