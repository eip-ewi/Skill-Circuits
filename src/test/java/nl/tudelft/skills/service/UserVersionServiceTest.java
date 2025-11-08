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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.security.AuthorisationService;

@Transactional
@SpringBootTest()
public class UserVersionServiceTest {
	private final UserVersionService userVersionService;
	private final UserVersionRepository userVersionRepository;
	private final PersonRepository scPersonRepository;
	private final AuthorisationService authorisationService;
	private final GitLabReleaseClient gitLabClient;

	private final BuildProperties buildProperties;

	public UserVersionServiceTest() {
		userVersionRepository = mock(UserVersionRepository.class);
		scPersonRepository = mock(PersonRepository.class);
		authorisationService = mock(AuthorisationService.class);
		gitLabClient = mock(GitLabReleaseClient.class);
		buildProperties = mock(BuildProperties.class);

		userVersionService = new UserVersionService(userVersionRepository, scPersonRepository,
				authorisationService, gitLabClient, buildProperties);
	}

	@Test
	public void isUpToDate() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);

		UserVersion userVersion = UserVersion.builder().id(1L).version("2.1.0").build();
		when(userVersionRepository.findByPersonId(anyLong())).thenReturn(Optional.of(userVersion));

		when(buildProperties.getVersion()).thenReturn("2.1.0");

		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isTrue();
	}

	@Test
	public void isUpToDateNotAuthenticated() {
		when(authorisationService.isAuthenticated()).thenReturn(false);

		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isTrue();
	}

	@Test
	public void isNotUpToDate() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);

		UserVersion userVersion = UserVersion.builder().id(1L).version("2.1.0").build();
		when(userVersionRepository.findByPersonId(anyLong())).thenReturn(Optional.of(userVersion));

		when(buildProperties.getVersion()).thenReturn("2.2.0");

		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isFalse();
	}
}
