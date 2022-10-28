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
package nl.tudelft.skills.service;

import java.util.Optional;

import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {

	private PersonRepository personRepository;

	@Autowired
	public PersonService(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	/**
	 * Get user saved path for an edition.
	 *
	 * @param  personId  person id.
	 * @param  editionId edition id.
	 * @return           path followed in edition by person or none if none was saved or path is no-path.
	 */
	public Optional<PathPreference> getPathForEdition(Long personId, Long editionId) {
		SCPerson scPerson = personRepository.findByIdOrThrow(personId);
		return scPerson.getPathPreferences().stream()
				.filter(pp -> pp.getEdition().getId().equals(editionId)).findFirst();
	}

	/**
	 * Returns a SCPerson by person id. If it doesn't exist, creates one.
	 *
	 * @param  personId The id of the person
	 * @return          The SCPerson with id.
	 */
	@Transactional
	public SCPerson getOrCreateSCPerson(Long personId) {
		return personRepository.findById(personId)
				.orElseGet(() -> personRepository.save(SCPerson.builder().id(personId).build()));
	}
}
