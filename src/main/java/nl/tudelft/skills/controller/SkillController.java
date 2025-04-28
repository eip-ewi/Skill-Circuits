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

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.service.SkillService;

@RestController
@AllArgsConstructor
@RequestMapping("api/skill")
public class SkillController {

	private final SkillService skillService;

	@ResponseBody
	@Transactional
	@PatchMapping("{skill}/position")
	public void updatePosition(@PathEntity Skill skill, @RequestParam Integer column) {
		skill.setColumn(column);
	}

	@ResponseBody
	@PatchMapping
	public void updateSkill(@RequestBody SkillPatchDTO patch) {
		skillService.patchSkill(patch);
	}

}
