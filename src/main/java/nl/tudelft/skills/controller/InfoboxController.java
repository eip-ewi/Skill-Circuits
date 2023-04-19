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

import net.minidev.json.JSONObject;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.TaskCompletionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("infobox")
public class InfoboxController {

	private final AuthorisationService authorisationService;
	private final TaskCompletionService taskCompletionService;

	@Autowired
	public InfoboxController(AuthorisationService authorisationService,
			TaskCompletionService taskCompletionService) {
		this.authorisationService = authorisationService;
		this.taskCompletionService = taskCompletionService;
	}

	/**
	 * Returns needed information for the infobox in form of a JSONObject, namely: - studentAndAuthenticated
	 * whether the user is a student and authenticated - completedSomeTask whether the user has completed a
	 * task with timestamp - taskInfo the string for the larger text, containing the task name -
	 * locationString the string for the smaller text, containing the location of the task - link the link to
	 * the skill of the most recent task
	 *
	 * @return A ResponseEntity of a JSONObject containing the needed information to render the infobox.
	 */
	@GetMapping
	public ResponseEntity<JSONObject> getInformation() {
		JSONObject information = new JSONObject();
		Person authPerson = authorisationService.getAuthPerson();

		// Not enabled in student mode
		boolean studentAndAuthenticated = authorisationService.isAuthenticated()
				&& !authorisationService.isStaff();
		information.put("studentAndAuthenticated", studentAndAuthenticated);

		if (studentAndAuthenticated) {
			Task latestTask = taskCompletionService.latestTaskCompletion(authPerson);
			information.put("completedSomeTask", latestTask != null);

			if (latestTask != null) {
				information.put("taskInfo", "Last worked on: " + latestTask.getName());

				long moduleId = latestTask.getSkill().getSubmodule().getModule().getId();
				long skillId = latestTask.getSkill().getId();
				information.put("link", "/module/" + moduleId + "#block-" + skillId + "-name");

				String locationString = taskCompletionService.getLocationString(latestTask);
				information.put("locationString", "In " + locationString);
			}
		}

		return ResponseEntity.ok(information);
	}
}
