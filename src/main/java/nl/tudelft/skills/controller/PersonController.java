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
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.ClickedLinkRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.TaskCompletionService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/person")
public class PersonController {

	private final TaskRepository taskRepository;
	private final PersonRepository scPersonRepository;
	private final TaskCompletionService taskCompletionService;
	private final ClickedLinkRepository clickedLinkRepository;

	public PersonController(TaskRepository taskRepository, PersonRepository scPersonRepository,
			TaskCompletionService taskCompletionService, ClickedLinkRepository clickedLinkRepository) {
		this.taskRepository = taskRepository;
		this.scPersonRepository = scPersonRepository;
		this.taskCompletionService = taskCompletionService;
		this.clickedLinkRepository = clickedLinkRepository;
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
			taskCompletionService.addTaskCompletion(person, task);
			List<Task> completedTasks = person.getTaskCompletions().stream()
					.map(TaskCompletion::getTask).toList();

			// TODO skill remains visible (see issue #90)
			return new TaskCompletedDTO(task.getRequiredFor().stream()
					.filter(s -> completedTasks.containsAll(s.getRequiredTasks()))
					.map(Skill::getId).toList());
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
	 * Saves the clicked link by a person
	 *
	 * @param authPerson the person clicking the link
	 * @param taskId     the id of the task the link is part of
	 */
	@PutMapping("clicked/{taskId}")
	@Transactional
	public void logClickedLinkByPerson(@AuthenticatedPerson Person authPerson,
			@PathVariable Long taskId) {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Task task = taskRepository.findByIdOrThrow(taskId);

		clickedLinkRepository.save(ClickedLink.builder()
				.task(task).person(person).build());

	}

	@GetMapping("clickedlinks")
	@Transactional
	@PreAuthorize("@authorisationService.isAdmin()")
	public String downloadClickedLinks() throws JsonProcessingException {
		var logs = clickedLinkRepository.findAll();

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		SimpleModule module = new SimpleModule();
		module.addSerializer(ClickedLink.class, new CustomClickedLinkSerializer());
		mapper.registerModule(module);

		return mapper.writeValueAsString(logs);
	}
}
