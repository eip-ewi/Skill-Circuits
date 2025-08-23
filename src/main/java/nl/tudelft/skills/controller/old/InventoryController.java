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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.PersonRepository;

@RequestMapping("inventory")
public class InventoryController {
	private final PersonRepository personRepository;

	@Autowired
	public InventoryController(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	/**
	 * Gets the page of the currently authenticated user's inventory.
	 *
	 * @param  model the model to add data to
	 * @return       the page to load
	 */
	@GetMapping
	public String getInventoryPage(@AuthenticatedPerson Person person, Model model) {
		SCPerson scPerson = personRepository.findByIdOrThrow(person.getId());
		//		model.addAttribute("inventory", View.convert(scPerson.getInventory(),
		//				InventoryViewDTO.class));
		return "inventory/view";
	}
}
