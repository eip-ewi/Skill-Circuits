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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.patch.InventoryPatchDTO;
import nl.tudelft.skills.dto.view.InventoryViewDTO;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.InventoryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class InventoryControllerTest extends ControllerTest {
	private final InventoryController inventoryController;

	private final InventoryService inventoryService;
	private final AuthorisationService authorisationService;

	@Autowired
	public InventoryControllerTest() {
		inventoryService = mock(InventoryService.class);
		authorisationService = mock(AuthorisationService.class);
		inventoryController = new InventoryController(inventoryService, authorisationService);
	}

	@Test
	void getInventoryPage() {
		InventoryViewDTO mockInventoryView = View.convert(db.getInventory(), InventoryViewDTO.class);

		mockInventoryView.setId(1L);
		mockInventoryView.setInventoryItems(new ArrayList<>());

		when(inventoryService.getInventoryView(any(SCPerson.class))).thenReturn(mockInventoryView);
		when(authorisationService.getAuthSCPerson()).thenReturn(db.getPerson());

		inventoryController.getInventoryPage(model);

		assertThat(model.getAttribute("inventory")).isEqualTo(mockInventoryView);

		verify(inventoryService).getInventoryView(db.getPerson());
	}

	@Test
	void getInventoryPageNotAuthenticated() throws Exception {
		when(authorisationService.isAuthenticated()).thenReturn(false);

		mvc.perform(get("/inventory")).andExpect(status().is3xxRedirection());
	}

	@Test
	@WithUserDetails("username")
	void patchInventoryPage() throws Exception {
		Inventory toPatch = db.getInventory();
		Inventory patch = Inventory.builder().id(toPatch.getId() + 5).person(db.getPerson())
				.inventoryItems(toPatch.getInventoryItems()).build();
		InventoryPatchDTO patchDto = InventoryPatchDTO.builder().id(toPatch.getId() + 5)
				.person(db.getPerson()).inventoryItems(toPatch.getInventoryItems()).build();

		System.out.println(db.getInventory().toString());
		assertThat(patch.getPerson()).isNotNull();

		when(inventoryService.patchInventory(patch)).thenReturn(patch);

		mvc.perform(patch("/inventory").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.content("id=" + toPatch.getId() + 5 + "&person=" + db.getPerson().getId()).with(csrf()))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	void patchInventoryNotAuthenticated() throws Exception {
		when(authorisationService.isAuthenticated()).thenReturn(false);

		mvc.perform(patch("/inventory").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.content("id=" + db.getInventory().getId() + 5 + "&person=" + db.getPerson().getId())
				.with(csrf())).andExpect(status().is3xxRedirection());
	}
}
