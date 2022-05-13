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

import nl.tudelft.skills.dto.create.BadgeCreateDTO;
import nl.tudelft.skills.service.BadgeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("item/badge")
public class BadgeController {
	public BadgeService badgeService;

	@Autowired
	public BadgeController(BadgeService badgeService) {
		this.badgeService = badgeService;
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Void> createBadge(@RequestBody BadgeCreateDTO create) {
		badgeService.createBadge(create.apply());
		return ResponseEntity.ok().build();
	}

}
