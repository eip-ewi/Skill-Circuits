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

import java.util.HashSet;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.BadgeCreateDTO;
import nl.tudelft.skills.model.Badge;
import nl.tudelft.skills.repository.BadgeRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
class BadgeControllerTest {

	@Autowired
	private TestDatabaseLoader db;

	private BadgeController badgeController;
	private BadgeRepository badgeRepository;

	public BadgeControllerTest() {
		badgeRepository = mock(BadgeRepository.class);
		badgeController = new BadgeController(badgeRepository);
	}

	@Test
	void createBadge() {
		BadgeCreateDTO create = BadgeCreateDTO.builder().id(db.getBadge1().getId())
				.name(db.getBadge1().getName()).build();

		badgeController.createBadge(create);

		verify(badgeRepository)
				.save(Badge.builder().id(db.getBadge1().getId()).name(db.getBadge1().getName())
						.inventories(new HashSet<>()).build());
	}

}
