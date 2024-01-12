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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
public class TaskCompletionService {

	private final EditionControllerApi editionApi;
	private final PersonRepository personRepository;
	private final SkillRepository skillRepository;
	private final TaskCompletionRepository taskCompletionRepository;

	@Autowired
	public TaskCompletionService(TaskCompletionRepository taskCompletionRepository,
			PersonRepository personRepository, EditionControllerApi editionApi,
			SkillRepository skillRepository) {
		this.taskCompletionRepository = taskCompletionRepository;
		this.personRepository = personRepository;
		this.editionApi = editionApi;
		this.skillRepository = skillRepository;
	}

	/**
	 * Gets the latest task completion of a given person, if it exists. Otherwise, or if the person is null,
	 * returns null.
	 *
	 * @param  person The person to get the latest task completion for
	 * @return        The latest task completion for the given person
	 */
	public Task latestTaskCompletion(Person person) {
		if (person == null) {
			return null;
		}
		SCPerson scperson = personRepository.findByIdOrThrow(person.getId());

		// Also need to filter out null values due to migrated data
		Optional<TaskCompletion> lastCompleted = taskCompletionRepository
				.getFirstByPersonIdAndTimestampNotNullOrderByTimestampDesc(scperson.getId());

		return lastCompleted.map(TaskCompletion::getTask).orElse(null);
	}

	/**
	 * Generates a string indicating the location of a given task in the form of: course name - submodule name
	 * - module name - skill name This is used to display information about the completed task in the
	 * frontend.
	 *
	 * @param  task The task to generate the string for
	 * @return      A string indicating the location of the given task
	 */
	public String getLocationString(Task task) {
		Skill skill = task.getSkill();
		Submodule submodule = skill.getSubmodule();
		EditionDetailsDTO edition = editionApi.getEditionById(submodule.getModule().getEdition().getId())
				.block();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(edition.getCourse().getName()).append(" - ")
				.append(submodule.getModule().getName()).append(" - ")
				.append(submodule.getName()).append(" - ")
				.append(skill.getName());

		return stringBuilder.toString();
	}

	/**
	 * Saves a TaskCompletion to the repository, given the corresponding SCPerson and Task.
	 *
	 * @param  person The SCPerson that completed the Task
	 * @param  task   The Task that was completed
	 * @return        The saved TaskCompletion
	 */
	@Transactional
	public TaskCompletion addTaskCompletion(SCPerson person, Task task) {
		TaskCompletion completion = taskCompletionRepository.save(TaskCompletion.builder()
				.task(task).person(person).build());
		person.getTaskCompletions().add(completion);
		task.getCompletedBy().add(completion);
		return completion;
	}

	/**
	 * Deletes a TaskCompletion from the repository, given the corresponding SCPerson and Task. If such a
	 * TaskCompletion does not exist, returns null.
	 *
	 * @param  person The SCPerson that had completed the Task
	 * @param  task   The Task that was completed
	 * @return        The deleted TaskCompletion, or null if such a TaskCompletion does not exist.
	 */
	@Transactional
	public TaskCompletion deleteTaskCompletion(SCPerson person, Task task) {
		Optional<TaskCompletion> completion = person.getTaskCompletions().stream()
				.filter(c -> c.getPerson().equals(person) && c.getTask().equals(task)).findFirst();

		completion.ifPresent((TaskCompletion taskCompletion) -> {
			taskCompletionRepository.delete(taskCompletion);
			person.getTaskCompletions().remove(taskCompletion);
			task.getCompletedBy().remove(taskCompletion);
		});

		return completion.orElse(null);
	}

	/**
	 * Deletes the TaskCompletions of a given Task, and removes them from the Tasks completion set, as well as
	 * completion sets of the Persons who completed the Task.
	 *
	 * @param task The Task for which the completions should be deleted
	 */
	@Transactional
	public void deleteTaskCompletionsOfTask(Task task) {
		Set<TaskCompletion> taskCompletions = task.getCompletedBy();
		// Remove from taskCompletionRepository
		taskCompletionRepository.deleteAll(taskCompletions);
		// Remove from Person sets
		taskCompletions.forEach(tc -> tc.getPerson().getTaskCompletions().remove(tc));
		// In case the Task will be used later on, also clear its TaskCompletion set
		task.setCompletedBy(new HashSet<>());
	}

	/**
	 * Collects the completed tasks in an edition
	 *
	 * @param  scperson  The person completing the tasks
	 * @param  editionId The edition
	 * @return           The set of completed tasks
	 */
	private Set<Task> getTasksDone(SCPerson scperson, long editionId) {
		return scperson.getTaskCompletions().stream().map(TaskCompletion::getTask)
				.filter(s -> Objects.equals(
						s.getSkill().getSubmodule().getModule().getEdition().getId(), editionId))
				.collect(Collectors.toSet());
	}

	/**
	 * Collects the completed skills based on the given path preference and customized skills
	 *
	 * @param  ownSkillsWithTask    All the customized non-empty skills in current edition
	 * @param  scPerson             The person completing skills
	 * @param  editionId            The edition
	 * @param  personPathPreference The chosen path for the current edition
	 * @return                      The set of completed skills
	 */
	public Set<Skill> determineSkillsDone(Set<Skill> ownSkillsWithTask, SCPerson scPerson, long editionId,
			List<PathPreference> personPathPreference) {
		Set<Task> tasksDone = getTasksDone(scPerson, editionId);

		// Completed skills from the customized skills
		Set<Skill> ownSkillsDone = ownSkillsWithTask.stream()
				.filter(s -> tasksDone.containsAll(scPerson.getTasksAdded().stream()
						.filter(t -> t.getSkill().equals(s)).toList()))
				.collect(Collectors.toSet());

		// Completed non-customized skills based on chosen path
		Set<Skill> skillsDone;
		if (personPathPreference.isEmpty() || personPathPreference.get(0).getPath() == null) {
			skillsDone = tasksDone.stream()
					.map(Task::getSkill)
					.filter(s -> !ownSkillsWithTask.contains(s) && tasksDone.containsAll(s.getTasks()))
					.collect(Collectors.toSet());
		} else {
			Path personPath = personPathPreference.get(0).getPath();
			skillsDone = tasksDone.stream()
					.filter(t -> t.getPaths().contains(personPath))
					.map(Task::getSkill)
					.filter(x -> !ownSkillsWithTask.contains(x) && tasksDone.containsAll(x.getTasks()
							.stream().filter(y -> y.getPaths().contains(personPath)).toList()))
					.collect(Collectors.toSet());
		}
		skillsDone.addAll(ownSkillsDone);

		return skillsDone;
	}

	/**
	 * Collects the empty skills based on path preferences and customized skills
	 *
	 * @param  ownSkillsWithTask    All the customized non-empty skills in current edition
	 * @param  personPathPreference The chosen path for the current edition
	 * @param  scPerson             The person completing skills
	 * @param  editionId            The edition
	 * @return                      The set of empty skills
	 */
	public Set<Skill> determineEmptySkills(Set<Skill> ownSkillsWithTask,
			List<PathPreference> personPathPreference,
			SCPerson scPerson, long editionId) {
		Set<Skill> ownSkills = scPerson.getSkillsModified().stream().filter(s -> Objects.equals(
				s.getSubmodule().getModule().getEdition().getId(), editionId))
				.collect(Collectors.toSet());

		// Customized empty skills
		Set<Skill> ownEmptySkills = ownSkills.stream()
				.filter(s -> !ownSkillsWithTask.contains(s)).collect(Collectors.toSet());

		// Non-customized empty skills based on chosen path
		Set<Skill> emptySkills;
		Set<Skill> notOwnSkills = skillRepository.findAll().stream()
				.filter(s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
						editionId))
				.filter(s -> !ownSkills.contains(s)).collect(Collectors.toSet());

		if (personPathPreference.isEmpty() || personPathPreference.get(0).getPath() == null) {
			emptySkills = notOwnSkills.stream().filter(s -> s.getTasks().isEmpty())
					.collect(Collectors.toSet());
		} else {
			Path personPath = personPathPreference.get(0).getPath();
			emptySkills = notOwnSkills.stream().filter(s -> s.getTasks().stream()
					.filter(t -> t.getPaths().contains(personPath)).collect(Collectors.toSet()).isEmpty())
					.collect(Collectors.toSet());
		}
		emptySkills.addAll(ownEmptySkills);
		return emptySkills;
	}

	/**
	 * Checks if the given empty skill can be considered complete. If so, adds it to skillsDone. An empty
	 * skill is complete if all its essential parents are complete.
	 *
	 * @param skillsDone  The list of completed skills
	 * @param emptySkills The list of empty skills
	 * @param empty       The empty skill that needs to be checked
	 */
	public void addCompletedEmptySkills(Set<Skill> skillsDone, Set<Skill> emptySkills, Skill empty) {
		if (skillsDone.contains(empty)) {
			return;
		}
		List<AbstractSkill> parents = empty.getParents().stream().filter(p -> {
			Skill sk = (Skill) p;
			return sk.isEssential();
		}).toList();

		List<AbstractSkill> notCompletedParents = parents.stream()
				.filter(x -> !skillsDone.contains((Skill) x)).toList();

		for (AbstractSkill parent : notCompletedParents) {
			if (!emptySkills.contains((Skill) parent)) {
				return;
			}
			addCompletedEmptySkills(skillsDone, emptySkills, (Skill) parent);
			if (!skillsDone.contains(parent)) {
				return;
			}
		}
		skillsDone.add(empty);
	}
}
