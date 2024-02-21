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

import javax.transaction.Transactional;

import nl.tudelft.skills.playlists.dto.PlaylistCreateDTO;
import nl.tudelft.skills.playlists.dto.PlaylistTaskCreateDTO;
import nl.tudelft.skills.playlists.dto.PlaylistTaskViewDTO;
import nl.tudelft.skills.playlists.dto.PlaylistVersionCreateDTO;
import nl.tudelft.skills.playlists.model.*;
import nl.tudelft.skills.playlists.repository.PlaylistTaskRepository;
import nl.tudelft.skills.playlists.repository.PlaylistVersionRepository;
import nl.tudelft.skills.playlists.repository.ResearchParticipantRepository;
import nl.tudelft.skills.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.playlists.service.PlaylistService;
import nl.tudelft.skills.playlists.service.ResearchParticipantService;
import nl.tudelft.skills.service.PersonService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
	public ResponseEntity<Void> createPlaylist(@AuthenticatedPerson Person person, @RequestBody PlaylistCreateDTO create,
			Model model) {
		ResearchParticipant participant = researchParticipantRepository
				.findByPerson(personService.getOrCreateSCPerson(person.getId()));
		create.setParticipant(participant);
		Playlist playlist = create.apply();

		PlaylistVersionCreateDTO playlistVersionCreate = create.getPlaylistVersionCreate();
		List<PlaylistTaskCreateDTO> taskCreates = playlistVersionCreate.getTaskCreates();
		taskCreates.forEach(t -> t.setParticipant(participant));
		Set<PlaylistTask> tasks = taskCreates.stream()
				.map(PlaylistTaskCreateDTO::apply).map(playlistTaskRepository:: saveAndFlush).collect(Collectors.toSet());
		playlistVersionCreate.setTasks((tasks));

		PlaylistVersion playlistVersion = playlistVersionCreate.apply();
		playlistVersion.setPlaylist(playlist);
		playlist.setLatestVersion(playlistVersion);
		playlistRepository.saveAndFlush(playlist);

		model.addAttribute("playlistStep", PlaylistStep.PLAY);
//		Return fragment with this playlist
//		In that fragment you have to get skills etc
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
	public ResponseEntity<String> patchOptIn(@AuthenticatedPerson Person person) {
		//        TODO: use patch object and rename method
		return ResponseEntity
				.ok(researchParticipantService.toggleOpt(personService.getOrCreateSCPerson(person.getId())));
	}
}
