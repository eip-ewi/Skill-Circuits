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

import java.util.List;
import java.util.Optional;

import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import nl.tudelft.skills.model.PathPreference;

public interface PathPreferenceRepository extends JpaRepository<PathPreference, Long> {

	default PathPreference findByIdOrThrow(Long id) {
		return findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("PathPreference was not found: " + id));
	}

    @Query("""
           select pathPreference from PathPreference pathPreference
           where pathPreference.person.id = :#{#person.id} and pathPreference.edition.id = :#{#edition.id}
           """)
    Optional<PathPreference> findByPersonAndEdition(@Param("person") SCPerson person, @Param("edition") SCEdition edition);

	List<PathPreference> findAllByPathId(Long pathId);

	List<PathPreference> findAllByPersonIdAndEditionId(Long personId, Long editionId);

	boolean existsByPathId(Long pathId);

}
