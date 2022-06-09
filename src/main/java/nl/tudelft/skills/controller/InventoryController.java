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

import javax.transaction.Transactional;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("inventory")
public class InventoryController {
	public InventoryService inventoryService;

	public AuthorisationService authorisationService;

	@Autowired
	public InventoryController(InventoryService inventoryService, AuthorisationService authorisationService) {
		this.inventoryService = inventoryService;
		this.authorisationService = authorisationService;
	}

	/**
	 * Gets the page of the currently authenticated user's inventory.
	 *
	 * @param  model the model to add data to
	 * @return       the page to load
	 */
	@GetMapping
	@Transactional
	@PreAuthorize("@authorisationService.isAuthenticated()")
	public String getInventoryPage(Model model) {
		Person person = authorisationService.getAuthPerson();
		model.addAttribute("inventory", inventoryService.getInventoryView(person.getId()));
		return "inventory/view";
	}
}
