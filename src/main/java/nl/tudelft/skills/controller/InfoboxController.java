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

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.TaskCompletionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("infobox")
public class InfoboxController {

	private final AuthorisationService authorisationService;
	private final TaskCompletionService taskCompletionService;

	private enum State {
		HIDDEN,
		COLLAPSED,
		EXPANDED
	}

	@Autowired
	public InfoboxController(AuthorisationService authorisationService,
			TaskCompletionService taskCompletionService) {
		this.authorisationService = authorisationService;
		this.taskCompletionService = taskCompletionService;
	}

	/**
	 * Gets the fragment for the infobox rendered with the needed parameters.
	 *
	 * @param  model The model to add data to
	 * @param  state The state of the infobox, namely HIDDEN, EXPANDED or COLLAPSED
	 * @return       The fragment for the infobox
	 */
	@RequestMapping(value = { "", "/{state}" }, method = RequestMethod.GET)
	public String getInfoBox(Model model, @PathVariable(required = false) State state) {
		Person authPerson = authorisationService.getAuthPerson();

		// TODO should it be enabled in student mode?
		boolean studentAndAuthenticated = authorisationService.isAuthenticated()
				&& !authorisationService.isStaff();
		model.addAttribute("studentAndAuthenticated", studentAndAuthenticated);

		Task latestTask = taskCompletionService.latestTaskCompletion(authPerson);
		model.addAttribute("completedSomeTask", latestTask != null);

		switch (state) {
			case HIDDEN -> model.addAttribute("state", "hidden");
			case EXPANDED -> model.addAttribute("state", "expanded");
			case COLLAPSED -> model.addAttribute("state", "collapsed");
		}

		if (latestTask != null) {
			model.addAttribute("latestTask", latestTask);

			long moduleId = latestTask.getSkill().getSubmodule().getModule().getId();
			model.addAttribute("moduleId", moduleId);

			long skillId = latestTask.getSkill().getId();
			model.addAttribute("skillId", skillId);

			String locationString = taskCompletionService.getLocationString(latestTask);
			model.addAttribute("locationString", locationString);
		}

		return "infobox :: infobox";
	}
}
