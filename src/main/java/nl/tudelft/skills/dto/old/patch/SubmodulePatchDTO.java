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
package nl.tudelft.skills.dto.old.patch;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.old.id.SCModuleIdDTO;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubmodulePatchDTO extends Patch<Submodule> {

	@NotNull
	private Long id;
	@NotBlank
	private String name;
	@NotNull
	private SCModuleIdDTO module;
	@NotNull
	@Builder.Default
	private List<SubmoduleLevelSkillPatchDTO> items = new ArrayList<>();
	@NotNull
	@Builder.Default
	private Set<Long> removedItems = new HashSet<>();

	@Override
	protected void applyOneToOne() {
		updateNonNull(name, data::setName);
		updateNonNullId(module, data::setModule);
	}

	@Override
	protected void applyOneToMany() {
		Map<Long, Skill> skills = data.getSkills().stream()
				.collect(Collectors.toMap(AbstractSkill::getId, Function.identity()));
		items.forEach(p -> p.apply(skills.get(p.getId())));

		data.getSkills().stream().filter(s -> removedItems.contains(s.getId())).toList()
				.forEach(s -> data.getSkills().remove(s));
	}

	@Override
	protected void validate() {
		Set<Long> skillIds = data.getSkills().stream().map(AbstractSkill::getId).collect(Collectors.toSet());
		if (!skillIds.containsAll(
				items.stream().map(SubmoduleLevelSkillPatchDTO::getId).collect(Collectors.toSet()))) {
			errors.rejectValue("items", "itemNotInSubmodule", "Item is not in submodule");
		}
		if (!skillIds.containsAll(removedItems)) {
			errors.rejectValue("removedItems", "itemNotInSubmodule", "Item is not in submodule");
		}
	}
}
