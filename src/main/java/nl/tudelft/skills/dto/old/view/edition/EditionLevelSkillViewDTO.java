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
package nl.tudelft.skills.dto.old.view.edition;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.old.view.module.TaskViewDTO;
import nl.tudelft.skills.model.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EditionLevelSkillViewDTO extends View<Skill> {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@NotNull
	private Boolean hidden;
	@NotNull
	private List<? extends TaskViewDTO<?>> tasks;
	//
	//	@Override
	//	public void postApply() {
	//		super.postApply();
	//		this.tasks = data.getTasks().stream().map(t -> {
	//			TaskViewDTO<?> dto = getMapper().map(t, t.viewClass());
	//			dto.postApply();
	//			return dto;
	//		}).toList();
	//	}

}
