/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.create.ExternalSkillCreate;
import nl.tudelft.skills.dto.create.SkillCreate;
import nl.tudelft.skills.dto.patch.SkillPatch;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.ExternalSkillRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.bookmark.HiddenSkillBookmarkListRepository;

@Service
@AllArgsConstructor
public class SkillService {

	private final AbstractSkillRepository abstractSkillRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final HiddenSkillBookmarkListRepository hiddenSkillBookmarkListRepository;
	private final SkillRepository skillRepository;

	private final DTOConverter dtoConverter;

	@Transactional
	public Skill createSkill(SkillCreate create) {
		return skillRepository.save(create.apply(dtoConverter));
	}

	@Transactional
	public ExternalSkill createSkill(ExternalSkillCreate create) {
		return externalSkillRepository.save(create.apply(dtoConverter));
	}

	@Transactional
	public void patchSkill(AbstractSkill abstractSkill, SkillPatch patch) {
		patch.apply(abstractSkill, dtoConverter);
		abstractSkillRepository.save(abstractSkill);

		if (abstractSkill instanceof Skill skill && patch.getHidden() != null) {
			if (patch.getHidden()) {
				HiddenSkillBookmarkList list = HiddenSkillBookmarkList.builder().skill(skill).build();
				hiddenSkillBookmarkListRepository.save(list);
				skill.setRequirements(list);
			} else {
				hiddenSkillBookmarkListRepository.deleteBySkill(skill);
				skill.setRequirements(null);
			}
			skillRepository.save(skill);
		}
	}

	@Transactional
	public void deleteSkill(AbstractSkill skill) {
		abstractSkillRepository.delete(skill);
	}

	@Transactional
	public void updatePosition(AbstractSkill skill, Integer column) {
		skill.setColumn(column);
		abstractSkillRepository.save(skill);
	}

	@Transactional
	public void connect(AbstractSkill from, AbstractSkill to) {
		to.getParents().add(from);
		abstractSkillRepository.save(to);
	}

	@Transactional
	public void disconnect(AbstractSkill from, AbstractSkill to) {
		to.getParents().remove(from);
		abstractSkillRepository.save(to);
	}

}
