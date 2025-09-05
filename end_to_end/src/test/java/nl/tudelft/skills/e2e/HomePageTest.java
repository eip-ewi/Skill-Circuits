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
package nl.tudelft.skills.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static nl.tudelft.skills.e2e.User.csestudent1;
import static nl.tudelft.skills.e2e.User.cseteacher1;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HomePageTest extends EndToEndTest
		implements BasicScripts.WithBasicScripts, EditionScripts.WithEditionScripts {

	@Test
	@DisplayName("Log in as cseteacher1 and then log out")
	public void logInAndLogOut() {
		basic().logIn(cseteacher1);

		assertThat(locators().heading("My courses")).isVisible();
		assertEquals("My Courses - Skill Circuits", page().title());

		basic().logOut();

		assertThat(locators().link("Click here to log in")).isVisible();
		assertThat(locators().query(".header")).isHidden();
		assertThat(locators().heading("Welcome to Skill Circuits")).isVisible();
		assertEquals("Skill Circuits", page().title());
	}

	@Test
	@DisplayName("Publish and unpublish editions and verify their visibility as student")
	public void editionVisibilityForStudentsMatchesPublishedState() {
		basic().logIn(cseteacher1);

		Edition edition = edition().findAnyManagingEdition();
		edition().publish(edition);

		if (edition().submodules(edition).isEmpty()) {
			edition().addSubmodule(edition, "Test submodule");
		}

		basic().logOut();
		basic().logIn(csestudent1);

		assertTrue(edition().canSeeEdition(edition));

		basic().logOut();
		basic().logIn(cseteacher1);

		edition().unpublish(edition);

		basic().logOut();
		basic().logIn(csestudent1);

		assertFalse(edition().canSeeEdition(edition));
	}

}
