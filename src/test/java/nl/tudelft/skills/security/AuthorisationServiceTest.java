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
package nl.tudelft.skills.security;

import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.skills.TestSkillCircuitsApplication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;

@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class AuthorisationServiceTest {

	private final AuthorisationService authorisationService;

	@Autowired
	public AuthorisationServiceTest(AuthorisationService authorisationService) {
		this.authorisationService = authorisationService;
	}

	@Test
	@WithUserDetails("username")
	void getAuthPerson() {
		assertThat(authorisationService.getAuthPerson().getUsername()).isEqualTo("username");
	}

	@Test
	@WithUserDetails("username")
	void isAuthenticated() {
		assertThat(authorisationService.isAuthenticated()).isTrue();
	}

}
