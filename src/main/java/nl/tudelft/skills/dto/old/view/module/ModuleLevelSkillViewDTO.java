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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.BlockView;
import nl.tudelft.skills.dto.view.ItemView;
import nl.tudelft.skills.dto.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.model.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleLevelSkillViewDTO extends View<Skill> implements BlockView {

	@NotNull
	private Long id;
	@NotNull
	private String name;
	@NotNull
	private Boolean essential;
	@NotNull
	private Boolean hidden;
	@NotNull
	private Integer row;
	@NotNull
	private Integer column;
	@NotNull
	@EqualsAndHashCode.Exclude
	private CheckpointViewDTO checkpoint;

	@EqualsAndHashCode.Exclude
	private List<? extends TaskViewDTO<?>> tasks;

	@NotNull
	private List<Long> parentIds;
	@NotNull
	private List<Long> childIds;
	@NotNull
	private List<Long> requiredTaskIds;
	@NotNull
	@Builder.Default
	@EqualsAndHashCode.Exclude
	private Boolean completedRequiredTasks = false;

	@Override
	public void postApply() {
		super.postApply();
		this.parentIds = data.getParents().stream().map(AbstractSkill::getId).toList();
		this.childIds = data.getChildren().stream().map(AbstractSkill::getId).toList();
		this.requiredTaskIds = data.getRequiredTasks().stream().map(Task::getId).toList();
		this.tasks = initializeTasks();
	}

	public List<? extends TaskViewDTO<?>> initializeTasks() {
		List<RegularTask> redundantRegularTasks = data.getTasks().stream().flatMap(t -> {
			if (t instanceof ChoiceTask choiceTask) {
				return choiceTask.getTasks().stream().map(TaskInfo::getTask);
			}
			return Stream.empty();
		}).toList();

		// Filter out regular tasks that are part of a choice task
		// This prevents duplication of tasks
		return data.getTasks().stream().filter(
				t -> !(t instanceof RegularTask regularTask && redundantRegularTasks.contains(regularTask)))
				.map(t -> {
					TaskViewDTO<?> dto = getMapper().map(t, t.viewClass());
					dto.postApply();
					return dto;
				}).toList();
	}

	@Override
	public List<? extends ItemView> getItems() {
		return tasks;
	}

	public List<Long> getChildIds() {
		return childIds;
	}

	public static ModuleLevelSkillViewDTO empty() {
		return ModuleLevelSkillViewDTO.builder().id(-1L).name("").essential(true).hidden(false).row(-1)
				.column(-1)
				.checkpoint(CheckpointViewDTO.empty()).tasks(new ArrayList<>())
				.parentIds(new ArrayList<>()).childIds(new ArrayList<>())
				.requiredTaskIds(new ArrayList<>()).build();
	}
}
