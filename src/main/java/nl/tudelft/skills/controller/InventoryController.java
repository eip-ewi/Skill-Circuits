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

import nl.tudelft.skills.dto.patch.InventoryPatchDTO;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
		SCPerson person = authorisationService.getAuthSCPerson();
		model.addAttribute("inventory", inventoryService.getInventoryView(person));
		return "inventory/view";
	}

	/**
	 * Patches an inventory.
	 *
	 * @param  patch the patch to make to the inventory
	 * @return       an ok response if patch happened successfully
	 */
	@PatchMapping
	@PreAuthorize("@authorisationService.isAuthenticated()")
	ResponseEntity<Void> patchInventory(InventoryPatchDTO patch) {
		Inventory inventory = inventoryService.getInventory(patch.getPerson());
		inventoryService.patchInventory(patch.apply(inventory));
		return ResponseEntity.ok().build();
	}
}
