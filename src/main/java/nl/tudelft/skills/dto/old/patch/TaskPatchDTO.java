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
package nl.tudelft.skills.dto.old.patch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.Task;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskPatchDTO extends Patch<Task> {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@NotNull
	@Min(0)
	private Integer time;
	@NotNull
	private TaskType type;
	private String link;
	@NotNull
	private Integer index;

	@Override
	protected void applyOneToOne() {
		//		updateNonNull(name, data::setName);
		//		updateNonNull(time, data::setTime);
		//		updateNonNull(type, data::setType);
		//		data.setLink(link == null || link.isBlank() ? null : link);
	}

	@Override
	protected void validate() {
	}
}
