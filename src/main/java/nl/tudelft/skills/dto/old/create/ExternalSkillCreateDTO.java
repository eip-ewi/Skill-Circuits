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
package nl.tudelft.skills.dto.old.create;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.dto.id.SCModuleId;
import nl.tudelft.skills.dto.id.SkillId;
import nl.tudelft.skills.model.ExternalSkill;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalSkillCreateDTO extends Create<ExternalSkill> {

	@NotNull
	private SCModuleId module;
	@NotNull
	private SkillId skill;
	@NotNull
	private Integer row;
	@NotNull
	private Integer column;

	@Override
	public Class<ExternalSkill> clazz() {
		return ExternalSkill.class;
	}
}
