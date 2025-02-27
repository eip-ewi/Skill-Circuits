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
package nl.tudelft.skills.dto.view.module;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.skills.model.ChoiceTask;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChoiceTaskViewDTO extends TaskViewDTO<ChoiceTask> {
	private String name;

	@NotNull
	@Min(1)
	private Integer minTasks;

	@NotNull
	@PostApply
	@EqualsAndHashCode.Exclude
	private List<RegularTaskViewDTO> tasks;

	@NotNull
	private Integer completedTasks;

	// TODO: completion handling needs to be considered more concretely in the follow-up MR
}
