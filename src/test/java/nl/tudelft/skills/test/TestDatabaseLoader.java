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
import java.util.Set;

import javax.annotation.PostConstruct;

import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class TestDatabaseLoader {

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
	private CheckpointRepository checkpointRepository;

	public CourseSummaryDTO course;
	public EditionDetailsDTO edition;

	private SCCourse scCourse;
	private SCEdition editionRL;
	public SCModule moduleProofTechniques;
	private SCPerson person;

	public Submodule submoduleLogicBasics;
	public Submodule submoduleGeneralisation;
	public Submodule submoduleCases;
	public Submodule submoduleContradiction;
	public Submodule submoduleContrapositive;
	public Submodule submoduleInduction;

	public Skill skillImplication;
	public Skill skillNegation;

	public Skill skillVariables;
	public Skill skillProofOutline;
	public Skill skillGeneralisationPractice;
	public Skill skillAssumption;

	public Skill skillDividingIntoCases;
	public Skill skillCasesPractice;

	public Skill skillContradictionPractice;

	public Skill skillNegateImplications;
	public Skill skillContrapositivePractice;

	public Skill skillTransitiveProperty;
	public Skill skillInductionPractice;

	public Task taskRead12;
	public Task taskDo12ae;
	public Task taskRead11;
	public Task taskDo11ad;
	public Task taskRead10;
	public Task taskDo10a;

	public Checkpoint checkpointLectureOne;
	public Checkpoint checkpointLectureTwo;

	public SCModule getModuleProofTechniques() {
		return moduleRepository.findByIdOrThrow(moduleProofTechniques.getId());
	}

	public SCEdition getEditionRL() {
		return editionRepository.findByIdOrThrow(editionRL.getId());
	}

	public SCCourse getCourseRL() {
		return courseRepository.findByIdOrThrow(course.getId());
	}

	public SCPerson getPerson() {
		return personRepository.findByIdOrThrow(person.getId());
	}

	@PostConstruct
	private void init() {
		course = new CourseSummaryDTO().id(30L).name("RL");
		edition = new EditionDetailsDTO().id(69L).name("Reasoning and Logic");

		initCourse();
		initEdition();
		initModules();
		initSubmodules();
		initCheckpoints();
		initSkills();
		initTasks();
		initPerson();
	}

	private void initCourse() {
		scCourse = courseRepository.save(SCCourse.builder()
				.id(30L)
				.build());
	}

	private void initEdition() {
		editionRL = editionRepository.save(SCEdition.builder()
				.id(edition.getId())
				.build());
	}

	private void initModules() {
		moduleProofTechniques = moduleRepository.save(SCModule.builder()
				.name("Proof Techniques")
				.edition(editionRL)
				.build());
	}

	private void initPerson() {
		person = personRepository.save(SCPerson.builder()
				.id(TestUserDetailsService.id)
				.tasksCompleted(Set.of(taskRead11, taskDo11ad, taskRead12, taskDo12ae))
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
				.column(0)
				.build());
		submoduleCases = submoduleRepository.save(Submodule.builder()
				.name("Cases")
				.module(moduleProofTechniques)
				.row(0)
				.column(1)
				.build());
		submoduleContradiction = submoduleRepository.save(Submodule.builder()
				.name("Contradiction")
				.module(moduleProofTechniques)
				.row(1)
				.column(1)
				.build());
		submoduleContrapositive = submoduleRepository.save(Submodule.builder()
				.name("Contrapositive")
				.module(moduleProofTechniques)
				.row(0)
				.column(2)
				.build());
		submoduleInduction = submoduleRepository.save(Submodule.builder()
				.name("Induction")
				.module(moduleProofTechniques)
				.row(1)
				.column(2)
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
				.parents(Set.of(skillProofOutline, skillDividingIntoCases))
				.build());

		skillContradictionPractice = skillRepository.save(Skill.builder()
				.name("Contradiction Practice")
				.submodule(submoduleContradiction)
				.row(3).column(3)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillProofOutline, skillNegation))
				.build());

		skillNegateImplications = skillRepository.save(Skill.builder()
				.name("Negate Implications")
				.submodule(submoduleContrapositive)
				.row(4).column(1)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillNegation, skillImplication))
				.build());
		skillContrapositivePractice = skillRepository.save(Skill.builder()
				.name("Contrapositive Practice")
				.submodule(submoduleContrapositive)
				.row(5).column(1)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillProofOutline, skillNegateImplications))
				.build());

		skillTransitiveProperty = skillRepository.save(Skill.builder()
				.name("Transitive Property")
				.submodule(submoduleInduction)
				.row(3).column(0)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillImplication))
				.build());
		skillInductionPractice = skillRepository.save(Skill.builder()
				.name("Induction Practice")
				.submodule(submoduleInduction)
				.row(6).column(2)
				.checkpoint(checkpointLectureTwo)
				.parents(Set.of(skillTransitiveProperty, skillProofOutline, skillDividingIntoCases))
				.build());
	}

	private void initTasks() {
		taskRead12 = taskRepository
				.save(Task.builder().name("Read chapter 1.2").skill(skillImplication).build());
		taskDo12ae = taskRepository
				.save(Task.builder().name("Do exercise 1.2a-e").skill(skillImplication).build());
		taskRead11 = taskRepository
				.save(Task.builder().name("Read chapter 1.1").skill(skillNegation).build());
		taskDo11ad = taskRepository
				.save(Task.builder().name("Do exercise 1.1a-d").skill(skillNegation).build());
		taskRead10 = taskRepository
				.save(Task.builder().name("Read chapter 1.0").skill(skillVariables).build());
		taskDo10a = taskRepository
				.save(Task.builder().name("Do exercise 1.0a").skill(skillVariables).build());

		taskRepository.save(Task.builder().name("Read chapter 2.0").skill(skillProofOutline).build());
		taskRepository.save(Task.builder().name("Do exercise 2.0a-f").skill(skillProofOutline).build());
		taskRepository.save(Task.builder().name("Watch lecture 1").skill(skillProofOutline).build());
		taskRepository.save(Task.builder().name("Read chapter 2.1").skill(skillAssumption).build());
		taskRepository.save(Task.builder().name("Do exercise 2.1a-g").skill(skillAssumption).build());
		taskRepository
				.save(Task.builder().name("Read chapter 2.2").skill(skillGeneralisationPractice).build());
		taskRepository
				.save(Task.builder().name("Do exercise 2.2a-b").skill(skillGeneralisationPractice).build());
		taskRepository.save(Task.builder().name("TA Check 1").skill(skillGeneralisationPractice).build());

		taskRepository.save(Task.builder().name("Read chapter 2.3").skill(skillDividingIntoCases).build());
		taskRepository.save(Task.builder().name("Watch lecture 2").skill(skillDividingIntoCases).build());
		taskRepository.save(Task.builder().name("Do exercise 2.3a-d").skill(skillCasesPractice).build());

		taskRepository
				.save(Task.builder().name("Read chapter 2.4").skill(skillContradictionPractice).build());
		taskRepository.save(Task.builder().name("Watch lecture 3").skill(skillContradictionPractice).build());
		taskRepository.save(Task.builder().name("TA Check 2").skill(skillContradictionPractice).build());

		taskRepository.save(Task.builder().name("Read chapter 2.5").skill(skillNegateImplications).build());
		taskRepository.save(Task.builder().name("Watch video 1").skill(skillContrapositivePractice).build());
		taskRepository
				.save(Task.builder().name("Do exercise 2.5a").skill(skillContrapositivePractice).build());

		taskRepository.save(Task.builder().name("Read chapter 2.5").skill(skillTransitiveProperty).build());
		taskRepository.save(Task.builder().name("Do exercise 2.5a").skill(skillTransitiveProperty).build());
		taskRepository
				.save(Task.builder().name("Watch video 2: dominos").skill(skillTransitiveProperty).build());

		taskRepository.save(Task.builder().name("Watch lecture 4").skill(skillInductionPractice).build());
		taskRepository.save(Task.builder().name("Do exercise 2.5b-d").skill(skillInductionPractice).build());
	}

	private void initCheckpoints() {
		checkpointLectureOne = checkpointRepository.save(Checkpoint.builder()
				.name("Lecture 1")
				.edition(edition.getId())
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT))
				.build());

		checkpointLectureTwo = checkpointRepository.save(Checkpoint.builder()
				.name("Lecture 2")
				.edition(edition.getId())
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 49), LocalTime.MIDNIGHT))
				.build());
	}

}
