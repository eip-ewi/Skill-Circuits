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
package nl.tudelft.skills.dto.old.view.module;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.id.SkillId;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.Task;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class TaskViewDTO<D extends Task> extends View<D> {
	@NotNull
	private Long id;
	@NotNull
	private String taskType;
	@NotNull
	private String submoduleName;
	@NotNull
	private SkillId skill;
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
		taskType = data.getClass().getSimpleName();
		skillName = data.getSkill().getName();
		submoduleName = data.getSkill().getSubmodule().getName();
		pathIds = data.getPaths().stream().map(Path::getId).collect(Collectors.toSet());
	}
}
