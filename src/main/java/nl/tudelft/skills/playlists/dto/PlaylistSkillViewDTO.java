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
package nl.tudelft.skills.playlists.dto;

import java.util.Comparator;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.TaskRepository;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistSkillViewDTO extends View<Skill> {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@Builder.Default
	private int totalTime = 0;

	private List<PlaylistTaskViewDTO> tasks;

	public void postApply(TaskRepository taskRepository, List<PlaylistTaskViewDTO> tasks) {
		this.tasks = tasks;
		this.totalTime = tasks.stream().map(PlaylistTaskViewDTO::getEstTime).reduce(0, Integer::sum);
		tasks.sort(Comparator.comparing(PlaylistTaskViewDTO::getIdx));
	}
}
