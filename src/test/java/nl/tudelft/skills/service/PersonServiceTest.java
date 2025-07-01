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
package nl.tudelft.skills.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.module.ChoiceTaskViewDTO;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.RegularTaskViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PersonServiceTest {

	private final PersonRepository personRepository;
	private final EditionRepository editionRepository;

	private final PersonService personService;
	private final SkillRepository skillRepository;
	private final RegularTaskRepository regularTaskRepository;
	private final TaskRepository taskRepository;
	private final PathPreferenceRepository pathPreferenceRepository;

	private final TestDatabaseLoader db;
	protected Model model;

	@Autowired
	public PersonServiceTest(PersonRepository personRepository, PathRepository pathRepository,
			EditionRepository editionRepository, SkillRepository skillRepository,
			RegularTaskRepository regularTaskRepository, TaskRepository taskRepository,
			PathPreferenceRepository pathPreferenceRepository, EditionService editionService,
			TestDatabaseLoader db) {
		this.personRepository = personRepository;
		this.skillRepository = skillRepository;
		this.regularTaskRepository = regularTaskRepository;
		this.taskRepository = taskRepository;
		this.editionRepository = editionRepository;
		this.pathPreferenceRepository = pathPreferenceRepository;
		this.db = db;

		personService = new PersonService(personRepository, pathRepository, editionService);
	}

	@BeforeEach
	void initFields() {
		model = new ExtendedModelMap();
	}

	@Test
	public void getOrCreateSCPerson() {
		Long personId = 1L;
		assertThat(personRepository.findById(personId)).isNotPresent();

		SCPerson person = personService.getOrCreateSCPerson(personId);
		Optional<SCPerson> retrieval = personRepository.findById(personId);
		assertThat(retrieval).isPresent();
		assertThat(retrieval.get()).isEqualTo(person);
	}

	@Test
	public void getPathForEdition() {
		Long editionId = 1L;
		SCPerson person = SCPerson.builder().pathPreferences(Collections.emptySet()).id(2L).build();
		person = personRepository.save(person);

		assertThat(personService.getPathForEdition(person.getId(), editionId)).isNotPresent();

	}

	@Test
	void getDefaultOrPreferredPathNull() {
		Path path = personService.getDefaultOrPreferredPath(db.getPerson().getId(),
				db.getEditionRL().getId());

		// displayed path is null if there is no default and no path preference set
		assertNull(path);
	}

	@Test
	void getDefaultOrPreferredPathDefault() {
		// Set default path
		SCEdition edition = db.getEditionRL();
		edition.setDefaultPath(db.getPathFinderPath());
		editionRepository.save(edition);

		Path path = personService.getDefaultOrPreferredPath(db.getPerson().getId(),
				db.getEditionRL().getId());

		// displayed path is default path of the course if there is no path preference set
		assertEquals(db.getPathFinderPath(), path);
	}

	@Test
	void getDefaultOrPreferredPathPreferred() {
		// Set preference
		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
				.edition(db.getEditionRL()).person(db.getPerson()).build();
		SCPerson person = db.getPerson();
		person.getPathPreferences().add(pathPreference);
		pathPreferenceRepository.save(pathPreference);
		personRepository.save(person);

		Path path = personService.getDefaultOrPreferredPath(db.getPerson().getId(),
				db.getEditionRL().getId());

		// displayed path is set by path preference
		assertEquals(db.getPathFinderPath(), path);
	}

	@Transactional
	void addTaskAndSkillModifications() {
		SCPerson person = db.getPerson();
		Skill skillA = db.getSkillVariables();
		Skill skillB = db.getSkillImplication();
		RegularTask task = db.getTaskDo10a();

		person.getSkillsModified().add(skillA);
		skillA.getPersonModifiedSkill().add(person);
		person.getSkillsModified().add(skillB);
		skillB.getPersonModifiedSkill().add(person);

		// skillImplication no tasks in it with the modification
		// taskDo10a is in skillVariables
		person.getTasksAdded().add(task);
		task.getPersonsThatAddedTask().add(person);
	}

	@Transactional
	void addChoiceTaskToTasks() {
		SCPerson person = db.getPerson();
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		RegularTask taskA = db.getTaskBook();
		RegularTask taskB = db.getTaskVideo();
		Skill skill = db.getSkillVariables();

		person.getTasksAdded().addAll(Set.of(choiceTask, taskA, taskB));
		choiceTask.getPersonsThatAddedTask().add(person);
		taskA.getPersonsThatAddedTask().add(person);
		taskB.getPersonsThatAddedTask().add(person);
		person.getSkillsModified().add(skill);
		skill.getPersonModifiedSkill().add(person);
	}

	@Test
	void setPersonalPathAttributesPathAndSkillNullWithChoiceTask() {
		// Path: null, skill: null, tasksAdded: contains tasks and choice task, skillsModified: contains some skills

		// Add choice task
		addChoiceTaskToTasks();

		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), null);

		assertThat(taskIds).isEmpty();
		assertThat(model.getAttribute("selectedPathId")).isNull();
		assertThat(model.getAttribute("tasksAdded"))
				.isEqualTo(Set.of(
						View.convert(db.getChoiceTaskBookOrVideo(), ChoiceTaskViewDTO.class),
						View.convert(db.getTaskBook(), RegularTaskViewDTO.class),
						View.convert(db.getTaskVideo(), RegularTaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(
				Set.of(View.convert(db.getSkillVariables(), ModuleLevelSkillViewDTO.class)));
	}

	@Test
	void setPersonalPathAttributesPathAndSkillSetWithChoiceTask() {
		// Path: null, skill: non-null, tasksAdded: contains tasks and choice task, skillsModified: contains some skills

		// Add choice task
		addChoiceTaskToTasks();

		// Add task outside of skill variables
		RegularTask task = db.getTaskRead12();
		SCPerson person = db.getPerson();
		person.getTasksAdded().add(task);
		personRepository.save(person);
		taskRepository.save(task);

		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), db.getSkillVariables());

		assertThat(taskIds).isEmpty();
		assertThat(model.getAttribute("selectedPathId")).isNull();
		// tasksAdded should not contain the task outside the skill
		assertThat(model.getAttribute("tasksAdded"))
				.isEqualTo(Set.of(
						View.convert(db.getChoiceTaskBookOrVideo(), ChoiceTaskViewDTO.class),
						View.convert(db.getTaskBook(), RegularTaskViewDTO.class),
						View.convert(db.getTaskVideo(), RegularTaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(
				Set.of(View.convert(db.getSkillVariables(), ModuleLevelSkillViewDTO.class)));
	}

	@Test
	void setPersonalPathAttributesPathAndSkillNull() {
		// Path: null, skill: null, tasksAdded: contains a task, skillsModified: contains some skills

		addTaskAndSkillModifications();
		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), null);

		assertThat(taskIds).isEmpty();
		assertThat(model.getAttribute("selectedPathId")).isNull();
		assertThat(model.getAttribute("tasksAdded"))
				.isEqualTo(Set.of(View.convert(db.getTaskDo10a(), RegularTaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(
				Set.of(View.convert(db.getSkillVariables(), ModuleLevelSkillViewDTO.class),
						View.convert(db.getSkillImplication(), ModuleLevelSkillViewDTO.class)));
	}

	@Test
	void setPersonalPathAttributesPathSetAndSkillNull() {
		// Path: non-null, skill: null, tasksAdded: contains a task, skillsModified: contains some skills

		// Set default path and task/skill modifications
		SCEdition edition = db.getEditionRL();
		edition.setDefaultPath(db.getPathFinderPath());
		editionRepository.save(edition);
		addTaskAndSkillModifications();

		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), null);

		assertThat(taskIds).isNotEmpty();
		assertThat(taskIds.get()).containsExactly(db.getTaskRead12().getId());
		assertThat(model.getAttribute("selectedPathId")).isEqualTo(db.getPathFinderPath().getId());
		assertThat(model.getAttribute("tasksAdded"))
				.isEqualTo(Set.of(View.convert(db.getTaskDo10a(), RegularTaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(
				Set.of(View.convert(db.getSkillVariables(), ModuleLevelSkillViewDTO.class),
						View.convert(db.getSkillImplication(), ModuleLevelSkillViewDTO.class)));
	}

	@Test
	void setPersonalPathAttributesPathSetSkillSetAndModified() {
		// Path: non-null, skill: non-null, tasksAdded: contains a task from skill, skillsModified: contains skill

		// Set default path
		SCEdition edition = db.getEditionRL();
		edition.setDefaultPath(db.getPathFinderPath());
		editionRepository.save(edition);

		// Set task/skill modifications
		addTaskAndSkillModifications();
		SCPerson person = db.getPerson();
		RegularTask task = db.getTaskRead12();
		person.getTasksAdded().add(task);
		task.getPersonsThatAddedTask().add(person);
		personRepository.save(person);
		regularTaskRepository.save(task);

		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), db.getSkillVariables());

		assertThat(taskIds).isNotEmpty();
		assertThat(taskIds.get()).containsExactly(db.getTaskRead12().getId());
		assertThat(model.getAttribute("selectedPathId")).isEqualTo(db.getPathFinderPath().getId());
		assertThat(model.getAttribute("tasksAdded"))
				.isEqualTo(Set.of(View.convert(db.getTaskDo10a(), RegularTaskViewDTO.class)));
		assertThat(model.getAttribute("skillsModified")).isEqualTo(
				Set.of(View.convert(db.getSkillVariables(), ModuleLevelSkillViewDTO.class)));
	}

	@Test
	void setPersonalPathAttributesPathSetSkillSetAndUnmodified() {
		// Path: non-null, skill: non-null, tasksAdded: contains tasks only not from skill, skillsModified: contains
		// only other skills

		// Set default path
		SCEdition edition = db.getEditionRL();
		edition.setDefaultPath(db.getPathFinderPath());
		editionRepository.save(edition);

		// Set task/skill modifications
		SCPerson person = db.getPerson();
		Skill skill = db.getSkillImplication();
		RegularTask task = db.getTaskRead12();
		person.getTasksAdded().add(task);
		task.getPersonsThatAddedTask().add(person);
		person.getSkillsModified().add(skill);
		skill.getPersonModifiedSkill().add(person);
		personRepository.save(person);
		regularTaskRepository.save(task);
		skillRepository.save(skill);

		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), db.getSkillVariables());

		assertThat(taskIds).isNotEmpty();
		assertThat(taskIds.get()).containsExactly(db.getTaskRead12().getId());
		assertThat(model.getAttribute("selectedPathId")).isEqualTo(db.getPathFinderPath().getId());
		assertThat(model.getAttribute("tasksAdded")).isEqualTo(Set.of());
		assertThat(model.getAttribute("skillsModified")).isEqualTo(Set.of());
	}

	@Test
	void setPersonalPathAttributesPathSetAndSkillNullNoModifications() {
		// Path: non-null, skill: null, tasksAdded: empty, skillsModified: empty

		// Set default path and task/skill modifications
		SCEdition edition = db.getEditionRL();
		edition.setDefaultPath(db.getPathFinderPath());
		editionRepository.save(edition);

		Optional<Set<Long>> taskIds = personService.setPersonalPathAttributes(db.getPerson().getId(), model,
				db.getEditionRL().getId(), null);

		assertThat(taskIds).isNotEmpty();
		assertThat(taskIds.get()).containsExactly(db.getTaskRead12().getId());
		assertThat(model.getAttribute("selectedPathId")).isEqualTo(db.getPathFinderPath().getId());
		assertThat(model.getAttribute("tasksAdded")).isEqualTo(Set.of());
		assertThat(model.getAttribute("skillsModified")).isEqualTo(Set.of());
	}

	@Test
	public void testAddRevealedSkillHiddenFalse() {
		SCPerson person = db.getPerson();
		Skill skill = db.getSkillNegation();

		assertThat(person.getSkillsRevealed()).doesNotContain(skill);
		personService.addRevealedSkill(person.getId(), skill);

		assertThat(person.getSkillsRevealed()).doesNotContain(skill);

	}

	@Test
	public void testAddRevealedSkillHiddenTrue() {
		SCPerson person = db.getPerson();
		Skill skill = db.getSkillVariablesHidden();

		assertThat(person.getSkillsRevealed()).doesNotContain(skill);
		personService.addRevealedSkill(person.getId(), skill);

		assertThat(person.getSkillsRevealed()).contains(skill);
		personService.addRevealedSkill(person.getId(), skill);
		assertThat(person.getSkillsRevealed().size()).isEqualTo(1);

	}
}
