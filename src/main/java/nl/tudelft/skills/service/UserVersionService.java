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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.Release;
import nl.tudelft.skills.model.UserVersion;
import nl.tudelft.skills.repository.UserVersionRepository;
import nl.tudelft.skills.security.AuthorisationService;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@AllArgsConstructor
public class UserVersionService {
	private final UserVersionRepository userVersionRepository;
	private final AuthorisationService authorisationService;
	private final WebClient webClient = WebClient.builder().baseUrl("https://gitlab.ewi.tudelft.nl/").build();

	private BuildProperties buildProperties;

	public boolean isUpToDate() {
		if (!authorisationService.isAuthenticated()) {
			return true;
		}
		Person person = authorisationService.getAuthPerson();

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());

		String latestVersion = buildProperties.getVersion();

		return userVersion.isPresent() && userVersion.get().getVersion().equals(latestVersion);
	}

	public String versionInformation() {
		if (!authorisationService.isAuthenticated()) {
			return "";
		}
		if (isUpToDate()) {
			return "";
		}
		Person person = authorisationService.getAuthPerson();

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());

		var entity = webClient.get()
				.uri("api/v4/projects/7331/releases?include_html_description=true")
				.retrieve()
				.toEntity(Release[].class)
				.block();
		var releases = entity.getBody();

		if (releases == null)
			return "";

		if (userVersion.isPresent()) {
			var userReleaseDate = Arrays.stream(releases)
					.filter(r -> r.getName().equals(userVersion.get().getVersion()))
					.map(Release::getReleased_at).findFirst();
			List<String> newReleasesDesc = Arrays.stream(releases)
					.filter(r -> r.getReleased_at().after(userReleaseDate.get()))
					.map(Release::getDescription_html).toList();
			return String.join("\n", newReleasesDesc);
		} else {
			Optional<Release> latest = Arrays.stream(releases).findFirst();
			if (latest.isPresent()) {
				return latest.get().getDescription_html();
			}

		}
		return "";
	}
}
