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

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public abstract class IntegrationTest {

	private static Playwright playwright;
	private static Browser browser;
	private static Properties properties;

	protected BrowserContext context;
	protected Page page;

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch();
	}

	@BeforeAll
	static void loadProperties() throws IOException {
		properties = new Properties();
		properties.load(IntegrationTest.class.getClassLoader().getResourceAsStream("integration.properties"));
	}

	@AfterAll
	static void closeBrowser() {
		playwright.close();
	}

	@BeforeEach
	void createContextAndPage() {
		context = browser.newContext();
		page = context.newPage(); // TODO set default timeout
	}

	@AfterEach
	void closeContext() {
		context.close();
	}

	/**
	 * Navigates to a specified path, prepending the base url.
	 *
	 * @param path The path to navigate to.
	 */
	protected void navigateTo(String path) {
		String baseUrl = properties.getProperty("base-url");
		if (!baseUrl.endsWith("/"))
			baseUrl += "/";
		page.navigate(baseUrl + path);
	}

	/**
	 * Log in with a specific username and password.
	 *
	 * @param user        The username for logging in.
	 * @param password    The password.
	 * @param displayName The username displayed in the UI.
	 */
	protected void logInAs(String user, String password, String displayName) {
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Login")).click();
		page.getByLabel("Username").fill(user);
		page.getByLabel("Password").fill(password);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();

		// Wait until timeout or user dropdown is visible
		page.getByText(displayName).waitFor();
	}

	/**
	 * Log out. The username is needed to open the log-out menu.
	 *
	 * @param displayName The username displayed in the UI.
	 */
	protected void logOutAs(String displayName) {
		page.getByText(displayName).click();
		page.getByText("Logout").click();

		// Wait until timeout or log in is visible
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Login")).waitFor();
	}

	/**
	 * Private utility method for navigating from the homepage to the setup pane of a specific edition in a
	 * course.
	 *
	 * @param courseCode The code of the course.
	 * @param edition    The name of the edition.
	 */
	private void navigateToEditionSetup(String courseCode, String edition) {
		page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(courseCode)).click();
		// Wait until timeout or edition name is visible
		Locator editionLocator = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName(edition));
		editionLocator.waitFor();
		editionLocator.click();
		// Wait until timeout or setup button is visible
		Locator setup = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Setup"));
		setup.waitFor();
		setup.click();
	}

	/**
	 * Performs the actions to publish an edition. This method assumes that the correct page (homepage) is
	 * open, and that the user is not logged in.
	 *
	 * @param courseCode The code of the course to publish an edition in.
	 * @param edition    The name of the edition to publish.
	 */
	protected void publishEdition(String courseCode, String edition) {
		logInAs("cseteacher1", "cseteacher1", "CSE Teacher 1");
		navigateToEditionSetup(courseCode, edition);
		// Wait until timeout or publish button is visible
		Locator publish = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Publish edition"));
		publish.waitFor();
		publish.click();
		logOutAs("CSE Teacher 1");
	}

	/**
	 * Performs the actions to un-publish an edition.This method assumes that the correct page (homepage) is
	 * open, that the user is not logged in and that the edition was published previously.
	 *
	 * @param courseCode The code of the course to un-publish an edition in.
	 * @param edition    The name of the edition to un-publish.
	 */
	protected void unpublishEdition(String courseCode, String edition) {
		logInAs("cseteacher1", "cseteacher1", "CSE Teacher 1");
		navigateToEditionSetup(courseCode, edition);
		// Wait until timeout or un-publish button is visible
		Locator publish = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Unpublish edition"));
		publish.waitFor();
		publish.click();
		logOutAs("CSE Teacher 1");
	}

}
