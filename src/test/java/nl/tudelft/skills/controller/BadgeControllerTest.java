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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.BadgeCreateDTO;
import nl.tudelft.skills.service.BadgeService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
class BadgeControllerTest extends ControllerTest {

	private BadgeController badgeController;
	private BadgeService badgeService;

	public BadgeControllerTest() {
		badgeService = mock(BadgeService.class);
		badgeController = new BadgeController(badgeService);
	}

	@Test
	void createBadge() {
		BadgeCreateDTO create = BadgeCreateDTO.builder().id(db.badge1.getId())
				.name(db.badge1.getName()).inventories(db.badge1.getInventories()).build();

		badgeController.createBadge(create);

		verify(badgeService).createBadge(db.badge1);
	}

}
