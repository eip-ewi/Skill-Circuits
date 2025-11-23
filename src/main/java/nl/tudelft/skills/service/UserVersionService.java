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
package nl.tudelft.skills.service;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.view.ReleaseDTO;
import nl.tudelft.skills.dto.view.ReleaseDetailsView;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.UserVersion;
import nl.tudelft.skills.repository.PersonRepository;
import nl.tudelft.skills.repository.UserVersionRepository;
import nl.tudelft.skills.security.AuthorisationService;

@Service
@AllArgsConstructor
public class UserVersionService {
	private final UserVersionRepository userVersionRepository;
	private final PersonRepository scPersonRepository;
	private final AuthorisationService authorisationService;
	private final GitLabReleaseClient gitLabClient;

	private final BuildProperties buildProperties;

	/**
	 * Checks if the person has seen the latest version before
	 *
	 * @return True if the person already seen the newest version, false otherwise
	 */
	public boolean isUpToDate() {
		if (!authorisationService.isAuthenticated()) {
			return true;
		}
		Person person = authorisationService.getAuthenticatedPerson();

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());

		String latestVersion = buildProperties.getVersion();

		return userVersion.isPresent() && userVersion.get().getVersion().equals(latestVersion);
	}

	/**
	 * Updates the version for a given person to the latest one
	 */
	public void versionUpdate() {
		Person authPerson = authorisationService.getAuthenticatedPerson();
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(authPerson.getId());
		String latestVersion = buildProperties.getVersion();

		if (userVersion.isPresent()) {
			UserVersion presentVersion = userVersion.get();
			presentVersion.setVersion(latestVersion);
			userVersionRepository.save(presentVersion);
		} else {
			userVersionRepository.save(UserVersion.builder().version(latestVersion)
					.person(person).build());

		}
	}

	/**
	 * Collects a list of release information that the user haven't seen yet. For proper functionality the
	 * version in build.gradle.kts should match the latest version in GitLab!
	 *
	 * @return The list of {@link ReleaseDetailsView} containing the release information
	 */
	public List<ReleaseDetailsView> versionInformation() {
		if (!authorisationService.isAuthenticated() || isUpToDate()) {
			return List.of();
		}
		Person person = authorisationService.getAuthenticatedPerson();

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());

		List<ReleaseDTO> releases = gitLabClient.getReleases();

		if (userVersion.isPresent()) {
			// Extracting the date of the user's last seen version
			var userReleaseDate = releases.stream()
					.filter(r -> r.getName().equals(userVersion.get().getVersion()))
					.map(ReleaseDTO::getReleased_at).findFirst();
			if (userReleaseDate.isPresent()) {
				// Collecting the new releases, the user haven't seen yet, based on the dates
				return releases.stream()
						.filter(r -> r.getReleased_at().isAfter(userReleaseDate.get()))
						.map(x -> new ReleaseDetailsView(x.getName(), x.getDescription_html())).toList();
			}
		}
		// If there is no previously seen version saved, the newest one is displayed.
		// Newest version is only displayed if the GitLab version is matching the version in build.gradle.kts!
		Optional<ReleaseDTO> latest = releases.stream().findFirst();
		if (latest.isPresent() && latest.get().getName().equals(buildProperties.getVersion())) {
			return List.of(new ReleaseDetailsView(latest.get().getName(),
					latest.get().getDescription_html()));
		}

		return List.of();
	}
}
