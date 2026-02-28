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

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.PersonalPreferences;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.bookmark.PersonalBookmarkList;
import nl.tudelft.skills.repository.PersonRepository;

@Service
@AllArgsConstructor
public class SCPersonService {

	private final PersonControllerApi personControllerApi;

	private final PersonRepository personRepository;

	@Transactional
	public SCPerson getOrCreate(Long personId) {
		return personRepository.findById(personId).orElseGet(() -> {
			PersonSummaryDTO person = requireNonNull(
					personControllerApi.getPeopleById(List.of(personId)).blockFirst());

			boolean isEmployee = person.getDefaultRole() != PersonSummaryDTO.DefaultRoleEnum.STUDENT;
			SCPerson scPerson = SCPerson.builder().id(person.getId())
					.viewMode(isEmployee ? ViewMode.EDITOR : ViewMode.VIEWER)
					.build();

			scPerson.setBookmarkLists(Set.of(PersonalBookmarkList.builder()
					.name("My bookmarks")
					.person(scPerson)
					.build()));

			scPerson.setPreferences(PersonalPreferences.builder().build());

			// Attempt to find again in case the person was already created in the meantime
			return personRepository.findById(personId).orElseGet(() -> personRepository.save(scPerson));
		});
	}

	public void setViewMode(SCPerson person, ViewMode viewMode) {
		person.setViewMode(viewMode);
		personRepository.save(person);
	}

}
