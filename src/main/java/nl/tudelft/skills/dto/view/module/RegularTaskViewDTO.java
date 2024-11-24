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
package nl.tudelft.skills.dto.view.module;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.model.TaskType;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegularTaskViewDTO extends TaskViewDTO<RegularTask> {
	// TODO SuperBuilder?
	@NotBlank
	private String name;
	@NotNull
	private TaskType type;
	@Min(0)
	private Integer time;
	private String link;
	@Builder.Default
	private boolean completed = false;

	@NotNull
	private Integer completedCount;

	@Override
	public void postApply() {
		super.postApply();
		completedCount = data.getCompletedBy().size();
	}
}
