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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.playlists.model.PlaylistTask;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTaskPatchDTO extends Patch<PlaylistTask> {

	private Long id;

	@NotNull
	private Long taskId;

	@NotNull
	@Min(0)
	private Integer completionTime;

	@Override
	protected void applyOneToOne() {
		updateNonNull(completionTime, data::setCompletionTime);
	}

	protected void postApply() {
		if (data.getStarted() == null) {
			data.setStarted(LocalDateTime.now());
		}
	}

	@Override
	protected void validate() {
	}
}
