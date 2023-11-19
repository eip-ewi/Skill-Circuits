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
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public abstract class IntegrationTest {

	private static Playwright playwright;
	private static Browser browser;
	private static Properties properties;

	protected BrowserContext context;
	protected Page page;

	protected record UserInfo(String userName, String password, String displayName) {
	}

	protected record EditionInfo(String name, boolean active) {
	}

	protected record CourseInfo(String name, String code, Set<EditionInfo> editions) {
	}

	protected UserInfo teacherUserInfo = new UserInfo("cseteacher1", "cseteacher1",
			"CSE Teacher 1");
	protected UserInfo studentUserInfo = new UserInfo("csestudent1", "csestudent1",
			"CSE Student 1");
	protected CourseInfo oopCourse = new CourseInfo("Object-Oriented Programming", "CSE1100", Set.of(
			new EditionInfo("NOW", true), new EditionInfo("19/20", false)));
	protected CourseInfo adsCourse = new CourseInfo("Algorithms & Datastructures", "CSE1305", Set.of(
			new EditionInfo("NOW", true), new EditionInfo("19/20", false)));

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
		page.waitForLoadState(LoadState.DOMCONTENTLOADED);
	}

	/**
	 * Clicks the given locator and waits for the page to load. This should be used if the click of a button
	 * results in the redirection to another url. Otherwise, the page may not be loaded fully before the next
	 * command.
	 *
	 * @param toClick The locator to click.
	 */
	protected void clickAndWaitForPageLoad(Locator toClick) {
		toClick.click();
		page.waitForLoadState(LoadState.DOMCONTENTLOADED);
	}

	/**
	 * Gets an active edition for the given course. Usually, there should be exactly one active edition. This
	 * method assumes that there is at least one.
	 *
	 * @param  courseInfo The course information.
	 * @return            An active edition for the given course.
	 */
	protected EditionInfo getActiveEdition(CourseInfo courseInfo) {
		return courseInfo.editions().stream().filter(EditionInfo::active).findFirst().get();
	}

	/**
	 * Log in with a specific username and password.
	 *
	 * @param user The user information.
	 */
	protected void logInAs(UserInfo user) {
		clickAndWaitForPageLoad(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Login")));
		page.getByLabel("Username").fill(user.userName());
		page.getByLabel("Password").fill(user.password());
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();

		// Wait until timeout or user dropdown is visible
		page.getByText(user.displayName()).waitFor();
	}

	/**
	 * Log out. The username is needed to open the log-out menu.
	 *
	 * @param user The user information.
	 */
	protected void logOutAs(UserInfo user) {
		page.getByText(user.displayName()).click();
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
	protected void navigateToEditionSetup(String courseCode, String edition) {
		clickAndWaitForPageLoad(
				page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(courseCode)));
		// Click edition name
		Locator editionLocator = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName(edition));
		clickAndWaitForPageLoad(editionLocator);
		// Click setup button
		Locator setup = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Setup"));
		setup.click();
		// Wait until setup menu is visible
		page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Edition setup")).waitFor();
	}

	/**
	 * Performs the actions to toggle whether an edition is published or unpublished. This method assumes that
	 * the correct page (homepage) is open, that the user is not logged in.
	 *
	 * @param courseCode The code of the course to (un)publish an edition in.
	 * @param edition    The name of the edition to (un)publish.
	 */
	protected void togglePublishUnpublish(String courseCode, String edition) {
		logInAs(teacherUserInfo);
		navigateToEditionSetup(courseCode, edition);
		// Wait until timeout or (un)publish button is visible
		// Locator matches on both "Unpublish" and "Publish"
		Locator publish = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("publish edition"));
		clickAndWaitForPageLoad(publish);
		logOutAs(teacherUserInfo);
	}
}
