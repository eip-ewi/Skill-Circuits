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

import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.service.TaskCompletionService;

@RestController
@AllArgsConstructor
@RequestMapping("api/task")
public class TaskController {

	private final TaskCompletionService taskCompletionService;

	@ResponseBody
	@PostMapping("{task}/complete")
	public void updatePosition(@AuthenticatedSCPerson SCPerson person, @PathEntity Task task,
			@RequestParam boolean completed) {
		if (completed) {
			taskCompletionService.completeTask(person, task);
		} else {
			taskCompletionService.uncompleteTask(person, task);
		}
	}

}
