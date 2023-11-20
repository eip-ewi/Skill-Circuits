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
		clickAndWaitForPageLoad(logIn);

		// Check redirection to login page
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Login")).isVisible())
				.isTrue();
		assertThat(page.title()).isEqualTo("Login");

		// Login as teacher
		page.getByLabel("Username").fill(teacherUserInfo.userName());
		page.getByLabel("Password").fill(teacherUserInfo.password());
		clickAndWaitForPageLoad(
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")));

		// Wait until timeout or user dropdown is visible
		Locator userDropdown = page.getByText(teacherUserInfo.displayName());
		userDropdown.waitFor();
		assertThat(userDropdown.isVisible()).isTrue();

		// Check redirection to homepage
		assertThat(page
				.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Welcome to Skill Circuits"))
				.isVisible()).isTrue();
		assertThat(page.title()).isEqualTo("Skill Circuits");

		// Log out again
		userDropdown.click();
		assertThat(page.getByText("Your Profile").isVisible()).isTrue();
		assertThat(page.getByText("Inventory").isVisible()).isTrue();
		Locator logOut = page.getByText("Logout");
		assertThat(logOut.isVisible()).isTrue();
		clickAndWaitForPageLoad(logOut);

		// Wait until timeout or log in is visible again
		logIn.waitFor();
		assertThat(logIn.isVisible()).isTrue();
		assertThat(userDropdown.isVisible()).isFalse();

		// Check that it is still on the homepage
		assertThat(page
				.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Welcome to Skill Circuits"))
				.isVisible()).isTrue();
		assertThat(page.title()).isEqualTo("Skill Circuits");
	}

	/**
	 * Publishes and un-publishes an edition, and asserts on the individual steps. Also asserts that the
	 * edition is (in)visible to the student/visible on the homepage. This test uses edition Object-Oriented
	 * Programming (CSE1100) - NOW.
	 */
	@Test
	void testPublishUnpublishEdition() {
		navigateTo("");
		String editionName = getActiveEdition(oopCourse).name();
		logInAs(teacherUserInfo);

		// Ensure that all other editions are not published so that this does not interfere with the test
		for (EditionInfo edition : oopCourse.editions()) {
			if (!edition.name().equals(editionName)) {
				setEditionState(false, oopCourse.code(), edition.name());
			}
		}

		// Check if the edition is published or not
		navigateToEditionSetup(oopCourse.code(), editionName);

		// The name is case-insensitive, so this matches on both "Unpublish edition" and "Publish edition"
		Locator publishBtn = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("publish edition"));
		boolean published = publishBtn.innerText().contains("Unpublish");

		// Close setup pane
		page.locator("#close-edition-setup-sidebar").click();

		// Log out to check visibility corresponding to whether the edition is published/unpublished
		logOutAs(teacherUserInfo);

		// If it is currently published, unpublish it and publish it again, and vice versa
		Locator course = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName(oopCourse.code()));
		if (published) {
			// Unpublish the edition
			assertEditionVisible(course);
			logInAs(teacherUserInfo);
			togglePublishUnpublish(oopCourse.code(), getActiveEdition(oopCourse).name());
			assertPublishButtonLabel(false);
			logOutAs(teacherUserInfo);

			// Publish the edition
			assertEditionInvisible(course);
			logInAs(teacherUserInfo);
			togglePublishUnpublish(oopCourse.code(), getActiveEdition(oopCourse).name());
			assertPublishButtonLabel(true);
			logOutAs(teacherUserInfo);

			assertEditionVisible(course);
		} else {
			// Publish the edition
			assertEditionInvisible(course);
			logInAs(teacherUserInfo);
			togglePublishUnpublish(oopCourse.code(), getActiveEdition(oopCourse).name());
			assertPublishButtonLabel(true);
			logOutAs(teacherUserInfo);

			// Unpublish the edition
			assertEditionVisible(course);
			logInAs(teacherUserInfo);
			togglePublishUnpublish(oopCourse.code(), getActiveEdition(oopCourse).name());
			assertPublishButtonLabel(false);
			logOutAs(teacherUserInfo);

			assertEditionInvisible(course);
		}
	}

	/**
	 * Asserts whether the button in the edition setup pane has the correct label (corresponding to the
	 * publish state). If it is published, it should contain "Unpublish", if it is not, then it should not
	 * contain "Unpublish".
	 *
	 * @param published Whether the edition is currently published.
	 */
	protected void assertPublishButtonLabel(boolean published) {
		openEditionSetupOnCurrentPage();
		Locator publishBtn = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("publish edition"));
		assertThat(publishBtn.innerText().contains("Unpublish")).isEqualTo(published);
		page.locator("#close-edition-setup-sidebar").click();
	}

	/**
	 * Asserts that the course/edition is visible when not logged in and when the user is logged in as
	 * student. This method assumes that the user is not logged in initially.
	 *
	 * @param course The Locator for the course heading on the homepage.
	 */
	protected void assertEditionVisible(Locator course) {
		// Assert that the course is visible not logged in
		assertThat(course.isVisible()).isTrue();

		// Assert that the edition is visible when logged in as student
		logInAs(studentUserInfo);
		assertThat(course.isVisible()).isTrue();
		clickAndWaitForPageLoad(course);
		Locator editionHeader = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
				.setName(oopCourse.name() + " - " + getActiveEdition(oopCourse).name()));
		editionHeader.waitFor();
		assertThat(editionHeader.isVisible()).isTrue();
		logOutAs(studentUserInfo);
	}

	/**
	 * Asserts that the course/edition is not visible when not logged in and also not when the user is logged
	 * in as student. This method assumes that the user is not logged in initially.
	 *
	 * @param course The Locator for the course heading on the homepage.
	 */
	protected void assertEditionInvisible(Locator course) {
		assertThat(course.isVisible()).isFalse();

		logInAs(studentUserInfo);
		assertThat(course.isVisible()).isFalse();
		logOutAs(studentUserInfo);
	}

}
