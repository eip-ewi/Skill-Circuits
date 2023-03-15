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
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.ItemView;
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

	/**
	 * Visibility of a task is set to true if it is part of the path currently displayed.
	 */
	@Builder.Default
	@NotNull
	private Boolean visible = true;

	@Override
	public void postApply() {
		super.postApply();
		// TODO modify to (also?) use TaskCompletion
		completedCount = data.getPersons().size();
	}
}
