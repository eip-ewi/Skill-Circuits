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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.model.TaskType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskInfoPatchDTO extends Patch<TaskInfo> {
	@NotBlank
	private String name;
	@NotNull
	@Min(0)
	private Integer time;
	@NotNull
	private TaskType type;
	private String link;

	@Override
	protected void applyOneToOne() {
		updateNonNull(name, data::setName);
		updateNonNull(time, data::setTime);
		updateNonNull(type, data::setType);
		data.setLink(link == null || link.isBlank() ? null : link);
	}

	@Override
	protected void validate() {
	}
}
