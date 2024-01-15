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

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PersonServiceTest {

	private PersonRepository personRepository;

	private PersonService personService;

	private TestDatabaseLoader db;

	@Autowired
	public PersonServiceTest(PersonRepository personRepository, TestDatabaseLoader db) {
		this.personRepository = personRepository;
		this.db = db;
		personService = new PersonService(personRepository);
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
	public void testAddRevealedSkill() {
		SCPerson person = db.getPerson();
		Skill skill = db.getSkillNegation();

		assertThat(person.getSkillsRevealed()).doesNotContain(skill);
		personService.addRevealedSkill(person.getId(), skill);

		assertThat(person.getSkillsRevealed()).contains(skill);
	}

}
