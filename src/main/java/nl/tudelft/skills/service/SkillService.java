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

import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

	private final AbstractSkillRepository abstractSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;

	@Autowired
	public SkillService(AbstractSkillRepository abstractSkillRepository,
			TaskCompletionRepository taskCompletionRepository) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
	}

	/**
	 * Deletes a skill.
	 *
	 * @param  id The id of the skill
	 * @return    The deleted skill
	 */
	@Transactional
	public AbstractSkill deleteSkill(Long id) {
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(id);
		skill.getChildren().forEach(c -> c.getParents().remove(skill));
		if (skill instanceof Skill s) {
			s.getTasks().forEach(t -> taskCompletionRepository.deleteAll(t.getCompletedBy()));
		}
		abstractSkillRepository.delete(skill);
		return skill;
	}

}
