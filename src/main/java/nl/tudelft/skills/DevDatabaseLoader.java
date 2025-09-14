/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.PersonRepository;

@Service
@Profile("development-data")
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
	private ChoiceTaskRepository choiceTaskRepository;
	@Autowired
	private TaskInfoRepository taskInfoRepository;
	@Autowired
	private CheckpointRepository checkpointRepository;
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

	private Submodule submoduleLogicBasics;
	private Submodule submoduleGeneralisation;
	private Submodule submoduleCases;

	private Skill skillImplication;
	private Skill skillNegation;

	private Skill skillVariables;
	private Skill skillProofOutline;

	private Skill skillDividingIntoCases;
	private Skill skillCasesPractice;

	private SCModule moduleA;
	private Submodule submoduleA;
	private Skill skillA;

	private Checkpoint checkpointLectureOne;
	private Checkpoint checkpointLectureTwo;

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
		initPerson();
	}

	private void initPerson() {
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
				.description("Pathfinder")
				.build());
		explorerPath = pathRepository.save(Path.builder()
				.edition(scEditionOOP)
				.name("Explorer")
				.description("Explorer")
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

		moduleA = moduleRepository.save(SCModule.builder()
				.name("Module A")
				.edition(scEditionADS)
				.build());
	}

	private void initSubmodules() {
		submoduleLogicBasics = submoduleRepository.save(Submodule.builder()
				.name("Logic Basics")
				.module(moduleProofTechniques)
				.column(0)
				.build());
		submoduleGeneralisation = submoduleRepository.save(Submodule.builder()
				.name("Generalisation")
				.module(moduleProofTechniques)
				.column(1)
				.build());
		submoduleCases = submoduleRepository.save(Submodule.builder()
				.name("Cases")
				.module(moduleProofTechniques)
				.column(2)
				.build());

		submoduleA = submoduleRepository.save(Submodule.builder()
				.name("SubModule A")
				.module(moduleA)
				.column(1)
				.build());

	}

	private void initSkills() {
		skillImplication = skillRepository.save(Skill.builder()
				.name("Implication")
				.submodule(submoduleLogicBasics)
				.column(0)
				.build());
		skillNegation = skillRepository.save(Skill.builder()
				.name("Negation")
				.submodule(submoduleLogicBasics)
				.column(1)
				.build());
		skillVariables = skillRepository.save(Skill.builder()
				.name("Variables")
				.submodule(submoduleLogicBasics)
				.column(2)
				.build());

		skillProofOutline = skillRepository.save(Skill.builder()
				.name("Proof Outline")
				.submodule(submoduleGeneralisation)
				.checkpoint(checkpointLectureOne)
				.parents(Set.of(skillNegation, skillVariables, skillImplication))
				.column(1)
				.build());

		skillDividingIntoCases = skillRepository.save(Skill.builder()
				.name("Dividing into Cases")
				.submodule(submoduleCases)
				.column(1)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillProofOutline))
				.build());
		skillCasesPractice = skillRepository.save(Skill.builder()
				.name("Cases Practice")
				.submodule(submoduleCases)
				.column(2)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillDividingIntoCases))
				.build());

		skillA = skillRepository.save(Skill.builder()
				.name("Skill A")
				.submodule(submoduleA)
				.column(1)
				.build());
	}

	private void initTasks() {
		TaskInfo read12Info = TaskInfo.builder().name("Read chapter 1.2")
				.time(7).type(TaskType.READING).build();
		RegularTask taskRead12 = RegularTask.builder().skill(skillImplication).taskInfo(read12Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath, explorerPath))).build();
		read12Info.setTask(taskRead12);
		taskInfoRepository.save(read12Info);

		TaskInfo do12aeInfo = TaskInfo.builder().name("Do exercise 1.2a-e")
				.time(10).type(TaskType.EXERCISE).build();
		RegularTask taskDo12ae = RegularTask.builder().skill(skillImplication).taskInfo(do12aeInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		do12aeInfo.setTask(taskDo12ae);
		taskInfoRepository.save(do12aeInfo);

		TaskInfo do11adInfo = TaskInfo.builder().name("Do exercise 1.1a-d").time(10).build();
		RegularTask taskDo11ad = RegularTask.builder().skill(skillNegation).taskInfo(do11adInfo)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath, explorerPath))).build();
		do11adInfo.setTask(taskDo11ad);
		taskInfoRepository.save(do11adInfo);

		TaskInfo read11Info = TaskInfo.builder().name("Read chapter 1.1").time(10)
				.link("https://docs.oracle.com/en/java/javase/17/docs/api/index.html").type(TaskType.READING)
				.build();

		TaskInfo watchVideoInfo = TaskInfo.builder().name("Watch video").time(10)
				.link("https://docs.oracle.com/en/java/javase/17/docs/api/index.html").type(TaskType.VIDEO)
				.build();

		ChoiceTask choiceTaskNegation = ChoiceTask.builder().minTasks(1)
				.tasks(new ArrayList<>(List.of(read11Info, watchVideoInfo))).name("What are negations?")
				.skill(skillNegation)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath)))
				.build();
		read11Info.setChoiceTask(choiceTaskNegation);
		watchVideoInfo.setChoiceTask(choiceTaskNegation);
		choiceTaskRepository.save(choiceTaskNegation);

		TaskInfo read10Info = TaskInfo.builder().name("Read chapter 1.0").time(10).type(TaskType.READING)
				.build();
		RegularTask taskRead10 = RegularTask.builder().skill(skillVariables).taskInfo(read10Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read10Info.setTask(taskRead10);
		taskInfoRepository.save(read10Info);

		TaskInfo read20Info = TaskInfo.builder().name("Read chapter 2.0").time(10).build();
		RegularTask taskRead20 = RegularTask.builder().skill(skillProofOutline).taskInfo(read20Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		read20Info.setTask(taskRead20);
		taskInfoRepository.save(read20Info);

		TaskInfo watch1Info = TaskInfo.builder().name("Watch lecture 1").time(10).type(TaskType.VIDEO)
				.build();
		RegularTask taskWatch1 = RegularTask.builder().skill(skillProofOutline).taskInfo(watch1Info)
				.paths(new HashSet<>(Arrays.asList(pathFinderPath))).build();
		watch1Info.setTask(taskWatch1);
		taskInfoRepository.save(watch1Info);

		TaskInfo taskA = TaskInfo.builder().name("A").time(10).type(TaskType.CLASSROOM).build();
		RegularTask taskCA = RegularTask.builder().skill(skillA).taskInfo(taskA).build();
		taskA.setTask(taskCA);
		taskInfoRepository.save(taskA);
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
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2078, 49), LocalTime.MIDNIGHT))
				.build());
	}

}
