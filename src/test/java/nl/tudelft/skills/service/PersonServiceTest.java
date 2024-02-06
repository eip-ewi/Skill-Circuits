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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PersonServiceTest {

	private final PersonRepository personRepository;
	private final PathRepository pathRepository;
	private final EditionRepository editionRepository;

	private final PersonService personService;
	private final EditionService editionService;

	private final TestDatabaseLoader db;

	@Autowired
	public PersonServiceTest(PersonRepository personRepository, PathRepository pathRepository,
			EditionRepository editionRepository, EditionService editionService,
			TestDatabaseLoader db) {
		this.personRepository = personRepository;
		this.pathRepository = pathRepository;
		this.editionRepository = editionRepository;
		this.editionService = editionService;
		this.db = db;

		personService = new PersonService(personRepository, pathRepository, editionService);
	}

	@Test
	public void getOrCreateSCPerson() {
		Long personId = 1L;
		assertThat(personRepository.findById(personId)).isNotPresent();

		SCPerson person = personService.getOrCreateSCPerson(personId);
		assertThat(personRepository.findById(personId)).isPresent();
	}

	@Test
	public void getPathForEdition() {
		Long editionId = 1L;
		SCPerson person = SCPerson.builder().pathPreferences(Collections.emptySet()).id(2L).build();
		person = personRepository.save(person);

		assertThat(personService.getPathForEdition(person.getId(), editionId)).isNotPresent();

	}

	@Test
	void getDefaultOrPreferredPathNull() {
		Path path = personService.getDefaultOrPreferredPath(db.getPerson().getId(),
				db.getEditionRL().getId());

		// displayed path is null if there is no default and no path preference set
		assertNull(path);
	}

	@Test
	void getDefaultOrPreferredPathDefault() {
		// Set default path
		SCEdition edition = db.getEditionRL();
		edition.setDefaultPath(db.getPathFinderPath());
		editionRepository.save(edition);

		Path path = personService.getDefaultOrPreferredPath(db.getPerson().getId(),
				db.getEditionRL().getId());

		// displayed path is default path of the course if there is no path preference set
		assertEquals(db.getPathFinderPath(), path);
	}

}
