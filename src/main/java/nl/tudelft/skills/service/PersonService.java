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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
@Transactional
@AllArgsConstructor
public class PersonService {

	private final PersonRepository personRepository;

	/**
	 * Returns a SCPerson by person id. If it doesn't exist, creates one.
	 *
	 * @param  personId The id of the person
	 * @return          The SCPerson with id.
	 */
	@Transactional
	public SCPerson getSCPerson(Long personId) {
		return personRepository.findById(personId)
				.orElseGet(() -> personRepository.save(SCPerson.builder().id(personId).build()));
	}

}
