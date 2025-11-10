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
package nl.tudelft.skills.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.view.PersonalPreferencesView;
import nl.tudelft.skills.dto.view.ThemeView;
import nl.tudelft.skills.enums.Theme;
import nl.tudelft.skills.model.PersonalPreferences;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.PersonalPreferencesRepository;

@Service
@AllArgsConstructor
public class PersonalPreferencesService {

	private final PersonalPreferencesRepository personalPreferencesRepository;

	/**
	 * Sets the "blur skills" personal preference of a user.
	 *
	 * @param  scPerson   The person.
	 * @param  blurSkills Whether to blur skills.
	 * @return            The updated preferences of the person.
	 */
	@Transactional
	public PersonalPreferences setBlurSkills(SCPerson scPerson, boolean blurSkills) {
		PersonalPreferences preferences = scPerson.getPreferences();
		preferences.setBlurSkills(blurSkills);
		return personalPreferencesRepository.save(preferences);
	}

	/**
	 * Sets the theme of a person.
	 *
	 * @param  scPerson The person.
	 * @param  theme    The theme.
	 * @return          The updated preferences of the person.
	 */
	@Transactional
	public PersonalPreferences setTheme(SCPerson scPerson, Theme theme) {
		PersonalPreferences preferences = scPerson.getPreferences();
		preferences.setTheme(theme);
		return personalPreferencesRepository.save(preferences);
	}

	/**
	 * Get the view of the personal preferences.
	 *
	 * @param  preferences The personal preferences.
	 * @return             The view of the preferences.
	 */
	public PersonalPreferencesView convertToPreferencesView(PersonalPreferences preferences) {
		ThemeView themeView = new ThemeView(preferences.getTheme().getName(),
				preferences.getTheme().getColourScheme(), preferences.getTheme().getDisplayName());
		return new PersonalPreferencesView(themeView, preferences.isBlurSkills());
	}

	/**
	 * Get the personal preferences of a person.
	 *
	 * @param  scPerson The person.
	 * @return          The personal preferences of the person.
	 */
	@Transactional
	public PersonalPreferences getPreferencesOfPerson(SCPerson scPerson) {
		return personalPreferencesRepository.findByPersonId(scPerson.getId());
	}
}
