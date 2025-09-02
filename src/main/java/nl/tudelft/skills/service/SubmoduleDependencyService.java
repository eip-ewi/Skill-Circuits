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
package nl.tudelft.skills.service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Submodule;

/**
 * Responsible for determining dependencies between submodules.
 */
@Service
@AllArgsConstructor
public class SubmoduleDependencyService {

	///
    /// The parents of a submodule are the submodules of the parents of all its skills.
    /// A submodule is never a parent of itself.
    ///
	public Set<Submodule> getSubmoduleParents(Submodule submodule) {
		return submodule.getSkills().stream().flatMap(skill -> skill.getParents().stream())
				.map(AbstractSkill::getSubmodule).filter(parent -> !Objects.equals(submodule, parent))
				.collect(Collectors.toSet());
	}

	///
    /// The children of a submodule are the submodules of the children of all its skills.
    /// A submodule is never a child of itself.
    ///
	public Set<Submodule> getSubmoduleChildren(Submodule submodule) {
		return submodule.getSkills().stream().flatMap(skill -> skill.getChildren().stream())
				.map(AbstractSkill::getSubmodule).filter(child -> !Objects.equals(submodule, child))
				.collect(Collectors.toSet());
	}

}
