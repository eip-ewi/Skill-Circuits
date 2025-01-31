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
package nl.tudelft.skills.dto.create;

import java.util.*;

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
public class ChoiceTaskCreateDTO extends TaskCreateDTO<ChoiceTask> {
	private String name;
	@NotNull
	@Min(1)
	private Integer minTasks;
	@NotNull
	@Builder.Default
	private List<RegularTaskCreateDTO> tasks = new ArrayList<>();

	// TODO: might need to add TaskInfo here

	@Override
	protected void postApply(ChoiceTask data) {
		if (name != null && name.isBlank()) {
			data.setName(null);
		}
	}

	@Override
	public Class<ChoiceTask> clazz() {
		return ChoiceTask.class;
	}

	@Override
	public void validate() {
		// Validate RegularTasks and this ChoiceTask to have same Skill
		if (!tasks.stream().allMatch(t -> Objects.equals(t.getSkill().getId(), getSkill().getId()))) {
			errors.rejectValue("tasks", "skillOfTasksNotMatching",
					"RegularTask is not in same Skill as ChoiceTask");
		}
	}
}
