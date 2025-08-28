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
package nl.tudelft.skills.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nl.tudelft.skills.model.SCEdition;

public interface EditionRepository extends JpaRepository<SCEdition, Long> {

	default SCEdition getOrCreate(Long editionId) {
		return findById(editionId).orElseGet(() -> save(SCEdition.builder().id(editionId).build()));
	}

	@Query("""
			select edition from SCEdition edition
			inner join edition.modules module
			where edition.isVisible = true
			""")
	Set<SCEdition> findAllOpen();

}
