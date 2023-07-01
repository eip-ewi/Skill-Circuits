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
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
	// TODO add PreAuthorize
	public ResponseEntity<Void> updateTaskLink(@RequestBody EditLinkDTO editLinkDTO) {
		// Update task link if it has changed
		Task task = taskRepository.getById(editLinkDTO.getTaskId());
		if (!task.getLink().equals(editLinkDTO.getNewLink())) {
			task.setLink(editLinkDTO.getNewLink());
			taskRepository.save(task);
		}

		return ResponseEntity.ok().build();
	}
}
