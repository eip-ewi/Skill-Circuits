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

import nl.tudelft.skills.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.repository.TaskCompletionRepository;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class TaskCompletionService {

	private final TaskCompletionRepository taskCompletionRepository;

    public Optional<Task> getLastCompletedTask(SCPerson person) {
        return taskCompletionRepository.findLastTaskCompletedFor(person)
                .map(completion -> completion.getTask().getTask() == null ? completion.getTask().getChoiceTask() : completion.getTask().getTask());
    }

	/**
	 * Saves a TaskCompletion to the repository, given the corresponding SCPerson and Task.
	 *
	 * @param person The SCPerson that completed the Task
	 * @param task   The Task that was completed
	 */
	public void completeTask(SCPerson person, TaskInfo task) {
		taskCompletionRepository.save(TaskCompletion.builder().task(task).person(person).build());
	}

	/**
	 * Deletes a TaskCompletion from the repository, given the corresponding SCPerson and Task. If such a
	 * TaskCompletion does not exist, returns null.
	 *
	 * @param person The SCPerson that had completed the Task
	 * @param task   The Task that was completed
	 */
	public void uncompleteTask(SCPerson person, TaskInfo task) {
        taskCompletionRepository.deleteByPersonAndTask(person, task);
	}

}
