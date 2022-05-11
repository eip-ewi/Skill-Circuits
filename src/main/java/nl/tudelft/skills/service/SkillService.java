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

import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillService {

	private final SkillRepository skillRepository;

	private final TaskRepository taskRepository;

	@Autowired
	public SkillService(SkillRepository skillRepository, TaskRepository taskRepository) {
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
	}

	/**
	 * Deletes a skill.
	 *
	 * @param  id The id of the skill
	 * @return    The deleted skill
	 */
	public Skill deleteSkill(Long id) {
		Skill skill = skillRepository.findByIdOrThrow(id);
		skill.getChildren().forEach(c -> {
			c.getParents().remove(skill);
			skillRepository.save(c);
		});
		skillRepository.delete(skill);
		return skill;
	}

}
