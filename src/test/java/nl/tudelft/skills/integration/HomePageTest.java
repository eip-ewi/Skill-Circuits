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
package nl.tudelft.skills.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class HomePageTest extends IntegrationTest {

	@Test
	void testLogInLogOut() {
		navigateTo("");
		Locator logIn = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Login"));
		logIn.click();

		// Check redirection to login page
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Login")).isVisible())
				.isTrue();
		assertThat(page.title()).isEqualTo("Login");

		// Login as teacher
		page.getByLabel("Username").fill("cseteacher1");
		page.getByLabel("Password").fill("cseteacher1");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();

		// Wait until timeout or user dropdown is visible
		Locator userDropdown = page.getByText("CSE Teacher 1");
		userDropdown.waitFor();
		assertThat(userDropdown.isVisible()).isTrue();

		// Check redirection to homepage (with courses)
		assertThat(page
				.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Welcome to Skill Circuits"))
				.isVisible()).isTrue();
		assertThat(page.title()).isEqualTo("Skill Circuits");
		assertThat(page.getByText("CSE1100").isVisible()).isTrue();
		assertThat(page.getByText("CSE1305").isVisible()).isTrue();

		// Log out again
		userDropdown.click();
		assertThat(page.getByText("Your Profile").isVisible()).isTrue();
		assertThat(page.getByText("Inventory").isVisible()).isTrue();
		Locator logOut = page.getByText("Logout");
		assertThat(logOut.isVisible()).isTrue();
		logOut.click();

		// Wait until timeout or log in is visible again
		logIn.waitFor();
		assertThat(logIn.isVisible()).isTrue();

		// Check that it is still on the homepage, but no courses are visible anymore
		assertThat(page
				.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Welcome to Skill Circuits"))
				.isVisible()).isTrue();
		assertThat(page.title()).isEqualTo("Skill Circuits");
		assertThat(page.getByText("CSE1100").isVisible()).isFalse();
		assertThat(page.getByText("CSE1305").isVisible()).isFalse();
	}

	/**
	 * Publishes and un-publishes an edition, and asserts on the individual steps. Also asserts that the
	 * edition is (in)visible to the student/visible on the homepage.
	 */
	@Test
	void testPublishUnpublishEdition() {
		navigateTo("");
		Locator course = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("CSE1100"));

		// Assert that the edition is not visible
		assertThat(course.isVisible()).isFalse();

		logInAs("cseteacher1", "cseteacher1", "CSE Teacher 1");
		navigateToSetupPaneAndAssert();

		// Wait until timeout or publish button is visible
		Locator publish = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Publish edition"));
		publish.waitFor();
		assertThat(publish.isVisible()).isTrue();
		publish.click();

		// Log out as teacher
		logOutAs("CSE Teacher 1");

		// Assert that the edition is visible
		assertThat(course.isVisible()).isTrue();
		logInAs("csestudent1", "csestudent1", "CSE Student 1");
		assertThat(course.isVisible()).isTrue();
		course.click();
		Locator editionHeader = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
				.setName("Object-Oriented Programming - NOW"));
		editionHeader.waitFor();
		assertThat(editionHeader.isVisible()).isTrue();
		logOutAs("CSE Student 1");

		logInAs("cseteacher1", "cseteacher1", "CSE Teacher 1");
		navigateToSetupPaneAndAssert();

		// Wait until timeout or un-publish button is visible
		Locator unpublish = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Unpublish edition"));
		unpublish.waitFor();
		assertThat(unpublish.isVisible()).isTrue();
		unpublish.click();

		// Log out as teacher
		logOutAs("CSE Teacher 1");

		// Assert that the edition is invisible
		assertThat(course.isVisible()).isFalse();
		logInAs("csestudent1", "csestudent1", "CSE Student 1");
		assertThat(course.isVisible()).isFalse();
		logOutAs("CSE Student 1");
	}

	/**
	 * Navigates from the homepage to the setup pane of CSE1100 NOW, and asserts on the individual steps.
	 */
	protected void navigateToSetupPaneAndAssert() {
		Locator course = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("CSE1100"));
		assertThat(course.isVisible()).isTrue();
		course.click();

		// Wait until timeout or edition name is visible
		Locator editionLocator = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("NOW"));
		editionLocator.waitFor();
		assertThat(editionLocator.isVisible()).isTrue();
		editionLocator.click();

		// Wait until timeout or setup button is visible
		Locator setup = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Setup"));
		setup.waitFor();
		assertThat(setup.isVisible()).isTrue();
		setup.click();
	}

}
