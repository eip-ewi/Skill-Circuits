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
package nl.tudelft.skills.playlists;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.service.PersonService;

@Controller
@RequestMapping("playlist")
public class PlaylistController {
	private ResearchParticipantService researchParticipantService;
	private PersonService personService;

	@Autowired
	public PlaylistController(ResearchParticipantService researchParticipantService,
			PersonService personService) {
		this.researchParticipantService = researchParticipantService;
		this.personService = personService;
	}

	@PostMapping("optIn")
	@Transactional
	@PreAuthorize("!@authorisationService.canEditEdition(#editionId)")
	public ResponseEntity<Void> optIn(@AuthenticatedPerson Person person, Long editionId) {
		SCPerson scPerson = personService.getOrCreateSCPerson(person.getId());
		//        TODO: change edition to ACC
		researchParticipantService.saveOptIn(scPerson);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("optIn")
	public ResponseEntity<String> patchOptIn(@AuthenticatedPerson Person person) {
		//        TODO: use patch object and rename method
		return ResponseEntity
				.ok(researchParticipantService.toggleOpt(personService.getOrCreateSCPerson(person.getId())));
	}
}
