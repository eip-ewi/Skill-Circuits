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
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.playlists.model.PlaylistVersion;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistVersionPatchDTO extends Patch<PlaylistVersion> {

	@NotNull
	private Long id;

	@NotNull
	@Min(0)
	private Integer elapsedTime;

	@NotNull
	@Builder.Default
	private List<PlaylistTaskPatchDTO> taskTimes = new ArrayList<>();

	@Override
	protected void applyOneToOne() {
		updateNonNull(elapsedTime, data::setElapsedTime);
	}

	@Override
	protected void postApply() {
		data.setElapsedTimeUpdated(LocalDateTime.now());
	}

	@Override
	protected void validate() {
	}

}
