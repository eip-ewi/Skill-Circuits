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

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.SkillRepository;

@Service
@AllArgsConstructor
public class SkillService {

	private final SkillRepository skillRepository;

	private final TaskService taskService;

	@Transactional
	public void patchSkill(SkillPatchDTO patch) {
		Skill skill = skillRepository.findByIdOrThrow(patch.getId());
		patch.apply(skill);
		skillRepository.save(skill);

		for (TaskPatchDTO taskPatch : patch.getTasks()) {
			taskService.patchTask(taskPatch);
		}
	}

}
