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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import nl.tudelft.skills.playlists.model.PlaylistVersion;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistVersionViewDTO extends View<PlaylistVersion> {

	@NotNull
	private Integer elapsedTime;

	@NotNull
	private LocalDateTime elapsedTimeUpdated;

	@NotNull
	private Map<Long, Integer> taskTimes;

	@Override
	public void postApply() {
		this.taskTimes = data.getTasks().stream()
				.collect(Collectors.toMap(PlaylistTask::getTaskId, PlaylistTask::getCompletionTime));
	}
}
