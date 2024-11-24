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
package nl.tudelft.skills.dto.create;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.model.Task;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = RegularTaskCreateDTO.class, name = "RegularTask"),
		@JsonSubTypes.Type(value = ChoiceTaskCreateDTO.class, name = "ChoiceTask")
})
public abstract class TaskCreateDTO<D extends Task> extends Create<D> {
	@NotNull
	private SkillIdDTO skill;
	@NotNull
	private Integer index;
}
