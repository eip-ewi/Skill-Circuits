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

import java.util.Set;

import javax.annotation.PostConstruct;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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
	private TaskRepository taskRepository;

	@Autowired
	private CourseControllerApi courseControllerApi;
	@Autowired
	private EditionControllerApi editionApi;

	private EditionDetailsDTO edition;

	private CourseSummaryDTO course;
	private SCEdition scEdition;

	private SCCourse scCourse;

	private SCModule moduleProofTechniques;
	private SCModule modulePropositionalLogic;

	private Submodule submoduleLogicBasics;
	private Submodule submoduleGeneralisation;
	private Submodule submoduleCases;
	private Submodule submoduleContradiction;
	private Submodule submoduleContrapositive;
	private Submodule submoduleInduction;

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

	@PostConstruct
	private void init() {
		course = courseControllerApi.getAllCourses().blockFirst();
		edition = editionApi.getAllEditions().blockFirst();

		initCourse();
		initEdition();
		initModules();
		initSubmodules();
		initSkills();
		initTasks();
	}

	private void initCourse() {
		scCourse = courseRepository.save(SCCourse.builder()
				.id(course.getId())
				.build());
	}

	private void initEdition() {
		scEdition = editionRepository.save(SCEdition.builder()
				.id(edition.getId())
				.build());
	}

	private void initModules() {
		moduleProofTechniques = moduleRepository.save(SCModule.builder()
				.name("Proof Techniques")
				.edition(edition.getId())
				.build());
		modulePropositionalLogic = moduleRepository.save(SCModule.builder()
				.name("Propositional Logic")
				.edition(edition.getId())
				.build());
	}

	private void initSubmodules() {
		submoduleLogicBasics = submoduleRepository.save(Submodule.builder()
				.name("Logic Basics")
				.module(moduleProofTechniques)
				.build());
		submoduleGeneralisation = submoduleRepository.save(Submodule.builder()
				.name("Generalisation")
				.module(moduleProofTechniques)
				.build());
		submoduleCases = submoduleRepository.save(Submodule.builder()
				.name("Cases")
				.module(moduleProofTechniques)
				.build());
		submoduleContradiction = submoduleRepository.save(Submodule.builder()
				.name("Contradiction")
				.module(moduleProofTechniques)
				.build());
		submoduleContrapositive = submoduleRepository.save(Submodule.builder()
				.name("Contrapositive")
				.module(moduleProofTechniques)
				.build());
		submoduleInduction = submoduleRepository.save(Submodule.builder()
				.name("Induction")
				.module(moduleProofTechniques)
				.build());
	}

	private void initSkills() {
		skillImplication = skillRepository.save(Skill.builder()
				.name("Implication")
				.submodule(submoduleLogicBasics)
				.row(0).column(0)
				.build());
		skillNegation = skillRepository.save(Skill.builder()
				.name("Negation")
				.submodule(submoduleLogicBasics)
				.row(0).column(2)
				.build());
		skillVariables = skillRepository.save(Skill.builder()
				.name("Variables")
				.submodule(submoduleLogicBasics)
				.row(0).column(4)
				.build());

		skillProofOutline = skillRepository.save(Skill.builder()
				.name("Proof Outline")
				.submodule(submoduleGeneralisation)
				.row(1).column(3)
				.build());
		skillAssumption = skillRepository.save(Skill.builder()
				.name("Assumption")
				.submodule(submoduleGeneralisation)
				.row(1).column(1)
				.parents(Set.of(skillImplication))
				.build());
		skillGeneralisationPractice = skillRepository.save(Skill.builder()
				.name("Generalisation Practice")
				.submodule(submoduleGeneralisation)
				.row(2).column(3)
				.parents(Set.of(skillAssumption, skillProofOutline, skillVariables))
				.build());

		skillDividingIntoCases = skillRepository.save(Skill.builder()
				.name("Dividing into Cases")
				.submodule(submoduleCases)
				.row(2).column(1)
				.build());
		skillCasesPractice = skillRepository.save(Skill.builder()
				.name("Cases Practice")
				.submodule(submoduleCases)
				.row(3).column(2)
				.parents(Set.of(skillProofOutline, skillDividingIntoCases))
				.build());

		skillContradictionPractice = skillRepository.save(Skill.builder()
				.name("Contradiction Practice")
				.submodule(submoduleContradiction)
				.row(3).column(3)
				.parents(Set.of(skillProofOutline, skillNegation))
				.build());

		skillNegateImplications = skillRepository.save(Skill.builder()
				.name("Negate Implications")
				.submodule(submoduleContrapositive)
				.row(4).column(1)
				.parents(Set.of(skillNegation, skillImplication))
				.build());
		skillContrapositivePractice = skillRepository.save(Skill.builder()
				.name("Contrapositive Practice")
				.submodule(submoduleContrapositive)
				.row(5).column(1)
				.parents(Set.of(skillProofOutline, skillNegateImplications))
				.build());

		skillTransitiveProperty = skillRepository.save(Skill.builder()
				.name("Transitive Property")
				.submodule(submoduleInduction)
				.row(3).column(0)
				.parents(Set.of(skillImplication))
				.build());
		skillInductionPractice = skillRepository.save(Skill.builder()
				.name("Induction Practice")
				.submodule(submoduleInduction)
				.row(6).column(2)
				.parents(Set.of(skillTransitiveProperty, skillProofOutline, skillDividingIntoCases))
				.build());
	}

	private void initTasks() {
		taskRepository.save(Task.builder().name("Read chapter 1.2").skill(skillImplication).build());
		taskRepository.save(Task.builder().name("Do exercise 1.2a-e").skill(skillImplication).build());
		taskRepository.save(Task.builder().name("Read chapter 1.1").skill(skillNegation).build());
		taskRepository.save(Task.builder().name("Do exercise 1.1a-d").skill(skillNegation).build());
		taskRepository.save(Task.builder().name("Read chapter 1.0").skill(skillVariables).build());
		taskRepository.save(Task.builder().name("Do exercise 1.0a").skill(skillVariables).build());

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

}
