/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.create.ChoiceTaskCreate;
import nl.tudelft.skills.dto.create.RegularTaskCreate;
import nl.tudelft.skills.dto.patch.ChoiceTaskPatch;
import nl.tudelft.skills.dto.patch.TaskMove;
import nl.tudelft.skills.dto.patch.TaskPatch;
import nl.tudelft.skills.dto.view.circuit.module.ModuleLevelTaskView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.service.ModuleCircuitService;
import nl.tudelft.skills.service.TaskService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

	private final ModuleCircuitService moduleCircuitService;
	private final TaskService taskService;

	@PostMapping
	@PreAuthorize("@authorisationService.canEditSkill(#create.skill)")
	public ModuleLevelTaskView createTask(@AuthenticatedSCPerson SCPerson person,
			@RequestBody RegularTaskCreate create) {
		return moduleCircuitService.convertToTaskView(taskService.createTask(create), person);
	}

	@PostMapping("choice")
	@PreAuthorize("@authorisationService.canEditSkill(#create.skill)")
	public ModuleLevelTaskView createChoiceTask(@AuthenticatedSCPerson SCPerson person,
			@RequestBody ChoiceTaskCreate create) {
		return moduleCircuitService.convertToTaskView(taskService.createTask(create), person);
	}

	@PatchMapping("{task}")
	@PreAuthorize("@authorisationService.canEditSkill(#task.skill)")
	public void patchTask(@PathEntity Task task, @RequestBody TaskPatch patch) {
		taskService.patchTask(task, patch);
	}

	@PatchMapping("choice/{task}")
	@PreAuthorize("@authorisationService.canEditSkill(#task.skill)")
	public void patchChoiceTask(@PathEntity ChoiceTask task, @RequestBody ChoiceTaskPatch patch) {
		taskService.patchTask(task, patch);
	}

	@PatchMapping("{task}/index")
	@PreAuthorize("@authorisationService.canEditSkill(#task.skill)")
	public void updateTaskIndex(@PathEntity Task task, @RequestParam Integer index) {
		taskService.updateTaskIndex(task, index);
	}

	@PatchMapping("{task}/skill")
	@PreAuthorize("@authorisationService.canEditSkill(#task.skill) and @authorisationService.canEditSkill(#move.skill)")
	public void moveTask(@PathEntity Task task, @RequestBody TaskMove move) {
		taskService.moveTask(task, move);
	}

	@PostMapping("{choiceTask}/add-subtask/{subtask}")
	@PreAuthorize("@authorisationService.canEditSkill(#choiceTask.skill) and @authorisationService.canEditSkill(#subtask.skill)")
	public ModuleLevelTaskView.ChoiceTaskChoiceView moveTaskInsideChoiceTask(
			@AuthenticatedSCPerson SCPerson person, @PathEntity ChoiceTask choiceTask,
			@PathEntity RegularTask subtask) {
		return moduleCircuitService
				.convertToChoiceView(taskService.moveTaskInsideChoiceTask(choiceTask, subtask), person);
	}

	@DeleteMapping("{task}")
	@PreAuthorize("@authorisationService.canEditSkill(#task.skill)")
	public void deleteTask(@PathEntity Task task) {
		taskService.deleteTask(task);
	}

}
