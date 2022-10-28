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
package nl.tudelft.skills.dto.create;

import java.util.HashSet;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.dto.id.CheckpointIdDTO;
import nl.tudelft.skills.dto.id.SubmoduleIdDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SkillCreateDTO extends Create<Skill> {

	@NotBlank
	private String name;
	@Builder.Default
	private Boolean essential = false;
	@Builder.Default
	private Boolean hidden = false;
	@NotNull
	private SubmoduleIdDTO submodule;
	private CheckpointIdDTO checkpoint;
	private CheckpointCreateDTO checkpointCreate;
	@Min(0)
	@NotNull
	private Integer row;
	@Min(0)
	@NotNull
	private Integer column;
	@NotNull
	private List<TaskCreateDTO> newItems;
	@NotNull
	private List<Long> requiredTaskIds;

	@Override
	public Class<Skill> clazz() {
		return Skill.class;
	}

	@Override
	protected void postApply(Skill data) {
		if (hidden) {
			TaskRepository taskRepository = SpringContext.getBean(TaskRepository.class);
			List<Task> requiredTasks = taskRepository.findAllByIdIn(requiredTaskIds);
			requiredTasks.forEach(t -> t.getRequiredFor().add(data));
			data.setRequiredTasks(new HashSet<>(requiredTasks));
		}
	}

	@Override
	public void validate() {
		if (checkpoint == null && checkpointCreate == null) {
			errors.rejectValue("checkpoint", "noCheckpointForSkill",
					"Skill needs either a new checkpoint or a checkpointId");
		}
	}
}
