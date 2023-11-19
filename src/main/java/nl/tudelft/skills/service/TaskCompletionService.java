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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
public class TaskCompletionService {

	private final EditionControllerApi editionApi;
	private final PersonRepository personRepository;
	private final TaskCompletionRepository taskCompletionRepository;

	@Autowired
	public TaskCompletionService(TaskCompletionRepository taskCompletionRepository,
			PersonRepository personRepository, EditionControllerApi editionApi) {
		this.taskCompletionRepository = taskCompletionRepository;
		this.personRepository = personRepository;
		this.editionApi = editionApi;
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
}
