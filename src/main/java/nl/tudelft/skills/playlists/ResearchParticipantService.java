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

import java.time.LocalDateTime;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;

@Service
public class ResearchParticipantService {

	private PersonRepository personRepository;
	private ResearchParticipantRepository researchParticipantRepository;

	@Autowired
	public ResearchParticipantService(PersonRepository personRepository,
			ResearchParticipantRepository researchParticipantRepository) {

		this.personRepository = personRepository;
		this.researchParticipantRepository = researchParticipantRepository;
	}

	@Transactional
	public ResearchParticipant saveOptIn(SCPerson scPerson) {

		return researchParticipantRepository.save(ResearchParticipant.builder().person(scPerson).build());

	}

	@Transactional
	public String toggleOpt(SCPerson scPerson) {
		Optional<Boolean> opt = optedIn(scPerson);
		ResearchParticipant rp = researchParticipantRepository.findByPerson(scPerson);

		if (opt.isPresent()) {
			if (opt.get()) {
				rp.setOptOut(LocalDateTime.now());
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

	public void addRPInfoToModel(Person person, Model model) {
		if (person != null) {
			SCPerson scPerson = personRepository.findByIdOrThrow(person.getId());
			model.addAttribute("studentOptedIn", optedIn(scPerson));
		} else {
			model.addAttribute("studentOptedIn", Optional.empty());
		}

	}

}
