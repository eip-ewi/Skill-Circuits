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
package nl.tudelft.skills.dto.old.create;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import nl.tudelft.skills.dto.id.SCEditionId;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.*;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.model.Checkpoint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CheckpointCreateDTO extends Create<Checkpoint> {

	@NotBlank
	private String name;
	@NotNull
	private SCEditionId edition;
	@NotNull
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Builder.Default
	private LocalDateTime deadline = LocalDateTime.now();

	@NotNull
	@Builder.Default
	private List<Long> skillIds = new ArrayList<>();

	@Override
	public Class<Checkpoint> clazz() {
		return Checkpoint.class;
	}

}
