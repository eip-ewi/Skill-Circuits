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

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.ItemView;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskViewDTO extends View<Task> implements ItemView {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@NotNull
	private TaskType type;
	@Min(0)
	private Integer time;
	private String link;
	@Builder.Default
	private boolean completed = false;

	private Integer completedCount;

	@NotNull
	private String submoduleName;
	@NotNull
	private String skillName;
	@NotNull
	private Set<Long> pathIds;

	/**
	 * Visibility of a task is set to true if it is part of the path currently displayed.
	 */
	@Builder.Default
	@NotNull
	@EqualsAndHashCode.Exclude
	private Boolean visible = true;

	@Override
	public void postApply() {
		super.postApply();
		completedCount = data.getCompletedBy().size();
		skillName = data.getSkill().getName();
		submoduleName = data.getSkill().getSubmodule().getName();
		pathIds = data.getPaths().stream().map(Path::getId).collect(Collectors.toSet());
	}
}
