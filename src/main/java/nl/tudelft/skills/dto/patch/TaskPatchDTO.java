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
package nl.tudelft.skills.dto.patch;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.model.Task;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = RegularTaskPatchDTO.class, name = "RegularTask"),
		@JsonSubTypes.Type(value = ChoiceTaskPatchDTO.class, name = "ChoiceTask")
})
public abstract class TaskPatchDTO<D extends Task> extends Patch<D> {
	@NotNull
	private Long id;
	@NotNull
	private Integer index;
	@NotNull
	private SkillIdDTO skill;

	@Override
	protected void applyOneToOne() {
		updateNonNull(index, data::setIdx);
	}
}
