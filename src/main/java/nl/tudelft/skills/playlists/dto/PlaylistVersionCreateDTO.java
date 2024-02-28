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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import nl.tudelft.skills.playlists.model.PlaylistVersion;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistVersionCreateDTO extends Create<PlaylistVersion> {

	@NotNull
	private List<PlaylistTaskCreateDTO> taskCreates;

	@NotNull
	@Builder.Default
	private Set<PlaylistTask> tasks =  new HashSet<>();

	@NotNull
	@Builder.Default
	private Integer estimatedTime = 0;

	@Override
	public Class<PlaylistVersion> clazz() {
		return PlaylistVersion.class;
	}
}
