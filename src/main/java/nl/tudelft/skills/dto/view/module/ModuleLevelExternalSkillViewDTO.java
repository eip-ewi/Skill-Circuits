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
package nl.tudelft.skills.dto.view.module;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.BlockView;
import nl.tudelft.skills.dto.view.ItemView;
import nl.tudelft.skills.dto.view.SCModuleSummaryDTO;
import nl.tudelft.skills.dto.view.SkillSummaryDTO;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.repository.SkillRepository;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleLevelExternalSkillViewDTO extends View<ExternalSkill> implements BlockView {

	@NotNull
	private Long id;
	@NotNull
	private SkillSummaryDTO skill;
	@NotNull
	private SCModuleSummaryDTO module;
	@NotNull
	private Integer row;
	@NotNull
	private Integer column;
	@NotNull
	private List<Long> parentIds;
	@NotNull
	private List<Long> childIds;

	@Override
	public void postApply() {
		super.postApply();
		this.parentIds = data.getParents().stream().map(AbstractSkill::getId).toList();
		this.childIds = data.getChildren().stream().map(AbstractSkill::getId).toList();
		this.module = View.convert(SpringContext.getBean(SkillRepository.class).findByIdOrThrow(skill.getId())
				.getSubmodule().getModule(), SCModuleSummaryDTO.class);
	}

	@Override
	public List<? extends ItemView> getItems() {
		return Collections.emptyList();
	}

	public List<Long> getChildIds() {
		return childIds;
	}

}
