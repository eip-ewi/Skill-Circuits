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
package nl.tudelft.skills.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.skills.dto.view.module.RegularTaskViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegularTask extends Task {
	@NotNull
	@OneToOne(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private TaskInfo taskInfo;

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "task")
	private Set<TaskCompletion> completedBy = new HashSet<>();

	@Override
	public Class<? extends TaskViewDTO<?>> viewClass() {
		return RegularTaskViewDTO.class;
	}

	public String getName() {
		return taskInfo.getName();
	}

	public void setName(String name) {
		taskInfo.setName(name);
	}

	public TaskType getType() {
		return taskInfo.getType();
	}

	public void setType(TaskType type) {
		taskInfo.setType(type);
	}

	public Integer getTime() {
		return taskInfo.getTime();
	}

	public void setTime(Integer time) {
		taskInfo.setTime(time);
	}

	public String getLink() {
		return taskInfo.getLink();
	}

	public void setLink(String link) {
		taskInfo.setLink(link);
	}

}
