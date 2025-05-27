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

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.model.*;

@Service
@Transactional
@AllArgsConstructor
public class ModuleViewService {

	public List<CheckpointViewDTO> getCheckpointViews(SCModule module) {
		return module.getEdition().getCheckpoints().stream()
				.filter(checkpoint -> checkpoint.getSkills().stream()
						.anyMatch(s -> Objects.equals(s.getSubmodule().getModule().getId(), module.getId())))
				.map(checkpoint -> new CheckpointViewDTO(
						checkpoint.getId(),
						checkpoint.getName(),
						checkpoint.getDeadline(),
						checkpoint.getSkills().stream().map(AbstractSkill::getId).toList()))
				.toList();
	}

	public ModuleLevelModuleViewDTO getCircuitView(SCModule module, SCPerson person) {
		return new ModuleLevelModuleViewDTO(
				module.getId(),
				module.getName(),
				module.getSubmodules().stream().map(s -> getGroupView(s, person)).toList());
	}

	public ModuleLevelSubmoduleViewDTO getGroupView(Submodule submodule, SCPerson person) {
		return new ModuleLevelSubmoduleViewDTO(
				submodule.getId(),
				submodule.getName(),
				submodule.getSkills().stream().map(s -> getBlockView(s, person)).toList());
	}

	public ModuleLevelSkillViewDTO getBlockView(Skill skill, SCPerson person) {
		return new ModuleLevelSkillViewDTO(
				skill.getId(),
				skill.getName(),
				skill.getColumn(),
				skill.getParents().stream().map(AbstractSkill::getId).toList(),
				skill.getChildren().stream().map(AbstractSkill::getId).toList(),
				skill.getTasks().stream().map(t -> getItemView(t, person)).toList(),
				skill.isEssential());
	}

	public ModuleLevelTaskViewDTO getItemView(Task task, SCPerson person) {
		return new ModuleLevelTaskViewDTO(
				task.getId(),
				task.getName(),
				task.getType().getIcon(),
				task.getTime(),
				task.getCompletedBy().stream()
						.anyMatch(c -> Objects.equals(c.getPerson().getId(), person.getId())));
	}

}
