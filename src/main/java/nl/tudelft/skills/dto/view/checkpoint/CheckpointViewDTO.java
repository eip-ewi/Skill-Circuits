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
package nl.tudelft.skills.dto.view.checkpoint;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelEditionViewDTO;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Skill;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CheckpointViewDTO extends View<Checkpoint> {

	@NotNull
	private long id;

	@NotNull
	private ModuleLevelEditionViewDTO edition;

	@NotBlank
	private String name;

	@NotNull
	private LocalDateTime deadline;

	@NotNull
	private List<Long> skillIds;

	@Override
	public void postApply() {
		super.postApply();
		this.skillIds = data.getSkills().stream().map(Skill::getId).toList();
	}
}
