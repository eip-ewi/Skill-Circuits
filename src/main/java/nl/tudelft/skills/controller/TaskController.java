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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.EditLinkDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.repository.RegularTaskRepository;
import nl.tudelft.skills.security.AuthorisationService;

@Controller
@RequestMapping("task")
public class TaskController {
	private final RegularTaskRepository regularTaskRepository;
	private AuthorisationService authorisationService;

	@Autowired
	public TaskController(RegularTaskRepository regularTaskRepository,
			AuthorisationService authorisationService) {
		this.regularTaskRepository = regularTaskRepository;
		this.authorisationService = authorisationService;
	}

	/**
	 * Update the link of a task.
	 *
	 * @param  editLinkDTO The DTO containing the task and the new link.
	 * @return             Empty 200 OK response.
	 */
	@Transactional
	@PatchMapping("change-link")
	@PreAuthorize("@authorisationService.canEditAbstractTask(#editLinkDTO.taskId)")
	public ResponseEntity<Void> updateTaskLink(@RequestBody EditLinkDTO editLinkDTO) {
		RegularTask task = regularTaskRepository.findByIdOrThrow(editLinkDTO.getTaskId());
		task.setLink(editLinkDTO.getNewLink());
		regularTaskRepository.save(task);

		return ResponseEntity.ok().build();
	}

	// TODO: getting a choice task

	/**
	 * Returns a task view for a custom path.
	 *
	 * @param  taskId The id of the task.
	 * @param  model  The model to configure.
	 * @return        Html page with the task.
	 */
	@GetMapping("{taskId}")
	@PreAuthorize("@authorisationService.isAuthenticated()")
	public String getTask(@PathVariable Long taskId, Model model) {
		RegularTask task = regularTaskRepository.findByIdOrThrow(taskId);
		if (task.getSkill().getSubmodule().getModule().getEdition().isVisible() || authorisationService
				.isAtLeastHeadTAInEdition(task.getSkill().getSubmodule().getModule().getEdition().getId())) {
			model.addAttribute("item", View.convert(task, TaskViewDTO.class));
			model.addAttribute("canEdit", false);
			model.addAttribute("level", "module");
			return "task/view";
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN);
	}

	// TODO: getting a choice task in an exploded skill

	/**
	 * Returns task view paths overview in an exploded skill.
	 *
	 * @param  taskId The id of the task.
	 * @param  model  The model to configure.
	 * @return        Html page with the task.
	 */
	@GetMapping("{taskId}/preview")
	@PreAuthorize("@authorisationService.isAuthenticated()")
	public String getTaskForCustomPath(@PathVariable Long taskId, Model model) {
		RegularTask task = regularTaskRepository.findByIdOrThrow(taskId);
		if (task.getSkill().getSubmodule().getModule().getEdition().isVisible() || authorisationService
				.isAtLeastHeadTAInEdition(task.getSkill().getSubmodule().getModule().getEdition().getId())) {
			model.addAttribute("item", View.convert(task, TaskViewDTO.class));
			model.addAttribute("canEdit", false);
			return "task/inactiveview :: item";
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN);
	}

}
