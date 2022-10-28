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
package nl.tudelft.skills.repository;

import java.util.Collection;
import java.util.Set;

import nl.tudelft.skills.model.AbstractSkill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

public interface AbstractSkillRepository extends JpaRepository<AbstractSkill, Long> {

	default AbstractSkill findByIdOrThrow(Long id) {
		return findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("AbstractSkill was not found: " + id));
	}

	Set<AbstractSkill> findAllByIdIn(Collection<Long> ids);

}