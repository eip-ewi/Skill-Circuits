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
package nl.tudelft.skills.controller;

import java.util.Set;
import java.util.stream.Collectors;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.SpringContext;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.PathCreateDTO;
import nl.tudelft.skills.dto.patch.PathPatchDTO;
import nl.tudelft.skills.dto.view.edition.PathViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.PathPreferenceRepository;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PersonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("path")
public class PathController {

	private PathRepository pathRepository;
	private PersonRepository personRepository;
	private PersonService personService;

	@Autowired
	public PathController(PathRepository pathRepository, PersonRepository personRepository,
			PersonService personService) {
		this.pathRepository = pathRepository;
		this.personRepository = personRepository;
		this.personService = personService;
	}

	/**
	 * Saves person path preference for an edition.
	 *
	 * @param  person
	 * @param  pathId The path id.
	 * @return
	 */
	@Transactional
	@PostMapping("{editionId}/preference")
	public ResponseEntity<Void> updateUserPathPreference(@AuthenticatedPerson(required = false) Person person,
			@PathVariable Long editionId, @RequestParam(required = false) Long pathId) {

		Path path = pathId == null ? null : pathRepository.findById(pathId).orElse(null);
		SCEdition edition = SpringContext.getBean(EditionRepository.class).findByIdOrThrow(editionId);

		if (person != null) {
			// Save path preference for person
			SCPerson scPerson = personService.getOrCreateSCPerson(person.getId());
			PathPreference pathPreference = PathPreference.builder().hasPreference(true).editionId(editionId)
					.pathId(path != null ? path.getId() : null).person(scPerson).build();
			pathPreference = SpringContext.getBean(PathPreferenceRepository.class).save(pathPreference);

			// Save newly selected path
			Set<PathPreference> updatedSet = scPerson.getPathPreferences().stream().map(p -> {
				if (p.getEditionId().equals(editionId)) {
					p.setPathId(path != null ? path.getId() : null);
				}
				return p;
			}).collect(Collectors.toSet());

			scPerson.setPathPreferences(updatedSet);

			PersonRepository personRepository = SpringContext.getBean(PersonRepository.class);
			personRepository.save(scPerson);
		}
		return ResponseEntity.ok().build();
	}

	/**
	 * Saves person path preference for an edition.
	 *
	 * @param  person
	 * @param  pathId The path id.
	 * @return
	 */
	@Transactional
	@PostMapping("{editionId}/default")
	public ResponseEntity<Void> updateDefaultPathForEdition(
			@AuthenticatedPerson(required = false) Person person, @PathVariable Long editionId,
			@RequestParam(required = false) Long pathId) {
		EditionRepository editionRepository = SpringContext.getBean(EditionRepository.class);
		SCEdition edition = editionRepository.getById(editionId);

		edition.setDefaultPathId(pathId);

		return ResponseEntity.ok().build();
	}

	@Transactional
	@DeleteMapping
	@PreAuthorize("@authorisationService.canDeletePath(#id)")
	public ResponseEntity<Void> deletePath(@RequestParam Long id) {
		Path path = pathRepository.findByIdOrThrow(id);

		// Remove path from tasks
		path.getTasks().forEach(task -> {
			task.getPaths().remove(path);
			TaskRepository taskRepository = SpringContext.getBean(TaskRepository.class);
			taskRepository.save(task);
		});

		// Remove path from edition default
		EditionRepository editionRepository = SpringContext.getBean(EditionRepository.class);
		SCEdition edition = editionRepository.findById(path.getEdition().getId()).get();
		if (edition.getDefaultPathId() != null && edition.getDefaultPathId().equals(path.getId())) {
			edition.setDefaultPathId(null);
			editionRepository.save(edition);
		}

		// Remove path from user preferences
		PathPreferenceRepository pathPreferenceRepository = SpringContext
				.getBean(PathPreferenceRepository.class);
		pathPreferenceRepository.findAllByPathId(path.getId()).forEach(pref -> {
			SCPerson person = personRepository.findByIdOrThrow(pref.getPerson().getId());
			person.setPathPreferences(null);
			personRepository.save(person);
			pathPreferenceRepository.delete(pref);
		});

		pathRepository.delete(path);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Creates a checkpoint, and returns a checkpoint element for the setup sidebar.
	 *
	 * @param  dto   the checkpoint to create.
	 * @param  model the model to add data to.
	 * @return       A new checkpoint html element.
	 */
	@Transactional
	@PostMapping("setup")
	@PreAuthorize("@authorisationService.canCreatePathInEdition(#dto.edition.id)")
	public String createPathSetup(PathCreateDTO dto, Model model) {
		Path path = pathRepository.saveAndFlush(dto.apply());

		// By default, all tasks are added to a new path
		TaskRepository taskRepository = SpringContext.getBean(TaskRepository.class);
		taskRepository.findAllBySkillSubmoduleModuleEditionId(path.getEdition().getId()).forEach(t -> {
			t.getPaths().add(path);
			taskRepository.save(t);
		});

		model.addAttribute("path", View.convert(path, PathViewDTO.class));
		return "edition_setup/path";

	}

	@Transactional
	@PatchMapping
	@PreAuthorize("@authorisationService.canEditPath(#patch.id)")
	public ResponseEntity<Void> patchPath(PathPatchDTO patch) {
		Path path = pathRepository.findByIdOrThrow(patch.getId());
		pathRepository.save(patch.apply(path));

		return ResponseEntity.ok().build();
	}

}
