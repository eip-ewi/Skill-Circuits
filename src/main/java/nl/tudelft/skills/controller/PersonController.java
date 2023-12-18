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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.view.TaskCompletedDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.TaskCompletionService;

@RestController
@RequestMapping("/person")
@AllArgsConstructor
public class PersonController {

	private final TaskRepository taskRepository;
	private final PersonRepository scPersonRepository;
	private final TaskCompletionService taskCompletionService;
	private final SkillRepository skillRepository;
	private final PathRepository pathRepository;

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
			taskCompletionService.addTaskCompletion(person, task);
			List<Task> completedTasks = person.getTaskCompletions().stream()
					.map(TaskCompletion::getTask).toList();

			// TODO skill remains visible (see issue #90)
			return new TaskCompletedDTO(task.getRequiredFor().stream()
					.filter(s -> completedTasks.containsAll(s.getRequiredTasks()))
					.map(Skill::getId).toList());
//			store unlocked tasks in authPerson.tasksRevealed

		} else {
			taskCompletionService.deleteTaskCompletion(person, task);
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

		List<Task> tasks = taskRepository.findAllById(completedTasks);
		tasks.forEach(task -> taskCompletionService.addTaskCompletion(person, task));
	}

	/**
	 * Adds a task to the user custom path.
	 *
	 * @param authPerson the currently authenticated person.
	 * @param taskId     id of task to be added to custom path.
	 */
	@PutMapping("add/{taskId}")
	@Transactional
	public void addTaskToOwnPath(@AuthenticatedPerson Person authPerson, @PathVariable Long taskId) {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Task task = taskRepository.findByIdOrThrow(taskId);

		// if first time modifying skill, put all tasks from current path in own path

		if (!person.getSkillsModified().contains(task.getSkill())) {
			addAllTaskFromCurrentPath(person, task);
			person.getSkillsModified().add(task.getSkill());
		}

		person.getTasksAdded().add(task);

	}

	/**
	 * Remove a task from the user custom path.
	 *
	 * @param authPerson the currently authenticated person.
	 * @param taskId     id of task to be added to custom path.
	 */
	@PutMapping("remove/{taskId}")
	@Transactional
	public void removeTaskFromOwnPath(@AuthenticatedPerson Person authPerson, @PathVariable Long taskId) {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Task task = taskRepository.findByIdOrThrow(taskId);

		// if first time modifying skill, put all tasks from current path in own path
		if (!person.getSkillsModified().contains(task.getSkill())) {
			addAllTaskFromCurrentPath(person, task);
			person.getSkillsModified().add(task.getSkill());
		}

		person.getTasksAdded().remove(task);
	}

	/**
	 * Resets a custom skill for a student. All tasks in the skill will be according to the selected path.
	 *
	 * @param authPerson the currently authenticated person.
	 * @param skillId    id of the skill to be reset.
	 */
	@PostMapping("reset/{skillId}")
	@Transactional
	public void resetSkill(@AuthenticatedPerson Person authPerson, @PathVariable Long skillId,
			HttpServletResponse response) throws IOException {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Skill skill = skillRepository.findByIdOrThrow(skillId);

		// Remove the skill from the set of custom skills
		person.setSkillsModified(
				person.getSkillsModified().stream().filter(s -> !s.getId().equals(skillId))
						.collect(Collectors.toSet()));
		//Remove the tasks from the custom skill
		person.setTasksAdded(
				person.getTasksAdded().stream().filter(t -> !t.getSkill().getId().equals(skillId))
						.collect(Collectors.toSet()));

		response.sendRedirect("/module/" + skill.getSubmodule().getModule().getId() + "#block-" + skillId);
	}

	/**
	 * Takes all tasks from a skill on current path and adds them to the set of modified tasks.
	 *
	 * @param person
	 * @param task
	 */
	void addAllTaskFromCurrentPath(SCPerson person, Task task) {
		Optional<Long> currentPathId = person.getPathPreferences().stream()
				.filter(p -> p.getEdition().equals(task.getSkill().getSubmodule().getModule().getEdition()))
				.filter(p -> p.getPath() != null)
				.map(p -> p.getPath().getId()).findFirst();

		currentPathId.ifPresentOrElse(id -> {
			Path path = pathRepository.getById(id);
			task.getSkill().getTasks().forEach(t -> {
				if (t.getPaths().contains(path))
					person.getTasksAdded().add(t);
			});
		}, () -> {
			// add all tasks
			task.getSkill().getTasks().forEach(t -> person.getTasksAdded().add(t));
		});
	}

}
