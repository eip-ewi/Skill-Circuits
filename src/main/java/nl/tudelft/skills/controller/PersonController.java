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

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.PersonalPreferencesView;
import nl.tudelft.skills.enums.Theme;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.PersonService;
import nl.tudelft.skills.service.PersonalPreferencesService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/person")
public class PersonController {

	private final PersonService personService;
	private final PersonalPreferencesService personalPreferencesService;

	@GetMapping("search")
	@PreAuthorize("@authorisationService.canSearchForPeople()")
	public List<PersonSummaryDTO> searchForPeople(@RequestParam String query) {
		return personService.searchForPeople(query);
	}

	@GetMapping("preferences")
	public PersonalPreferencesView getPreferences(@AuthenticatedSCPerson SCPerson scPerson) {
		return personalPreferencesService
				.convertToPreferencesView(personalPreferencesService.getPreferencesOfPerson(scPerson));
	}

	@PatchMapping("preferences/blur")
	public PersonalPreferencesView setBlurSkillsPreference(@AuthenticatedSCPerson SCPerson scPerson,
			@RequestParam boolean blurSkills) {
		return personalPreferencesService
				.convertToPreferencesView(personalPreferencesService.setBlurSkills(scPerson, blurSkills));
	}

	@PatchMapping("preferences/theme")
	public PersonalPreferencesView setTheme(@AuthenticatedSCPerson SCPerson scPerson,
			@RequestParam Theme theme) {
		return personalPreferencesService
				.convertToPreferencesView(personalPreferencesService.setTheme(scPerson, theme));
	}
}
