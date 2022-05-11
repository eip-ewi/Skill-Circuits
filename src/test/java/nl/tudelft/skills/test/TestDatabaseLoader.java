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

import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.SCCourse;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.CourseRepository;
import nl.tudelft.skills.repository.EditionRepository;
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
	private CheckpointRepository checkpointRepository;

	/**
	 * Used to simulate sequential IDs as done by the @GeneratedValue annotation.
	 */
	private long currentId = 0L;

	private SCCourse scCourse;
	private SCEdition editionRL;
	public SCModule moduleProofTechniques;
	private SCPerson person;

	public SCEdition editionRL2021 = SCEdition.builder().id(this.getNextId()).build();

	public SCModule moduleProofTechniques = SCModule.builder().id(this.getNextId()).name("Proof Techniques")
			.build();

	public Submodule submoduleLogicBasics = Submodule.builder().id(this.getNextId()).name("Logic Basics")
			.row(0).column(0).build();
	public Submodule submoduleGeneralisation = Submodule.builder().id(this.getNextId())
			.name("Generalisation").row(1).column(0).build();
	public Submodule submoduleCases = Submodule.builder().id(this.getNextId()).name("Cases").row(0)
			.column(1).build();
	public Submodule submoduleContradiction = Submodule.builder().id(this.getNextId()).name("Contradiction")
			.row(1).column(1).build();
	public Submodule submoduleContrapositive = Submodule.builder().id(this.getNextId())
			.name("Contrapositive").row(0).column(2).build();
	public Submodule submoduleInduction = Submodule.builder().id(this.getNextId()).name("Induction").row(1)
			.column(2).build();

	public Checkpoint checkpointLectureOne = Checkpoint.builder().id(this.getNextId()).name("Lecture 1")
			.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT))).build();
	public Checkpoint checkpointLectureTwo = Checkpoint.builder().id(this.getNextId()).name("Lecture 2")
			.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 49), LocalTime.MIDNIGHT))).build();

	public Skill skillImplication = Skill.builder().id(this.getNextId()).name("Implication").row(0)
			.column(0).build();
	public Skill skillNegation = Skill.builder().id(this.getNextId()).name("Negation").row(0).column(2)
			.build();
	public Skill skillVariables = Skill.builder().id(this.getNextId()).name("Variables").row(0).column(4)
			.build();
	public Skill skillProofOutline = Skill.builder().id(this.getNextId()).name("Proof Outline").row(1)
			.column(3).build();
	public Skill skillAssumption = Skill.builder().id(this.getNextId()).name("Assumption").row(1).column(1)
			.build();
	public Skill skillGeneralisationPractice = Skill.builder().id(this.getNextId())
			.name("Generalisation Practice").row(2).column(3).build();
	public Skill skillDividingIntoCases = Skill.builder().id(this.getNextId()).name("Dividing into Cases")
			.row(2).column(1).build();
	public Skill skillCasesPractice = Skill.builder().id(this.getNextId()).name("Cases Practice").row(3)
			.column(2).build();
	public Skill skillContradictionPractice = Skill.builder().id(this.getNextId())
			.name("Contradiction Practice").row(3).column(3).build();
	public Skill skillNegateImplications = Skill.builder().id(this.getNextId()).name("Negate Implications")
			.row(4).column(1).build();
	public Skill skillContrapositivePractice = Skill.builder().id(this.getNextId())
			.name("Contrapositive Practice").row(5).column(1).build();
	public Skill skillTransitiveProperty = Skill.builder().id(this.getNextId()).name("Transitive Property")
			.row(3).column(0).build();
	public Skill skillInductionPractice = Skill.builder().id(this.getNextId()).name("Induction Practice")
			.row(6).column(2).build();

	public Task taskRead12 = Task.builder().id(this.getNextId()).name("Read chapter 1.2").build();
	public Task taskDo12ae = Task.builder().id(this.getNextId()).name("Do exercise 1.2a-e").build();
	public Task taskRead11 = Task.builder().id(this.getNextId()).name("Read chapter 1.1").build();
	public Task taskDo11ad = Task.builder().id(this.getNextId()).name("Do exercise 1.1a-d").build();
	public Task taskRead10 = Task.builder().id(this.getNextId()).name("Read chapter 1.0").build();
	public Task taskDo10a = Task.builder().id(this.getNextId()).name("Do exercise 1.0a").build();

	public Path explorerRL = Path.builder().id(this.getNextId()).name("Explorer").build();
	public Path pathfinderRL = Path.builder().id(this.getNextId()).name("Pathfinder").build();
	public Path mountainClimberRL = Path.builder().id(this.getNextId()).name("Mountain Climber").build();

	public SCPerson person = SCPerson.builder().id(TestUserDetailsService.id).build();

	public Checkpoint checkpointLectureOne;
	public Checkpoint checkpointLectureTwo;

	public SCEdition getEditionRL() {
		return editionRepository.findByIdOrThrow(editionRL2021.getId());
	}

	public SCModule getModuleProofTechniques() {
		return moduleRepository.findByIdOrThrow(moduleProofTechniques.getId());
	}

	public SCPerson getPerson() {
		return personRepository.findByIdOrThrow(person.getId());
	}

	@PostConstruct
	private void init() {
		initCourse();
		initEdition();
		initPath();
		initModule();
		initSubmodule();
		initCheckpoint();
		initSkill();
		initTask();
		initPerson();
	}

	private void initCourse() {
		courseRL = courseRepository.save(courseRL);
	}

	private void initEdition() {
		editionRL2021 = editionRepository.save(editionRL2021);
	}

	private void initPath() {
		explorerRL.setEdition(editionRL2021);
		pathfinderRL.setEdition(editionRL2021);
		mountainClimberRL.setEdition(editionRL2021);

		explorerRL = pathRepository.save(explorerRL);
		pathfinderRL = pathRepository.save(pathfinderRL);
		mountainClimberRL = pathRepository.save(mountainClimberRL);
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
		person.setPaths(Set.of(explorerRL));
		person.setTasksCompleted(Set.of(taskDo11ad, taskRead12, taskDo12ae, taskRead11));
		person = personRepository.save(person);
	}

	public long getNextId() {
		currentId++;

		return currentId;
	}

}
