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
package nl.tudelft.skills.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.PathCreateDTO;
import nl.tudelft.skills.dto.patch.PathNamePatchDTO;
import nl.tudelft.skills.dto.patch.PathTasksPatchDTO;
import nl.tudelft.skills.dto.view.edition.PathViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PathService;
import nl.tudelft.skills.service.PersonService;

@Controller
@RequestMapping("path")
public class PathController {

	private final PathRepository pathRepository;
	private final PersonRepository personRepository;
	private final EditionRepository editionRepository;
	private final TaskRepository taskRepository;
	private final PathPreferenceRepository pathPreferenceRepository;
	private final PathService pathService;

	private final PersonService personService;

	@Autowired
	public PathController(PathRepository pathRepository, PersonRepository personRepository,
			EditionRepository editionRepository, TaskRepository taskRepository,
			PathPreferenceRepository pathPreferenceRepository, PersonService personService,
			PathService pathService) {
		this.pathRepository = pathRepository;
		this.personRepository = personRepository;
		this.editionRepository = editionRepository;
		this.taskRepository = taskRepository;
		this.pathPreferenceRepository = pathPreferenceRepository;
		this.personService = personService;
		this.pathService = pathService;
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
	public ResponseEntity<Void> updateUserPathPreference(@AuthenticatedPerson Person person,
			@PathVariable Long editionId, @RequestParam(required = false) Long pathId) {

		Path path = pathId == null ? null : pathRepository.findById(pathId).orElse(null);
		SCEdition edition = editionRepository.findByIdOrThrow(editionId);

		// Save path preference for person
		SCPerson scPerson = personService.getOrCreateSCPerson(person.getId());
		PathPreference pathPreference = PathPreference.builder().edition(edition)
				.path(path).person(scPerson).build();

		// Remove all path preferences for this given user and edition
		pathPreferenceRepository.findAllByPersonIdAndEditionId(scPerson.getId(), editionId)
				.forEach(pp -> {
					pathPreferenceRepository.delete(pp);
				});

		pathPreferenceRepository.save(pathPreference);

		return ResponseEntity.ok().build();
	}

	/**
	 * Saves person path preference for an edition.
	 *
	 * @param  pathId The path id.
	 * @return
	 */
	@Transactional
	@PostMapping("{editionId}/default")
	@PreAuthorize("@authorisationService.canEditPathInEdition(#editionId)")
	public ResponseEntity<Void> updateDefaultPathForEdition(@PathVariable Long editionId,
			@RequestParam(required = false) Long pathId) {
		SCEdition edition = editionRepository.getById(editionId);

		Path path = pathRepository.findByIdOrThrow(pathId);
		edition.setDefaultPath(path);

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
			taskRepository.save(task);
		});

		// Remove path from edition default
		SCEdition edition = editionRepository.findById(path.getEdition().getId()).get();
		if (edition.getDefaultPath() != null && edition.getDefaultPath().equals(path)) {
			edition.setDefaultPath(null);
			editionRepository.save(edition);
		}

		// Remove path from user preferences
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
		taskRepository.findAllBySkillSubmoduleModuleEditionId(path.getEdition().getId())
				.forEach(t -> {
					t.getPaths().add(path);
					taskRepository.save(t);
				});

		model.addAttribute("path", View.convert(path, PathViewDTO.class));
		return "edition_setup/path";

	}

	@Transactional
	@PatchMapping("name")
	@PreAuthorize("@authorisationService.canEditPath(#patch.id)")
	public ResponseEntity<Void> renamePath(PathNamePatchDTO patch) {
		Path oldPath = pathRepository.findByIdOrThrow(patch.getId());
		pathRepository.save(patch.apply(oldPath));
		return ResponseEntity.ok().build();
	}

	@Transactional
	@PatchMapping("tasks")
	@PreAuthorize("@authorisationService.canEditPath(#patch.id)")
	public ResponseEntity<Void> patchPathTasks(PathTasksPatchDTO patch) {
		Path path = pathRepository.findByIdOrThrow(patch.getId());
		pathService.updateTasksInPathManyToMany(patch, path);
		return ResponseEntity.ok().build();
	}

}
