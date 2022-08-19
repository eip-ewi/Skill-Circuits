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
package nl.tudelft.skills.controller;

import java.util.Collections;
import java.util.List;

import javax.transaction.Transactional;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.view.TaskCompletedDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person")
public class PersonController {

	private final TaskRepository taskRepository;
	private final PersonRepository scPersonRepository;

	public PersonController(TaskRepository taskRepository, PersonRepository scPersonRepository) {
		this.taskRepository = taskRepository;
		this.scPersonRepository = scPersonRepository;
	}

	/**
	 * Marks a certain task as completed or uncompleted for a certain person.
	 *
	 * @param authPerson the currently authenticated person
	 * @param taskId     the id of the task
	 * @param completed  whether the task has been completed or uncompleted
	 */
	@PutMapping("completion/{taskId}")
	@Transactional
	@ResponseBody
	public TaskCompletedDTO updateTaskCompletedForPerson(@AuthenticatedPerson Person authPerson,
			@PathVariable Long taskId, @RequestBody boolean completed) {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Task task = taskRepository.findByIdOrThrow(taskId);
		if (completed) {
			person.getTasksCompleted().add(task);
			// TODO skill remains visible (see issue #90)
			return new TaskCompletedDTO(task.getRequiredFor().stream()
					.filter(s -> person.getTasksCompleted().containsAll(s.getRequiredTasks()))
					.map(Skill::getId).toList());
		} else {
			person.getTasksCompleted().remove(task);
		}
		return new TaskCompletedDTO(Collections.emptyList());
	}

	/**
	 * Marks a list of task as completed for a certain person.
	 *
	 * @param authPerson     the currently authenticated person
	 * @param completedTasks a list of ids of completed tasks
	 */
	@PutMapping("complete")
	@Transactional
	public void setTasksCompletedForPerson(@AuthenticatedPerson Person authPerson,
			@RequestBody List<Long> completedTasks) {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		person.getTasksCompleted().addAll(taskRepository.findAllById(completedTasks));
	}
}
