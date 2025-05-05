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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.skills.dto.create.RegularTaskCreateDTO;
import nl.tudelft.skills.model.ChoiceTask;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChoiceTaskPatchDTO extends TaskPatchDTO<ChoiceTask> {
	private String name;
	@NotNull
	@Min(1)
	private Integer minTasks;
	@NotNull
	@Builder.Default
	private List<RegularTaskPatchDTO> updatedSubTasks = new ArrayList<>();
	@NotNull
	@Builder.Default
	private List<RegularTaskCreateDTO> newSubTasks = new ArrayList<>();

	@Override
	protected void applyOneToOne() {
		super.applyOneToOne();
		data.setName(name == null || name.isBlank() ? null : name);
		updateNonNull(minTasks, data::setMinTasks);
	}

	@Override
	protected void validate() {
		int numberSubTasks = newSubTasks.size() + updatedSubTasks.size();
		if (numberSubTasks <= 1) {
			errors.rejectValue("newSubTasks", "notEnoughSubTasks",
					"ChoiceTask has to contain at least two subtasks");
			errors.rejectValue("updatedSubTasks", "notEnoughSubTasks",
					"ChoiceTask has to contain at least two subtasks");
		}
		if (minTasks <= 0 || minTasks >= numberSubTasks) {
			errors.rejectValue("minTasks", "invalidMinTasks",
					"minTasks should be larger than zero and smaller than the number of subtasks");
		}
		if (!newSubTasks.stream().allMatch(t -> Objects.equals(t.getSkill().getId(), getSkill().getId()))) {
			errors.rejectValue("newSubTasks", "regularTaskNotInSameSkill",
					"RegularTask is not in same Skill as ChoiceTask");
		}
		if (!updatedSubTasks.stream()
				.allMatch(t -> Objects.equals(t.getSkill().getId(), getSkill().getId()))) {
			errors.rejectValue("updatedSubTasks", "regularTaskNotInSameSkill",
					"RegularTask is not in same Skill as ChoiceTask");
		}
	}
}
