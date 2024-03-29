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

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.playlists.model.Playlist;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistViewDTO extends View<Playlist> {

	@NotNull
	private Long id;
	@NotNull
	private LocalDate created;

	@NotNull
	private int estTime;

	@Builder.Default
	private int timePlayed = 0;

	@Builder.Default
	private boolean completed = false;

	@NotNull
	private List<PlaylistTaskViewDTO> tasks;

}
