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

import nl.tudelft.skills.dto.view.EditLinkDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;

import org.springframework.stereotype.Controller;
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
	 * Update the link of a task.
	 *
	 * @param  editLinkDTO The DTO containing the task and the new link.
	 * @return             Empty 200 OK response.
	 */
	@Transactional
	@PatchMapping("change-link")
	@PreAuthorize("@authorisationService.canEditTask(#editLinkDTO.taskId)")
	public ResponseEntity<Void> updateTaskLink(@RequestBody EditLinkDTO editLinkDTO) {
		Task task = taskRepository.findByIdOrThrow(editLinkDTO.getTaskId());
		task.setLink(editLinkDTO.getNewLink());
		taskRepository.save(task);

		return ResponseEntity.ok().build();
	}

	/**
	 * Returns a task view for a custom path.
	 *
	 * @param  taskId The id of the task.
	 * @param  model  The model to configure.
	 * @return        Html page with the task.
	 */
	@GetMapping("{taskId}")
	public String getTask(@PathVariable Long taskId, Model model) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		model.addAttribute("item", View.convert(task, TaskViewDTO.class));
		model.addAttribute("canEdit", false);
		return "task/view";
	}

	/**
	 * Returns task view paths overview in a exploded skill.
	 *
	 * @param  taskId The id of the task.
	 * @param  model  The model to configure.
	 * @return        Html page with the task.
	 */
	@GetMapping("{taskId}/preview")
	public String getTaskForCustomPath(@PathVariable Long taskId, Model model) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		model.addAttribute("item", View.convert(task, TaskViewDTO.class));
		model.addAttribute("canEdit", false);
		return "task/inactiveview :: item";
	}

}
