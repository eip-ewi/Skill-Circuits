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

import java.util.Collections;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.AfterTaskCompletionCircuitUpdate;
import nl.tudelft.skills.dto.patch.SubtaskMove;
import nl.tudelft.skills.dto.patch.TaskInfoPatch;
import nl.tudelft.skills.dto.patch.TaskMove;
import nl.tudelft.skills.dto.view.circuit.module.ModuleLevelTaskView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.service.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/task-info")
public class TaskInfoController {

	private final ClickedLinkService clickedLinkService;
	private final TaskCompletionService taskCompletionService;
	private final TaskInfoService taskInfoService;
	private final ModuleCircuitService moduleCircuitService;

	@PostMapping("{taskInfo}/click")
	@PreAuthorize("@authorisationService.canViewTaskInfo(#taskInfo)")
	public void reportClickedList(@AuthenticatedSCPerson SCPerson person, @PathEntity TaskInfo taskInfo) {
		clickedLinkService.reportClickedLink(person, taskInfo);
	}

	@PatchMapping("{taskInfo}")
	@PreAuthorize("@authorisationService.canEditTaskInfo(#taskInfo)")
	public void patchTaskInfo(@PathEntity TaskInfo taskInfo, @RequestBody TaskInfoPatch patch) {
		taskInfoService.patchTaskInfo(taskInfo, patch);
	}

	@PatchMapping("{subtask}/task")
	@PreAuthorize("@authorisationService.canEditTaskInfo(#subtask) and @authorisationService.canEditTask(#move.choiceTask)")
	public void moveSubtask(@PathEntity TaskInfo subtask, @RequestBody SubtaskMove move) {
		taskInfoService.moveSubtask(subtask, move);
	}

	@PatchMapping("{choiceTask}/move-subtask/{subtask}")
	@PreAuthorize("@authorisationService.canEditSkill(#move.skill) and @authorisationService.canEditSkill(#subtask.choiceTask.skill)"
			+
			"and @authorisationService.canEditTaskInfo(#subtask) and @authorisationService.canEditTask(#choiceTask)")
	public ModuleLevelTaskView.Regular moveSubtaskOutsideChoiceTask(
			@AuthenticatedSCPerson SCPerson person, @PathEntity ChoiceTask choiceTask,
			@PathEntity TaskInfo subtask, @RequestBody TaskMove move) {
		return (ModuleLevelTaskView.Regular) moduleCircuitService
				.convertToTaskView(taskInfoService.moveSubtaskOutsideChoiceTask(choiceTask, subtask, move),
						person);
	}

	@PostMapping("{taskInfo}/complete")
	@PreAuthorize("@authorisationService.canViewTaskInfo(#taskInfo)")
	public AfterTaskCompletionCircuitUpdate updateTaskCompletion(@AuthenticatedSCPerson SCPerson person,
			@PathEntity TaskInfo taskInfo, @RequestParam boolean completed) {
		if (completed) {
			return taskCompletionService.completeTask(person, taskInfo);
		} else {
			taskCompletionService.uncompleteTask(person, taskInfo);
			return new AfterTaskCompletionCircuitUpdate(Collections.emptySet());
		}
	}

	@DeleteMapping("{taskInfo}")
	@PreAuthorize("@authorisationService.canEditTaskInfo(#taskInfo)")
	public void deleteTaskInfo(@PathEntity TaskInfo taskInfo) {
		taskInfoService.deleteTaskInfo(taskInfo);
	}

}
