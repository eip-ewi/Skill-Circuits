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

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.TaskCreateDTO;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("task")
public class TaskController {

	private final TaskRepository taskRepository;

	@Autowired
	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	/**
	 * Creates a task.
	 *
	 * @param  create The DTO with information to create the task
	 * @return        A new skill html element
	 */
	@PostMapping
	@Transactional
	@PreAuthorize("@authorisationService.canEditSkill(#create.skill.id)")
	public String createTask(TaskCreateDTO create, Model model) {
		Task task = taskRepository.save(create.apply());
		model.addAttribute("level", "module");
		model.addAttribute("item", View.convert(task, TaskViewDTO.class));
		model.addAttribute("block", task.getSkill());
		return "item/view";
	}

	/**
	 * Deletes a task.
	 *
	 * @param  id The id of the task to delete
	 * @return    A redirect to the module page
	 */
	@DeleteMapping
	@Transactional
	@PreAuthorize("@authorisationService.canDeleteTask(#id)")
	public String deleteTask(@RequestParam Long id) {
		Task task = taskRepository.findByIdOrThrow(id);
		taskRepository.delete(task);
		return "redirect:/module/" + task.getSkill().getSubmodule().getModule().getId();
	}

	/**
	 * Patches a skill.
	 *
	 * @param  patch The patch containing the new data
	 * @return       Empty 200 response
	 */
	@PatchMapping
	@Transactional
	@PreAuthorize("@authorisationService.canEditTask(#patch.id)")
	public ResponseEntity<Void> patchTask(TaskPatchDTO patch) {
		Task task = taskRepository.findByIdOrThrow(patch.getId());
		taskRepository.save(patch.apply(task));
		return ResponseEntity.ok().build();
	}

}
