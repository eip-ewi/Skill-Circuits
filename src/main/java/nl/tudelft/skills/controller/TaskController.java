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
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("task")
public class TaskController {

	private final TaskRepository taskRepository;

	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@GetMapping("{taskId}")
	public String getTask(@PathVariable Long taskId, Model model) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		model.addAttribute("item", View.convert(task, TaskViewDTO.class));
		model.addAttribute("canEdit", false);
		return "task/view";
	}

	@GetMapping("{taskId}/right")
	public String getTaskForCustomPath(@PathVariable Long taskId, Model model) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		model.addAttribute("item", View.convert(task, TaskViewDTO.class));
		model.addAttribute("canEdit", false);
		return "task/leftview";
	}

}
