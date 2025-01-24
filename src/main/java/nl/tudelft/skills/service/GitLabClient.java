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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import nl.tudelft.skills.dto.view.ReleaseDTO;

@Service
public class GitLabClient {
	private final WebClient webClient = WebClient.builder().baseUrl("https://gitlab.ewi.tudelft.nl/").build();

	/**
	 * Collects all Skill Circuits releases from GitLab
	 *
	 * @return An array of Releases for Skill Circuits
	 */
	public List<ReleaseDTO> getReleases() {
		var entity = webClient.get()
				.uri("api/v4/projects/7331/releases?include_html_description=true")
				.retrieve()
				.toEntity(ReleaseDTO[].class)
				.block();
		if (entity == null || entity.getBody() == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(entity.getBody());
		}
	}
}
