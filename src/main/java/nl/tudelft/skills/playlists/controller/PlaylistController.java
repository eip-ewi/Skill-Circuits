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
package nl.tudelft.skills.playlists.controller;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.dto.*;
import nl.tudelft.skills.playlists.model.*;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.playlists.repository.PlaylistTaskRepository;
import nl.tudelft.skills.playlists.repository.PlaylistVersionRepository;
import nl.tudelft.skills.playlists.repository.ResearchParticipantRepository;
import nl.tudelft.skills.playlists.service.PlaylistService;
import nl.tudelft.skills.playlists.service.ResearchParticipantService;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.service.PersonService;

@Controller
@RequestMapping("playlist")
public class PlaylistController {
	private ResearchParticipantService researchParticipantService;
	private ResearchParticipantRepository researchParticipantRepository;
	private PersonService personService;

	private PlaylistService playlistService;
	private PlaylistRepository playlistRepository;
	private PlaylistTaskRepository playlistTaskRepository;
	private PlaylistVersionRepository playlistVersionRepository;
	private TaskRepository taskRepository;
	public Long accEdition = 643L;

	@Autowired
	public PlaylistController(ResearchParticipantService researchParticipantService,
			ResearchParticipantRepository researchParticipantRepository,
			PersonService personService, PlaylistService playlistService,
			PlaylistRepository playlistRepository, PlaylistTaskRepository playlistTaskRepository,
			PlaylistVersionRepository playlistVersionRepository, TaskRepository taskRepository) {
		this.researchParticipantService = researchParticipantService;
		this.researchParticipantRepository = researchParticipantRepository;
		this.personService = personService;
		this.playlistService = playlistService;
		this.playlistRepository = playlistRepository;
		this.playlistTaskRepository = playlistTaskRepository;
		this.playlistVersionRepository = playlistVersionRepository;
		this.taskRepository = taskRepository;
	}

	@PostMapping
	@Transactional
	@PreAuthorize("@researchParticipantService.canCreatePlaylist(#person)")
	public ResponseEntity<Void> createPlaylist(@AuthenticatedPerson Person person,
			@RequestBody PlaylistCreateDTO create) {
		ResearchParticipant participant = researchParticipantRepository
				.findByPerson(personService.getOrCreateSCPerson(person.getId()));
		//		Check if there is an active playlist that should be deactivated
		playlistService.deactivateActivePlaylist(participant);

		create.setParticipant(participant);
		Playlist playlist = create.apply();

		PlaylistVersionCreateDTO playlistVersionCreate = create.getPlaylistVersionCreate();
		List<PlaylistTaskCreateDTO> taskCreates = playlistVersionCreate.getTaskCreates();
		taskCreates.forEach(t -> t.setParticipant(participant));
		Set<PlaylistTask> tasks = taskCreates.stream().map(t -> {
			PlaylistTask exists = playlistTaskRepository.findByParticipantAndTaskId(participant,
					t.getTaskId());
			if (exists != null) {
				return exists;
			} else {
				return playlistTaskRepository.saveAndFlush(t.apply());
			}
		})
				.collect(Collectors.toSet());

		playlistVersionCreate.setTasks(tasks);
		PlaylistVersion playlistVersion = playlistVersionCreate.apply();
		playlist.setLatestVersion(playlistVersion);
		playlistVersion.setPlaylist(playlist);
		playlistRepository.saveAndFlush(playlist);

		return ResponseEntity.ok().build();
	}

	/**
	 * Creates a new research participant
	 *
	 * @param  person    authenticated person who should a student
	 * @param  editionId the edition from which participants are allowed
	 * @return           ResponseEntity with OK status on success
	 */
	@PostMapping("optIn")
	@Transactional
	@PreAuthorize("!@authorisationService.canEditEdition(this.accEdition)")
	public ResponseEntity<Void> optIn(@AuthenticatedPerson Person person, Long editionId) {
		//        TODO: change edition to ACC
		//        TODO: authorize students only
		SCPerson scPerson = personService.getOrCreateSCPerson(person.getId());

		researchParticipantService.saveOptIn(scPerson);
		return ResponseEntity.ok().build();
	}

	/**
	 * Toggle the research participant's opt status
	 *
	 * @param  person The authenticated student that is a research participant
	 * @return        A string indicating the new opt status
	 */
	@PatchMapping("optIn")
	public ResponseEntity<String> patchOptIn(@AuthenticatedPerson Person person,
			@RequestBody(required = false) Map<String, Boolean> data) {
		//        TODO: use patch object and rename method
		boolean clearData = data != null ? data.get("clear_data") : false;
		return ResponseEntity
				.ok(researchParticipantService.toggleOpt(personService.getOrCreateSCPerson(person.getId()),
						clearData));
	}

	@DeleteMapping("/{playlistId}")
	@Transactional
	@PreAuthorize("@researchParticipantService.canCreatePlaylist(#person)")
	public ResponseEntity<Void> deletePlaylist(@AuthenticatedPerson Person person,
			@PathVariable Long playlistId) {

		ResearchParticipant participant = researchParticipantRepository
				.findByPerson(personService.getOrCreateSCPerson(person.getId()));
		if (participant != null) {
			Playlist playlist = playlistRepository.findByIdOrThrow(playlistId);
			if (playlist != null) {
				playlist.setActive(false);
				playlist.setState(PlaylistState.ABORTED);
				playlist.setDeleted(LocalDateTime.now());
				return ResponseEntity.ok().build();
			}
		}
		return ResponseEntity.notFound().build();
	}

	@PatchMapping("/{playlistId}/times")
	@Transactional
	@PreAuthorize("@researchParticipantService.canEditPlaylist(#person, #playlistId)")
	public ResponseEntity<Void> storeCompletionTimes(@AuthenticatedPerson Person person,
			@RequestBody(required = true) PlaylistVersionPatchDTO patch,
			@PathVariable Long playlistId) {
		Playlist playlist = playlistRepository.findByIdOrThrow(playlistId);
		ResearchParticipant participant = researchParticipantRepository
				.findByPerson(personService.getOrCreateSCPerson(person.getId()));
		if (!playlist.getActive()) {
			return ResponseEntity.badRequest().build();
		}

		if (playlist.getState() != PlaylistState.IN_USE){
			playlist.setState(PlaylistState.IN_USE);
		}

		patch.setId(playlist.getLatestVersion().getId());

		List<PlaylistTaskPatchDTO> taskTimes = patch.getTaskTimes();
		//		TODO: Refactor to use PlaylistTask id from the start
		for (PlaylistTaskPatchDTO pTask : taskTimes) {
			PlaylistTask task = playlistTaskRepository.findByParticipantAndTaskId(participant,
					pTask.getTaskId());
			pTask.setId(task.getId());
			PlaylistTask newTask = pTask.apply(task);
			playlistTaskRepository.save(newTask);
		}

		PlaylistVersion playlistVersion = playlistVersionRepository.findByIdOrThrow(patch.getId());

		playlistVersionRepository.save(patch.apply(playlistVersion));
		return ResponseEntity.ok().build();

	}

	@GetMapping("/{playlistId}/times")
	@Transactional
	@ResponseBody
	@PreAuthorize("@researchParticipantService.canEditPlaylist(#person, #playlistId)")
	public PlaylistVersionViewDTO getCompletionTimes(@AuthenticatedPerson Person person,
			@PathVariable Long playlistId) {
		Playlist playlist = playlistRepository.findByIdOrThrow(playlistId);

		return View.convert(playlist.getLatestVersion(), PlaylistVersionViewDTO.class);

	}
}
