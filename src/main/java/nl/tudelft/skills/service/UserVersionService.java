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
package nl.tudelft.skills.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.controller.UserVersionController;
import nl.tudelft.skills.dto.view.ReleaseDTO;
import nl.tudelft.skills.model.UserVersion;
import nl.tudelft.skills.repository.UserVersionRepository;
import nl.tudelft.skills.security.AuthorisationService;

@Service
public class UserVersionService {
	private final UserVersionRepository userVersionRepository;

	private final UserVersionController userVersionController;

	private final AuthorisationService authorisationService;
	private final GitLabClient gitLabClient;

	private final BuildProperties buildProperties;

	@Autowired
	public UserVersionService(UserVersionRepository userVersionRepository,
			UserVersionController userVersionController, AuthorisationService authorisationService,
			GitLabClient gitLabClient, BuildProperties buildProperties) {
		this.userVersionRepository = userVersionRepository;
		this.userVersionController = userVersionController;
		this.authorisationService = authorisationService;
		this.gitLabClient = gitLabClient;
		this.buildProperties = buildProperties;
	}

	/**
	 * Checks if the person has seen the latest version before
	 *
	 * @return True if the person already seen the newest version, false otherwise
	 */
	public boolean isUpToDate() {
		if (!authorisationService.isAuthenticated()) {
			return true;
		}
		Person person = authorisationService.getAuthPerson();

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());

		String latestVersion = buildProperties.getVersion();

		return userVersion.isPresent() && userVersion.get().getVersion().equals(latestVersion);
	}

	/**
	 * Creates html code for the release information that the user haven't seen yet
	 *
	 * @return The html code containing the release information
	 */
	public String versionInformation() {
		if (!authorisationService.isAuthenticated()) {
			return "";
		}
		if (isUpToDate()) {
			return "";
		}
		Person person = authorisationService.getAuthPerson();

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());

		List<ReleaseDTO> releases = gitLabClient.getReleases();

		if (releases == null)
			return "";

		if (userVersion.isPresent()) {
			var userReleaseDate = releases.stream()
					.filter(r -> r.getName().equals(userVersion.get().getVersion()))
					.map(ReleaseDTO::getReleased_at).findFirst();
			if (userReleaseDate.isPresent()) {
				List<ReleaseDTO> newReleases = releases.stream()
						.filter(r -> r.getReleased_at().isAfter(userReleaseDate.get())).toList();
				List<String> newReleasesDesc = newReleases.stream()
						.map(x -> "<h2>" + x.getName() + "</h2>" + x.getDescription_html()).toList();
				return String.join("<hr>", newReleasesDesc);
			}
		}
		Optional<ReleaseDTO> latest = releases.stream().findFirst();
		if (latest.isPresent()) {
			if (latest.get().getName().equals(buildProperties.getVersion())) {
				return "<h2>" + latest.get().getName() + "</h2>" + latest.get().getDescription_html();
			} else {
				userVersionController.versionUpdate(person);
				return "";
			}
		}

		return "";
	}
}
