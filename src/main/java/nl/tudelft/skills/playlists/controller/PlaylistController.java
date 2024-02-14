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

@Controller
@RequestMapping("playlist")
public class PlaylistController {
	private ResearchParticipantService researchParticipantService;
	private PersonService personService;

	private PlaylistService playlistService;
	private PlaylistRepository playlistRepository;

	@Autowired
	public PlaylistController(ResearchParticipantService researchParticipantService,
			PersonService personService, PlaylistService playlistService,
			PlaylistRepository playlistRepository) {
		this.researchParticipantService = researchParticipantService;
		this.personService = personService;
		this.playlistService = playlistService;
		this.playlistRepository = playlistRepository;
	}

	@PostMapping
	@Transactional
	public String createPlaylist(@AuthenticatedPerson Person person, @RequestBody SkillCreateDTO create,
			Model model) {

		return "Success";
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
	@PreAuthorize("!@authorisationService.canEditEdition(#editionId)")
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
