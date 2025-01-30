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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.view.InfoboxDTO;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.TaskCompletionService;

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
	 * Returns needed information for the infobox in form of a JSONObject, namely: - studentAndAuthenticated:
	 * whether the user is a student and authenticated - completedSomeTask: whether the user has completed a
	 * task with timestamp - taskInfo: the string for the larger text, containing the task name -
	 * locationString the string for the smaller text, containing the location of the task - link: the link to
	 * the skill of the most recent task
	 *
	 * @return A InfoboxDTO containing the needed/available information to render the infobox. May contain
	 *         null fields, if the user is not a student, not authenticated or has not completed a task yet.
	 */
	@GetMapping
	@ResponseBody
	public InfoboxDTO getInformation() {
		Person authPerson = authorisationService.getAuthPerson();

		// Not enabled in student mode
		boolean studentAndAuthenticated = authorisationService.isAuthenticated()
				&& !authorisationService.isStaff();

		if (studentAndAuthenticated) {
			RegularTask latestTask = taskCompletionService.latestTaskCompletion(authPerson);

			if (latestTask != null) {
				String taskInfo = "Last worked on: " + latestTask.getName();

				long moduleId = latestTask.getSkill().getSubmodule().getModule().getId();
				long skillId = latestTask.getSkill().getId();
				String link = "/module/" + moduleId + "#block-" + skillId + "-name";

				String locationString = "In " + taskCompletionService.getLocationString(latestTask);

				return new InfoboxDTO(true, true, taskInfo, link, locationString);
			} else {
				return InfoboxDTO.builder().studentAndAuthenticated(true).completedSomeTask(false).build();
			}
		} else {
			return InfoboxDTO.builder().studentAndAuthenticated(false).build();
		}
	}

}
