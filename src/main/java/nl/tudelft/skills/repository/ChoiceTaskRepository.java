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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import nl.tudelft.skills.model.ChoiceTask;

public interface ChoiceTaskRepository extends JpaRepository<ChoiceTask, Long> {

	default ChoiceTask findByIdOrThrow(Long id) {
		return findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("ChoiceTask was not found: " + id));
	}

}
