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
package nl.tudelft.skills.security;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.InventoryRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class LoginUserHandlerTest {

	@Autowired
	private TestDatabaseLoader db;

	private LoginUserHandler loginUserHandler;
	private PersonRepository personRepository;
	private InventoryRepository inventoryRepository;

	@Autowired
	public LoginUserHandlerTest() {
		this.personRepository = mock(PersonRepository.class);
		this.inventoryRepository = mock(InventoryRepository.class);
		this.loginUserHandler = new LoginUserHandler(personRepository, inventoryRepository);
	}

	@Test
	void handleUserLoginAddsSCPersonIfNotPresent() {
		Long id = 42L;
		when(personRepository.existsById(any())).thenReturn(false);

		loginUserHandler.handleUserLogin(Person.builder().id(id).build());

		SCPerson person = SCPerson.builder().id(id).build();
		Inventory inventory = Inventory.builder().person(person).inventoryItems(new ArrayList<>()).build();
		person.setInventory(inventory);
		verify(personRepository).save(person);
	}

	@Test
	void handleUserLoginDoesNotAddScPersonIfPresent() {
		Long id = 42L;
		when(personRepository.existsById(any())).thenReturn(true);

		loginUserHandler.handleUserLogin(Person.builder().id(id).build());
		verify(personRepository, never()).save(SCPerson.builder().id(id).build());
	}
}
