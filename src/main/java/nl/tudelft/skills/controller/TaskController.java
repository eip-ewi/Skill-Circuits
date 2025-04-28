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

import nl.tudelft.skills.dto.create.ChoiceTaskCreate;
import nl.tudelft.skills.dto.create.RegularTaskCreate;
import nl.tudelft.skills.dto.patch.ChoiceTaskPatch;
import nl.tudelft.skills.dto.patch.TaskPatch;
import nl.tudelft.skills.dto.patch.TaskMove;
import nl.tudelft.skills.dto.view.circuit.module.ModuleLevelTaskView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.service.ModuleCircuitService;
import nl.tudelft.skills.service.TaskService;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.service.TaskCompletionService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final ModuleCircuitService moduleCircuitService;
	private final TaskCompletionService taskCompletionService;
    private final TaskService taskService;

    @PostMapping
    public ModuleLevelTaskView createTask(@AuthenticatedSCPerson SCPerson person, @RequestBody RegularTaskCreate create) {
        return moduleCircuitService.convertToTaskView(taskService.createTask(create), person);
    }

    @PostMapping("choice")
    public ModuleLevelTaskView createChoiceTask(@AuthenticatedSCPerson SCPerson person, @RequestBody ChoiceTaskCreate create) {
        return moduleCircuitService.convertToTaskView(taskService.createTask(create), person);
    }

    @PatchMapping("{task}")
    public void patchTask(@PathEntity Task task, @RequestBody TaskPatch patch) {
        taskService.patchTask(task, patch);
    }

    @PatchMapping("choice/{task}")
    public void patchChoiceTask(@PathEntity ChoiceTask task, @RequestBody ChoiceTaskPatch patch) {
        taskService.patchTask(task, patch);
    }

    @PatchMapping("{task}/index")
    public void updateTaskIndex(@PathEntity Task task, @RequestParam Integer index) {
        taskService.updateTaskIndex(task, index);
    }

    @PatchMapping("{task}/skill")
    public void moveTask(@PathEntity Task task, @RequestBody TaskMove move) {
        taskService.moveTask(task, move);
    }

    @PostMapping("{choiceTask}/add-subtask/{subtask}")
    public ModuleLevelTaskView.ChoiceTaskChoiceView moveTaskInsideChoiceTask(@AuthenticatedSCPerson SCPerson person, @PathEntity ChoiceTask choiceTask, @PathEntity RegularTask subtask) {
        return moduleCircuitService.convertToChoiceView(taskService.moveTaskInsideChoiceTask(choiceTask, subtask), person);
    }

    @DeleteMapping("{task}")
    public void deleteTask(@PathEntity Task task) {
        taskService.deleteTask(task);
    }

}
