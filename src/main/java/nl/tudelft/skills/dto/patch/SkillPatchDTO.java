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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.create.TaskCreateDTO;
import nl.tudelft.skills.dto.id.SubmoduleIdDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SkillPatchDTO extends Patch<Skill> {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@Builder.Default
	private Boolean essential = false;
	private SubmoduleIdDTO submodule;
	@NotNull
	@Builder.Default
	private List<TaskPatchDTO> items = new ArrayList<>();
	@NotNull
	@Builder.Default
	private List<TaskCreateDTO> newItems = new ArrayList<>();
	@NotNull
	@Builder.Default
	private Set<Long> removedItems = new HashSet<>();

	@Override
	protected void applyOneToOne() {
		updateNonNull(name, data::setName);
		updateNonNull(essential, data::setEssential);
		updateNonNullId(submodule, data::setSubmodule);
	}

	@Override
	protected void applyOneToMany() {
		Map<Long, Task> tasks = data.getTasks().stream()
				.collect(Collectors.toMap(Task::getId, Function.identity()));
		items.forEach(p -> p.apply(tasks.get(p.getId())));

		TaskRepository taskRepository = SpringContext.getBean(TaskRepository.class);
		data.getTasks().addAll(
				newItems.stream().map(c -> taskRepository.save(c.apply())).collect(Collectors.toSet()));

		data.getTasks().stream().filter(t -> removedItems.contains(t.getId())).toList()
				.forEach(t -> data.getTasks().remove(t));
	}

	@Override
	protected void validate() {
		Set<Long> taskIds = data.getTasks().stream().map(Task::getId).collect(Collectors.toSet());
		if (!newItems.stream().allMatch(t -> Objects.equals(t.getSkill().getId(), id))) {
			errors.rejectValue("newItems", "itemNotInSkill", "Item is not in skill");
		}
		if (!taskIds.containsAll(items.stream().map(TaskPatchDTO::getId).collect(Collectors.toSet()))) {
			errors.rejectValue("items", "itemNotInSkill", "Item is not in skill");
		}
		if (!taskIds.containsAll(removedItems)) {
			errors.rejectValue("removedItems", "itemNotInSkill", "Item is not in skill");
		}
	}
}
