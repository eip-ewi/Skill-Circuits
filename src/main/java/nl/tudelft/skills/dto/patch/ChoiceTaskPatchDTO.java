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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.skills.dto.create.TaskCreateDTO;
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
	private List<TaskPatchDTO<?>> tasks = new ArrayList<>();
	@NotNull
	@Builder.Default
	private List<TaskCreateDTO<?>> newTasks = new ArrayList<>();

	// TODO: might need to add taskInfo here

	@Override
	protected void applyOneToOne() {
		data.setName(name == null || name.isBlank() ? null : name);
		updateNonNull(minTasks, data::setMinTasks);
	}

	@Override
	protected void validate() {
		Set<Long> taskIds = data.getTasks().stream().map(taskInfo -> taskInfo.getTask().getId())
				.collect(Collectors.toSet());
		if (!newTasks.stream().allMatch(t -> Objects.equals(t.getSkill().getId(), getSkill().getId()))) {
			errors.rejectValue("newTasks", "regularTaskNotInSameSkill",
					"RegularTask is not in same Skill as ChoiceTask");
		}
		if (!taskIds.containsAll(tasks.stream().map(TaskPatchDTO::getId).collect(Collectors.toSet()))) {
			errors.rejectValue("tasks", "regularTaskNotInChoiceTask", "RegularTask is not in ChoiceTask");
		}
	}
}
