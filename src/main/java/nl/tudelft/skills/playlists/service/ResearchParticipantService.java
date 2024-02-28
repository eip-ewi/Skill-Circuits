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
package nl.tudelft.skills.playlists.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.model.Playlist;
import nl.tudelft.skills.playlists.model.PlaylistStep;
import nl.tudelft.skills.playlists.model.ResearchParticipant;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.playlists.repository.ResearchParticipantRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
public class ResearchParticipantService {

	private PersonRepository personRepository;
	private ResearchParticipantRepository researchParticipantRepository;

	private PlaylistRepository playlistRepository;

	@Autowired
	public ResearchParticipantService(PersonRepository personRepository,
			ResearchParticipantRepository researchParticipantRepository,
			PlaylistRepository playlistRepository) {

		this.personRepository = personRepository;
		this.researchParticipantRepository = researchParticipantRepository;
		this.playlistRepository = playlistRepository;
	}

	/**
	 * Create a new research participant
	 *
	 * @param  scPerson authenticated student
	 * @return          created ResearchParticipant
	 */
	@Transactional
	public ResearchParticipant saveOptIn(SCPerson scPerson) {

		return researchParticipantRepository.save(ResearchParticipant.builder().person(scPerson).build());

	}

	/**
	 * Toggles the opt status of a ResearchParticipant
	 *
	 * @param  scPerson authenticated student which is a research particiapnt
	 * @return          String holding the new opt status
	 */
	@Transactional
	public String toggleOpt(SCPerson scPerson, boolean clearData) {
		Optional<Boolean> opt = optedIn(scPerson);
		ResearchParticipant rp = researchParticipantRepository.findByPerson(scPerson);

		if (opt.isPresent()) {
			if (opt.get()) {
				rp.setOptOut(LocalDateTime.now());
				rp.setClearData(clearData);
				return "Opted out";
			} else {
				rp.setOptIn(LocalDateTime.now());
				return "Opted in";
			}
		} else {
			//            TODO: Throw exception or refactor code
			return "This should not happen";
		}

	}

	/**
	 * Checks whether a student is opted-in or -out
	 *
	 * @param  scPerson Student to be checked
	 * @return          boolean indicating opted-in (true) or opted-out (false)
	 */
	public Optional<Boolean> optedIn(SCPerson scPerson) {
		ResearchParticipant rp = researchParticipantRepository.findByPerson(scPerson);

		if (rp != null) {
			LocalDateTime optInDate = rp.getOptIn();
			LocalDateTime optOutDate = rp.getOptOut();

			if (optOutDate == null) {
				return Optional.of(true);
			} else {
				return Optional.of(!optOutDate.isAfter(optInDate));
			}
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Method used to add ResearhParticipant data to the Thymeleaf Model
	 *
	 * @param person authenticated person to get ResearchParticipant info of
	 * @param model  to add info to
	 */
	public void addRPInfoToModel(Person person, Model model) {
		if (person != null) {
			SCPerson scPerson = personRepository.findByIdOrThrow(person.getId());
			Optional<Boolean> optedIn = optedIn(scPerson);
			model.addAttribute("studentOptedIn", optedIn);

			model.addAttribute("playlistStep", getPlaylistStep(scPerson, optedIn));

		} else {
			model.addAttribute("studentOptedIn", Optional.empty());
		}

	}

	private PlaylistStep getPlaylistStep(SCPerson person, Optional<Boolean> optedIn) {
		if (optedIn.isPresent() && optedIn.get()) {
			ResearchParticipant rp = researchParticipantRepository.findByPerson(person);

			List<Playlist> playlists = rp.getPlaylists();
			if (!playlists.isEmpty()) {
				return playlists.stream().anyMatch(Playlist::getActive) ? PlaylistStep.PLAY
						: PlaylistStep.CREATE;
			}
		}
		return PlaylistStep.FIRST_TIME;

	}

	@Transactional
	public boolean canCreatePlaylist(Person person) {
		if (person != null) {
			SCPerson scPerson = personRepository.findByIdOrThrow(person.getId());
			Optional<Boolean> optedIn = optedIn(scPerson);
			return optedIn.orElse(false);
		} else {
			return false;
		}
	}

	@Transactional
	public boolean canEditPlaylist(Person person, Long playlistId) {
		if (person != null) {
			SCPerson scPerson = personRepository.findByIdOrThrow(person.getId());
			Optional<Boolean> optedIn = optedIn(scPerson);
			if (optedIn.orElse(false)) {
				ResearchParticipant participant = researchParticipantRepository.findByPerson(scPerson);
				Playlist playlist = playlistRepository.findByIdAndParticipant(playlistId, participant);
				return playlist != null && playlist.getActive();
			}
		}

		return false;
	}

}
