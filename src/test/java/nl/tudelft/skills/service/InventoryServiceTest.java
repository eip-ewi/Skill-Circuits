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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.InventoryViewDTO;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.InventoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class InventoryServiceTest {
	private InventoryService inventoryService;
	private InventoryRepository inventoryRepository;
	private Inventory inventory;
	private SCPerson person;

	private final Long INVENTORY_ID = 32L;

	@Autowired
	public InventoryServiceTest(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
		this.inventoryService = new InventoryService(inventoryRepository);
	}

	@BeforeEach
	public void setUp() {
		inventory = Inventory.builder().id(INVENTORY_ID).inventoryItems(new ArrayList<>()).build();
		person = SCPerson.builder().id(1L).inventory(inventory).build();
	}

	@Test
	public void getInventory() {
		assertEquals(inventory, inventoryService.getInventory(person));
	}

	@Test
	public void getInventoryView() {
		InventoryViewDTO inventoryDTO = InventoryViewDTO.builder().id(INVENTORY_ID)
				.inventoryItems(new ArrayList<>()).build();
		assertEquals(inventoryDTO, inventoryService.getInventoryView(person));
	}

	@Test
	public void createInventory() {
		assertEquals(inventory, inventoryService.createInventory(person));
	}

	@Test
	public void patchInventory() {
		Inventory patchedInventory = inventoryRepository.save(inventory);
		patchedInventory.setPerson(person);
		assertEquals(patchedInventory, inventoryService.patchInventory(patchedInventory));
	}
}
