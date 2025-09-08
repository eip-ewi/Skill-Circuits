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
package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.CheckpointId;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Skill;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SkillPatch extends Patch<AbstractSkill> {

	private String name;

	private SubmoduleId submodule;

	private CheckpointId checkpoint;

	private Boolean essential;

	private Boolean hidden;

	@Override
	protected void applyOneToOne() {
		if (data instanceof Skill skill) {
			updateNonNull(name, skill::setName);
			updateNonNullId(submodule, skill::setSubmodule);
			if (checkpoint != null) {
				if (checkpoint.getId() == null) {
					skill.setCheckpoint(null);
				} else {
					updateNonNullId(checkpoint, skill::setCheckpoint);
				}
			}
			updateNonNull(hidden, skill::setHidden);
		}
		updateNonNull(essential, data::setEssential);
	}

	@Override
	protected void validate() {

	}
}
