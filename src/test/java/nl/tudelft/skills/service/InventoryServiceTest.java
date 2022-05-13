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
import java.util.List;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.InventoryViewDTO;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.InventoryItem;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.BadgeRepository;
import nl.tudelft.skills.repository.InventoryRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class InventoryServiceTest {
	@Autowired
	private TestDatabaseLoader db;
	private InventoryService inventoryService;
	private InventoryRepository inventoryRepository;
	private PersonRepository personRepository;
	private BadgeRepository badgeRepository;
	private Inventory inventory;
	private SCPerson person;
	private List<InventoryItem> badgeList = new ArrayList<>();

	@Autowired
	public InventoryServiceTest(InventoryRepository inventoryRepository, PersonRepository personRepository,
			BadgeRepository badgeRepository) {
		this.inventoryRepository = inventoryRepository;
		this.inventoryService = new InventoryService(inventoryRepository, personRepository, badgeRepository);
	}

	@BeforeEach
	public void setUp() {
		badgeList.add(db.badge1);
		inventory = Inventory.builder().id(db.getInventory().getId()).inventoryItems(badgeList)
				.build();
		person = SCPerson.builder().id(1L).inventory(inventory).build();
	}

	@Test
	public void getInventory() {
		assertEquals(inventory, inventoryService.getInventory(db.getPerson().getId()));
	}

	@Test
	public void getInventoryView() {
		InventoryViewDTO inventoryDTO = InventoryViewDTO.builder().id(db.getInventory().getId())
				.inventoryItems(badgeList).build();
		assertEquals(inventoryDTO, inventoryService.getInventoryView(db.getPerson().getId()));
	}

	@Test
	public void createInventory() {
		assertEquals(inventory, inventoryService.createInventory(db.getPerson().getId()));
	}
}
