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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.UserVersion;
import nl.tudelft.skills.repository.UserVersionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
class UserVersionControllerTest extends ControllerTest {

	private final UserVersionRepository userVersionRepository;
	private final PersonRepository scPersonRepository;
	private final BuildProperties buildProperties;
	private final UserVersionController userVersionController;

	@Autowired
	public UserVersionControllerTest(UserVersionRepository userVersionRepository,
			PersonRepository scPersonRepository) {
		this.userVersionRepository = userVersionRepository;
		this.scPersonRepository = scPersonRepository;
		this.buildProperties = mock(BuildProperties.class);
		this.userVersionController = new UserVersionController(userVersionRepository, scPersonRepository,
				this.buildProperties);
	}

	@Test
	public void versionUpdateNotSavedTest() {
		when(buildProperties.getVersion()).thenReturn("1.0.0");
		Person person = new Person();
		person.setId(db.getPerson().getId());
		userVersionController.versionUpdate(person);
		assertEquals("1.0.0", userVersionRepository.findByPersonId(person.getId()).get().getVersion());
	}

	@Test
	public void versionUpdateSavedTest() {
		when(buildProperties.getVersion()).thenReturn("2.0.0");
		Person person = new Person();
		person.setId(db.getPerson().getId());
		userVersionRepository.save(UserVersion.builder()
				.person(scPersonRepository.findByIdOrThrow(person.getId())).version("1.1.0").build());
		userVersionController.versionUpdate(person);
		assertEquals("2.0.0", userVersionRepository.findByPersonId(person.getId()).get().getVersion());
	}
}
