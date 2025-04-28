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
package nl.tudelft.skills.controller.old;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.old.view.InventoryViewDTO;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class InventoryControllerTest extends ControllerTest {
	private final InventoryController inventoryController;
	private final PersonRepository personRepository;

	@Autowired
	public InventoryControllerTest() {
		personRepository = mock(PersonRepository.class);
		inventoryController = new InventoryController(personRepository);
	}

	@Test
	void getInventoryPage() {
		InventoryViewDTO inventoryViewDTO = InventoryViewDTO.builder().id(db.getInventory().getId())
				.inventoryItems(new ArrayList<>()).build();

		when(personRepository.findByIdOrThrow(anyLong())).thenReturn(db.getPerson());

		inventoryController.getInventoryPage(Person.builder().id(db.getPerson().getId()).build(), model);

		assertThat(model.getAttribute("inventory")).isEqualTo(inventoryViewDTO);
	}

}
