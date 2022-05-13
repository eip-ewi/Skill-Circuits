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

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.InventoryViewDTO;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.InventoryItem;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.BadgeRepository;
import nl.tudelft.skills.repository.InventoryRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
	public InventoryRepository inventoryRepository;
	public PersonRepository personRepository;
	public BadgeRepository badgeRepository;

	@Autowired
	public InventoryService(InventoryRepository inventoryRepository, PersonRepository personRepository,
			BadgeRepository badgeRepository) {
		this.inventoryRepository = inventoryRepository;
		this.personRepository = personRepository;
		this.badgeRepository = badgeRepository;
	}

	/**
	 * Converts the inventory entity of a user to an inventory view
	 *
	 * @param  personId the id of the person whose inventory is needed
	 * @return
	 */
	public InventoryViewDTO getInventoryView(Long personId) {
		return View.convert(getInventory(personId),
				InventoryViewDTO.class);
	}

	/**
	 * Gets the inventory of a person.
	 *
	 * @param  personId the id of the person whose inventory must be retrieved
	 * @return          the inventory
	 */
	public Inventory getInventory(Long personId) {
		Inventory inventory = personRepository.findByIdOrThrow(personId).getInventory();

		if (inventory == null) {
			inventory = createInventory(personId);
		}

		return inventory;
	}

	/**
	 * Create an inventory for the person. This only happens if the inventory does not already exist.
	 *
	 * @param  personId the id of the person for which the inventory is created.
	 * @return          the newly created inventory.
	 */
	public Inventory createInventory(Long personId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);

		List<InventoryItem> badgeList = new ArrayList<>();
		badgeList.add(badgeRepository.findAll().get(0));
		Inventory inventory = Inventory.builder().inventoryItems(badgeList).build();

		inventoryRepository.save(inventory);
		person.setInventory(inventory);
		return person.getInventory();
	}
}
