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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.patch.UserVersionPatchDTO;
import nl.tudelft.skills.model.UserVersion;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.UserVersionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user_version")
public class UserVersionController {
	private final UserVersionRepository userVersionRepository;
	private final PersonRepository scPersonRepository;

	@Autowired
	public UserVersionController(UserVersionRepository userVersionRepository,
			PersonRepository scPersonRepository) {
		this.userVersionRepository = userVersionRepository;
		this.scPersonRepository = scPersonRepository;
	}

	@PatchMapping
	@Transactional
	public String versionUpdate(@AuthenticatedPerson Person authPerson) throws GitLabApiException {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(authPerson.getId());

		GitLabApi gitLabApi = new GitLabApi("https://gitlab.ewi.tudelft.nl/", "YOUR_PERSONAL_ACCESS_TOKEN");
		Stream<Release> releases = gitLabApi.getReleasesApi().getReleasesStream("7331");

		if (userVersion.isPresent()) {
			List<Release> newReleases = releases
					.filter(r -> r.getReleasedAt().after(userVersion.get().getReleaseDate())).toList();
			if (newReleases.size() > 0) {
				Optional<Release> latest = newReleases.stream().findFirst();
				List<String> newReleasesDesc = newReleases.stream().map(Release::getDescription).toList();
				UserVersionPatchDTO patch = UserVersionPatchDTO.builder().version(latest.get().getName())
						.releaseDate(latest.get().getReleasedAt()).build();
				userVersionRepository.save(patch.apply(userVersion.get()));
				return String.join("\n", newReleasesDesc);
			}
		} else {
			Optional<Release> latest = releases.findFirst();
			if (latest.isPresent()) {
				userVersionRepository.save(UserVersion.builder().version(latest.get().getName())
						.person(person).releaseDate(latest.get().getReleasedAt()).build());
				return latest.get().getDescription();
			}

		}
		return "";
	}
}
