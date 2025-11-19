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

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.PathPreferenceRepository;
import nl.tudelft.skills.repository.PersonRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;

@Service
@AllArgsConstructor
public class ProgressService {

	private final PathPreferenceRepository pathPreferenceRepository;
	private final PersonRepository personRepository;
	private final TaskCompletionRepository taskCompletionRepository;

	@Transactional
	public void resetProgress(SCEdition edition, SCPerson person) {
		pathPreferenceRepository.deleteByPersonAndEdition(person, edition);
		taskCompletionRepository
				.deleteAll(taskCompletionRepository.findAllByPersonAndEdition(person, edition));

		person.getTasksAdded().clear();
		person.getTasksRemoved().clear();
		person.getSkillsRevealed().clear();
		personRepository.save(person);
	}

}
