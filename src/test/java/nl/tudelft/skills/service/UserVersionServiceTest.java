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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.controller.UserVersionController;
import nl.tudelft.skills.model.Release;
import nl.tudelft.skills.model.UserVersion;
import nl.tudelft.skills.repository.UserVersionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
class UserVersionServiceTest {
	private final UserVersionRepository userVersionRepository;
	private final PersonRepository scPersonRepository;
	private final UserVersionController userVersionController;

	private final AuthorisationService authorisationService;
	private final BuildProperties buildProperties;
	private final GitLabClient gitLabClient;
	private final TestDatabaseLoader db;
	private final UserVersionService userVersionService;

	@Autowired
	public UserVersionServiceTest(UserVersionRepository userVersionRepository,
			PersonRepository scPersonRepository,
			AuthorisationService authorisationService,
			TestDatabaseLoader db) {
		this.userVersionRepository = userVersionRepository;
		this.scPersonRepository = scPersonRepository;
		this.authorisationService = authorisationService;
		this.db = db;
		this.buildProperties = mock(BuildProperties.class);
		this.gitLabClient = mock(GitLabClient.class);
		this.userVersionController = new UserVersionController(userVersionRepository, scPersonRepository,
				this.buildProperties);
		this.userVersionService = new UserVersionService(userVersionRepository, this.userVersionController,
				authorisationService, gitLabClient, this.buildProperties);
	}

	@Test
	@WithUserDetails("username")
	public void isUpToDate() {
		when(buildProperties.getVersion()).thenReturn("1.0.0");
		Person person = TestUserDetailsService.assemblePerson("username");
		person.setId(db.getPerson().getId());
		userVersionRepository.save(UserVersion.builder()
				.person(scPersonRepository.findByIdOrThrow(person.getId())).version("1.0.0").build());

		assertTrue(userVersionService.isUpToDate());
	}

	@Test
	@WithUserDetails("username")
	public void isNotUpToDate() {
		when(buildProperties.getVersion()).thenReturn("1.1.0");
		Person person = TestUserDetailsService.assemblePerson("username");
		person.setId(db.getPerson().getId());
		userVersionRepository.save(UserVersion.builder()
				.person(scPersonRepository.findByIdOrThrow(person.getId())).version("1.0.0").build());

		assertFalse(userVersionService.isUpToDate());
	}

	@Test
	@WithUserDetails("username")
	public void isUpToDateNotPresent() {
		when(buildProperties.getVersion()).thenReturn("1.1.0");
		assertFalse(userVersionService.isUpToDate());
	}

	@Test
	@WithAnonymousUser
	public void isUpToDateNotAuth() {
		assertTrue(userVersionService.isUpToDate());
	}

	@Test
	@WithUserDetails("username")
	public void versionInformationNotPresent() {
		when(buildProperties.getVersion()).thenReturn("1.2.0");
		Release r1 = Release.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		Release r2 = Release.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r1, r2));
		String update = userVersionService.versionInformation();
		assertEquals("<h2>1.2.0</h2><p>desc1</p>", update);
	}

	@Test
	@WithUserDetails("username")
	public void versionInformationPresent() {
		when(buildProperties.getVersion()).thenReturn("2.1.0");
		Person person = TestUserDetailsService.assemblePerson("username");
		person.setId(db.getPerson().getId());
		userVersionRepository.save(UserVersion.builder()
				.person(scPersonRepository.findByIdOrThrow(person.getId())).version("1.1.0").build());

		Release r0 = Release.builder().name("2.1.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		Release r1 = Release.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		Release r2 = Release.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));
		String update = userVersionService.versionInformation();
		assertEquals("<h2>2.1.0</h2><p>desc0</p><hr><h2>1.2.0</h2><p>desc1</p>", update);
	}

	@Test
	@WithUserDetails("username")
	public void versionInformationPresentNoGitLabRelease() {
		when(buildProperties.getVersion()).thenReturn("2.3.0");
		Person person = TestUserDetailsService.assemblePerson("username");
		person.setId(db.getPerson().getId());
		userVersionRepository.save(UserVersion.builder()
				.person(scPersonRepository.findByIdOrThrow(person.getId())).version("2.2.0").build());

		Release r0 = Release.builder().name("2.1.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		Release r1 = Release.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		Release r2 = Release.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));
		String update = userVersionService.versionInformation();
		assertEquals("", update);
		assertEquals("2.3.0", userVersionRepository.findByPersonId(person.getId()).get().getVersion());
	}

	@Test
	@WithUserDetails("username")
	public void versionInformationPresentMissedVersion() {
		when(buildProperties.getVersion()).thenReturn("2.3.0");
		Person person = TestUserDetailsService.assemblePerson("username");
		person.setId(db.getPerson().getId());
		userVersionRepository.save(UserVersion.builder()
				.person(scPersonRepository.findByIdOrThrow(person.getId())).version("2.2.0").build());

		Release r0 = Release.builder().name("2.3.0").description_html("<p>desc0</p>")
				.released_at(Instant.parse("2023-07-01T00:00:00.793+02:00")).build();
		Release r1 = Release.builder().name("1.2.0").description_html("<p>desc1</p>")
				.released_at(Instant.parse("2023-03-01T00:00:00.793+02:00")).build();
		Release r2 = Release.builder().name("1.1.0").description_html("<p>desc2</p>")
				.released_at(Instant.parse("2023-01-01T00:00:00.793+02:00")).build();
		when(gitLabClient.getReleases()).thenReturn(List.of(r0, r1, r2));
		String update = userVersionService.versionInformation();
		assertEquals("<h2>2.3.0</h2><p>desc0</p>", update);
	}
}
