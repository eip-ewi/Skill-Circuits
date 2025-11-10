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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.view.ReleaseDTO;
import nl.tudelft.skills.dto.view.ReleaseDetailsView;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.security.AuthorisationService;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest()
public class UserVersionServiceTest {

	private final UserVersionService userVersionService;
	private final UserVersionRepository userVersionRepository;
	private final PersonRepository scPersonRepository;
	private final AuthorisationService authorisationService;
	private final GitLabReleaseClient gitLabClient;

	private final BuildProperties buildProperties;

	@Autowired
	public UserVersionServiceTest(UserVersionRepository userVersionRepository,
			PersonRepository scPersonRepository) {
		this.userVersionRepository = userVersionRepository;
		this.scPersonRepository = scPersonRepository;
		this.authorisationService = mock(AuthorisationService.class);
		this.gitLabClient = mock(GitLabReleaseClient.class);
		this.buildProperties = mock(BuildProperties.class);

		userVersionService = new UserVersionService(userVersionRepository, scPersonRepository,
				authorisationService, gitLabClient, buildProperties);
	}

	@Test
	public void isUpToDate() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		UserVersion userVersion = UserVersion.builder().person(scPerson).version("2.1.0").build();
		scPersonRepository.save(scPerson);
		userVersionRepository.save(userVersion);

		when(buildProperties.getVersion()).thenReturn("2.1.0");

		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isTrue();
	}

	@Test
	public void isNotUpToDate() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		UserVersion userVersion = UserVersion.builder().person(scPerson).version("2.1.0").build();
		scPersonRepository.save(scPerson);
		userVersionRepository.save(userVersion);

		when(buildProperties.getVersion()).thenReturn("2.2.0");

		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isFalse();
	}

	@Test
	public void isUpToDateNotPresent() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		when(buildProperties.getVersion()).thenReturn("2.1.0");
		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isFalse();
	}

	@Test
	public void isUpToDateNotAuthenticated() {
		when(authorisationService.isAuthenticated()).thenReturn(false);

		boolean isUpToDate = userVersionService.isUpToDate();
		assertThat(isUpToDate).isTrue();
	}

	@Test
	public void versionUpdateNotPresent() {
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		SCPerson scPerson = SCPerson.builder().id(person.getId()).viewMode(ViewMode.VIEWER).build();
		scPersonRepository.save(scPerson);

		when(buildProperties.getVersion()).thenReturn("2.1.0");

		assertThat(userVersionRepository.findByPersonId(person.getId())).isEmpty();
		userVersionService.versionUpdate();
		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());
		assertThat(userVersion).isPresent();
		assertThat(userVersion.get().getVersion()).isEqualTo("2.1.0");
	}

	@Test
	public void versionUpdatePresent() {
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		SCPerson scPerson = SCPerson.builder().id(person.getId()).viewMode(ViewMode.VIEWER).build();
		UserVersion userVersionOld = UserVersion.builder().person(scPerson).version("2.1.0").build();
		scPersonRepository.save(scPerson);
		userVersionRepository.save(userVersionOld);

		when(buildProperties.getVersion()).thenReturn("2.2.0");

		Optional<UserVersion> userVersion = userVersionRepository.findByPersonId(person.getId());
		assertThat(userVersion).isPresent();
		assertThat(userVersion.get().getVersion()).isEqualTo("2.1.0");

		userVersionService.versionUpdate();

		userVersion = userVersionRepository.findByPersonId(person.getId());
		assertThat(userVersion).isPresent();
		assertThat(userVersion.get().getVersion()).isEqualTo("2.2.0");
	}

	@Test
	public void versionInformationIsUpToDate() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		UserVersion userVersion = UserVersion.builder().person(scPerson).version("2.1.0").build();
		scPersonRepository.save(scPerson);
		userVersionRepository.save(userVersion);

		when(buildProperties.getVersion()).thenReturn("2.1.0");

		List<ReleaseDetailsView> releaseDetails = userVersionService.versionInformation();
		assertThat(releaseDetails).isEmpty();
	}

	@Test
	public void versionInformationNotPresent() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		when(buildProperties.getVersion()).thenReturn("2.1.0");

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		scPersonRepository.save(scPerson);

		ReleaseDTO r0 = ReleaseDTO.builder().name("2.1.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		ReleaseDTO r1 = ReleaseDTO.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		ReleaseDTO r2 = ReleaseDTO.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));

		List<ReleaseDetailsView> releaseDetails = userVersionService.versionInformation();
		assertThat(releaseDetails.size()).isEqualTo(1);
		assertThat(releaseDetails)
				.isEqualTo(List.of(new ReleaseDetailsView(r0.getName(), r0.getDescription_html())));
	}

	@Test
	public void versionInformationPresent() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		when(buildProperties.getVersion()).thenReturn("2.1.0");

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		UserVersion userVersion = UserVersion.builder().person(scPerson).version("1.1.0").build();
		scPersonRepository.save(scPerson);
		userVersionRepository.save(userVersion);

		ReleaseDTO r0 = ReleaseDTO.builder().name("2.1.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		ReleaseDTO r1 = ReleaseDTO.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		ReleaseDTO r2 = ReleaseDTO.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));

		List<ReleaseDetailsView> releaseDetails = userVersionService.versionInformation();
		assertThat(releaseDetails.size()).isEqualTo(2);
		assertThat(releaseDetails)
				.isEqualTo(List.of(new ReleaseDetailsView(r0.getName(), r0.getDescription_html()),
						new ReleaseDetailsView(r1.getName(), r1.getDescription_html())));
	}

	@Test
	public void versionInformationPresentMissedVersion() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		when(buildProperties.getVersion()).thenReturn("2.1.0");

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		UserVersion userVersion = UserVersion.builder().person(scPerson).version("1.3.0").build();
		scPersonRepository.save(scPerson);
		userVersionRepository.save(userVersion);

		ReleaseDTO r0 = ReleaseDTO.builder().name("2.1.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		ReleaseDTO r1 = ReleaseDTO.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		ReleaseDTO r2 = ReleaseDTO.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));

		List<ReleaseDetailsView> releaseDetails = userVersionService.versionInformation();
		assertThat(releaseDetails.size()).isEqualTo(1);
		assertThat(releaseDetails)
				.isEqualTo(List.of(new ReleaseDetailsView(r0.getName(), r0.getDescription_html())));
	}

	@Test
	public void versionInformationPresentNoGitLabRelease() {
		when(authorisationService.isAuthenticated()).thenReturn(true);
		Person person = Person.builder().id(1L).build();
		when(authorisationService.getAuthenticatedPerson()).thenReturn(person);
		when(buildProperties.getVersion()).thenReturn("2.3.0");

		SCPerson scPerson = SCPerson.builder().id(1L).viewMode(ViewMode.VIEWER).build();
		scPersonRepository.save(scPerson);

		ReleaseDTO r0 = ReleaseDTO.builder().name("2.1.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		ReleaseDTO r1 = ReleaseDTO.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		ReleaseDTO r2 = ReleaseDTO.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));

		List<ReleaseDetailsView> releaseDetails = userVersionService.versionInformation();
		assertThat(releaseDetails).isEmpty();
	}
}
