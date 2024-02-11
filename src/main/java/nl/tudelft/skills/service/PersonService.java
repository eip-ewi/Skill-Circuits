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
package nl.tudelft.skills.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
public class PersonService {

	private PersonRepository personRepository;
	private PathRepository pathRepository;
	private EditionService editionService;

	@Autowired
	public PersonService(PersonRepository personRepository, PathRepository pathRepository,
			EditionService editionService) {
		this.personRepository = personRepository;
		this.pathRepository = pathRepository;
		this.editionService = editionService;
	}

	/**
	 * Get user saved path for an edition.
	 *
	 * @param  personId  person id.
	 * @param  editionId edition id.
	 * @return           path followed in edition by person or none if none was saved or path is no-path.
	 */
	public Optional<PathPreference> getPathForEdition(Long personId, Long editionId) {
		SCPerson scPerson = personRepository.findByIdOrThrow(personId);
		return scPerson.getPathPreferences().stream()
				.filter(pp -> pp.getEdition().getId().equals(editionId)).findFirst();
	}

	/**
	 * Returns a SCPerson by person id. If it doesn't exist, creates one.
	 *
	 * @param  personId The id of the person
	 * @return          The SCPerson with id.
	 */
	@Transactional
	public SCPerson getOrCreateSCPerson(Long personId) {
		return personRepository.findById(personId)
				.orElseGet(() -> personRepository.save(SCPerson.builder().id(personId).build()));
	}

	/**
	 * Adds attributes concerning the personal path, and selected path of a person to the response model.
	 * Returns the path's task ids, iff a path is selected.
	 *
	 * @param  personId  The id of the currently authenticated person (non-null).
	 * @param  model     The response model.
	 * @param  editionId The id of the edition.
	 * @param  skill     If the attributes should only be added for a specific skill, the skill. Otherwise,
	 *                   null.
	 * @return           If a path is selected, an Optional containing the set of the task ids in the path.
	 *                   Otherwise, an empty Optional.
	 */
	public Optional<Set<Long>> setPersonalPathAttributes(Long personId, Model model, Long editionId,
			Skill skill) {
		Path path = getDefaultOrPreferredPath(personId, editionId);
		model.addAttribute("selectedPathId", path != null ? path.getId() : null);

		SCPerson scPerson = personRepository.findByIdOrThrow(personId);
		Set<Task> tasks = scPerson.getTasksAdded();
		Set<Skill> skillsModified = scPerson.getSkillsModified();
		// If the skill is null, the tasksAdded and skillsModified are all added tasks and modified skills. Otherwise,
		// they are only added corresponding to the skill (tasks in the skill and the skill itself, if modified).
		if (skill == null) {
			model.addAttribute("tasksAdded",
					tasks.stream().map(at -> View.convert(at, TaskViewDTO.class))
							.collect(Collectors.toSet()));
			model.addAttribute("skillsModified", skillsModified.stream()
					.map(at -> View.convert(at, ModuleLevelSkillViewDTO.class)).collect(Collectors.toSet()));
		} else {
			model.addAttribute("tasksAdded", tasks.stream().filter(t -> skill.getTasks().contains(t))
					.map(at -> View.convert(at, TaskViewDTO.class)).collect(Collectors.toSet()));
			model.addAttribute("skillsModified",
					skillsModified.contains(skill)
							? Set.of(View.convert(skill, ModuleLevelSkillViewDTO.class))
							: Set.of());
		}

		// Returns an Optional of the tasks in the path if a path is selected, and an empty Optional otherwise.
		return path == null ? Optional.empty()
				: Optional.of(path.getTasks().stream().map(Task::getId).collect(Collectors.toSet()));
	}

	/**
	 * Return the followed path in an edition. This is either the user preferred path or the default one.
	 *
	 * @param  personId  Authenticated person id, or null if not authenticated
	 * @param  editionId Edition id.
	 * @return           Followed path.
	 */
	public Path getDefaultOrPreferredPath(Long personId, Long editionId) {
		Path path = editionService.getDefaultPath(editionId);

		Optional<PathPreference> preference = getPathForEdition(personId, editionId);
		if (personId == null || preference.isEmpty()) {
			return path;
		}

		return preference.filter(p -> p.getPath() != null)
				.flatMap(p -> pathRepository.findById(p.getPath().getId())).orElse(null);
	}

}
