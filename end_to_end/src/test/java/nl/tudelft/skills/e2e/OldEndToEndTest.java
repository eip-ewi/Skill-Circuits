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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.*;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import nl.tudelft.skills.e2e.docker.SkillCircuitsContainer;

public abstract class OldEndToEndTest {

	private static Playwright playwright;
	private static Browser browser;

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
	static void loadPropertiesAndLaunchBrowser() throws IOException {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(
				new BrowserType.LaunchOptions().setHeadless(EndToEndProperties.getBoolean("headless")));
		if (EndToEndProperties.getBoolean("use-docker")) {
			SkillCircuitsContainer.create();
		}
	}

	@AfterAll
	static void closeBrowser() {
		playwright.close();
	}

	@BeforeEach
	void createContextAndPage() {
		context = browser.newContext();
		context.setDefaultTimeout(10000);
		page = context.newPage();
		navigateTo("");
	}

	@AfterEach
	void closeContext() {
		context.close();
	}

	protected void takeScreenshotOf(Locator loc) {
		try {
			Path path = Path.of(EndToEndProperties.get("screenshots"));
			Files.createDirectories(path);
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
			Files.write(path.resolve("screenshot-" + timestamp + ".png"), loc.screenshot(),
					StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Navigates to a specified path prepending the base url, if it is not the current url.
	 *
	 * @param path The path to navigate to.
	 */
	protected void navigateTo(String path) {
		String baseUrl = EndToEndProperties.get("base-url");
		if (path.startsWith("http"))
			baseUrl = "";
		else if (!baseUrl.endsWith("/"))
			baseUrl += "/";

		if (!page.url().equals(baseUrl + path)) {
			page.navigate(baseUrl + path);
			page.waitForLoadState(LoadState.DOMCONTENTLOADED);
		}
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

	protected void clickAndWaitForClickedThingToDetach(Locator toClick) {
		Locator specific = page.getByText(toClick.innerText());
		toClick.click();
		specific.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
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
		navigateTo("");

		Locator logIn = page.getByRole(AriaRole.LINK,
				new Page.GetByRoleOptions().setName("Click here to log in"));
		clickAndWaitForPageLoad(logIn);

		// Check redirection to login page
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Log In")))
				.isVisible();

		// Login as teacher
		page.getByLabel("Username").fill(user.userName());
		page.getByLabel("Password").fill(user.password());
		clickAndWaitForPageLoad(
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")));

		// Close the changelog box if it is open
		closeChangelogBoxIfOpen();

		// Wait until timeout or user dropdown is visible
		Locator userDropdown = page.getByText(user.displayName());
		assertThat(userDropdown).isVisible();
	}

	/**
	 * Log out. The username is needed to open the log-out menu.
	 *
	 * @param user The user information.
	 */
	protected void logOutAs(UserInfo user) {
		// Log out again
		Locator userDropdown = page.getByText(user.displayName());
		userDropdown.click();
		Locator logOut = page.getByText("Log out");
		assertThat(logOut).isVisible();
		clickAndWaitForPageLoad(logOut);
	}

	/**
	 * On any page, if the changelog ("what's new") box is open, it is closed.
	 */
	protected void closeChangelogBoxIfOpen() {
		// Identify box by two main components: header and button
		Locator okayButtonLocator = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("OK"));
		Locator whatsNewHeader = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName("What's new"));

		if (okayButtonLocator.isVisible() && whatsNewHeader.isVisible()) {
			okayButtonLocator.click();

			// Wait until the box is hidden
			whatsNewHeader.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
			okayButtonLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
		}
	}

	/**
	 * If the course is not visible in the current homepage tab, switch to the other tab.
	 *
	 * @param course The locator for the course to check.
	 */
	protected void ifCourseHiddenSwitchToOtherHomepageTab(Locator course) {
		navigateTo("");

		// If it is not visible currently, check the other tab
		if (!course.isVisible()) {
			Locator yourCourses = page.getByRole(AriaRole.BUTTON,
					new Page.GetByRoleOptions().setName("Your Courses"));
			Locator availableCourses = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Available Courses"));

			// If no tabs are visible, that means no courses are visible at all
			if (!yourCourses.isVisible()) {
				return;
			}

			// Find current tab, assert on attributes and switch tabs
			Locator currentTab = yourCourses.getAttribute("data-active").equals("true") ? yourCourses
					: availableCourses;
			Locator otherTab = currentTab.equals(yourCourses) ? availableCourses : yourCourses;
			assertThat(currentTab).hasAttribute("data-active", "true");
			assertThat(otherTab).hasAttribute("data-active", "false");
			otherTab.click();
		}
	}

	/**
	 * Private utility method for navigating to the setup pane of a specific edition in a course.
	 *
	 * @param courseName The code of the course.
	 * @param edition    The name of the edition.
	 */
	protected void navigateToEditionSetup(String courseName, String edition) {
		navigateTo("");
		clickAndWaitForClickedThingToDetach(
				page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(courseName)));
		// Click edition name
		Locator editionLocator = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName(edition));
		clickAndWaitForClickedThingToDetach(editionLocator);
		openEditionSetupOnCurrentPage();
	}

	protected String navigateToFirstEditionWithEditRights() {
		navigateTo("");
		Locator currentEditions = page.locator(".editions")
				.filter(new Locator.FilterOptions()
						.setHas(page.getByRole(AriaRole.HEADING,
								new Page.GetByRoleOptions().setName("Current editions").setExact(false))));
		Locator buttonForFirstEdition = currentEditions.getByRole(AriaRole.BUTTON)
				.filter(new Locator.FilterOptions()
						.setHas(page.getByText("Editor")))
				.first();
		clickAndWaitForClickedThingToDetach(buttonForFirstEdition);
		return page.url();
	}

	protected String createOrNavigateToFirstModuleInEdition(String editionUrl) {
		navigateTo(editionUrl);
		List<Locator> blocks = page.locator(".circuit").locator(".block").all();
		if (blocks.isEmpty()) {
			createModuleIfNoneExists(editionUrl);
			createSubModuleIfNoneExists(editionUrl);
		}
		Locator block = page.locator(".circuit").locator(".block").first();
		clickAndWaitForClickedThingToDetach(block);
		return page.url();
	}

	private Locator openTray() {
		return openSidePanel("Open Tray", "Tray");
	}

	private Locator openSidePanel(String buttonName, String panelName) {
		Locator moduleButton = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName(buttonName));
		clickAndWaitForPageLoad(moduleButton);
		Locator panel = page.locator(".panel")
				.filter(new Locator.FilterOptions().setHas(
						page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(panelName))))
				.first();
		panel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
		return panel;
	}

	private void closeSidePanel(Locator panel) {
		clickAndWaitForPageLoad(panel.getByLabel("Close Panel"));
		panel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
	}

	private void createModuleIfNoneExists(String editionUrl) {
		navigateTo(editionUrl);
		Locator modulePanel = openSidePanel("Open Modules Panel", "Modules");

		List<Locator> modules = modulePanel.locator(".modules").locator(".module").all();
		if (!modules.isEmpty()) {
			closeSidePanel(modulePanel);
			return;
		}
		// Add a module
		clickAndWaitForPageLoad(modulePanel.getByLabel("Add Module"));

		// Confirm a module was created
		modulePanel.locator(".modules").locator(".module").first().waitFor();
		modules = modulePanel.locator(".modules").locator(".module").all();

		// Give it a name and save it
		modules.get(0).getByRole(AriaRole.TEXTBOX).fill("Module 1");
		clickAndWaitForPageLoad(modules.get(0).getByRole(AriaRole.BUTTON));
		closeSidePanel(modulePanel);
	}

	private void createSubModuleIfNoneExists(String editionUrl) {
		navigateTo(editionUrl);
		addNewItemFromTrayIfCircuitIsEmpty();
	}

	private Locator addNewItemFromTrayIfCircuitIsEmpty() {
		List<Locator> blocks = page.locator(".circuit").locator(".block").all();
		if (!blocks.isEmpty()) {
			return blocks.getFirst();
		}
		Locator tray = openTray();

		Locator newButton = tray.locator(".block").filter(
				new Locator.FilterOptions().setHasText("New")).first();
		assertThat(newButton).hasAttribute("draggable", "true");

		Locator columns = page.locator(".column");
		//newSubmoduleButton.dragTo(columns.first());
		newButton.hover();
		page.mouse().down();
		page.locator(".circuit").hover();
		columns.first().hover();
		page.mouse().up();

		page.locator(".circuit").locator(".block").waitFor();
		blocks = page.locator(".circuit").locator(".block").all();
		assertEquals(1, blocks.size());

		closeSidePanel(tray);
		return blocks.getFirst();
	}

	protected String createSkillAndTaskIfNoneExists(String moduleUrl) {
		navigateTo(moduleUrl);
		Locator newSkillBlock = addNewItemFromTrayIfCircuitIsEmpty();
		newSkillBlock.hover();

		Locator blockWrapper = page.locator(".circuit").locator(".block-wrapper").first();

		Locator editButton = blockWrapper.locator(".controls").getByLabel("Edit",
				new Locator.GetByLabelOptions().setExact(true));
		editButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
		clickAndWaitForPageLoad(editButton);

		takeScreenshotOf(blockWrapper);
		Locator nameField = blockWrapper.locator(".name");//AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("name").setExact(false));
		nameField.fill("Skill 1");

		Locator addTaskButton = blockWrapper.getByRole(AriaRole.BUTTON).getByText("Create a new task");
		clickAndWaitForPageLoad(addTaskButton);

		//        Locator task = blockWrapper.locator(".task").first();
		//        Locator taskName = task.locator(".name");
		//        taskName.fill("Task 1");

		Locator confirmButton = blockWrapper.getByRole(AriaRole.BUTTON).getByLabel("Stop editing",
				new Locator.GetByLabelOptions().setExact(false));
		clickAndWaitForPageLoad(confirmButton);

		return moduleUrl;
	}

	protected void navigateToFirstEditionWithCircuit() {
		navigateTo("");
		Locator currentEditions = page.locator(".editions")
				.filter(new Locator.FilterOptions()
						.setHas(page.getByRole(AriaRole.HEADING,
								new Page.GetByRoleOptions().setName("Current Editions").setExact(false))));
		List<Locator> buttonsForCurrentEditions = currentEditions.getByRole(AriaRole.BUTTON).all();
		for (Locator button : buttonsForCurrentEditions) {
			clickAndWaitForPageLoad(button);
			Locator circuit = page.locator("circuit").locator("block");
			circuit.click();
		}
	}

	/**
	 * Opens the edition setup pane, assuming the edition page is open.
	 */
	protected void openEditionSetupOnCurrentPage() {
		// Click setup button
		Locator setup = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Setup"));
		setup.click();
		// Wait until setup menu is visible
		page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Edition setup")).waitFor();
	}

	/**
	 * Given a desired state, makes an edition published, if it is not yet published, or unpublished if it is
	 * not yet unpublished. This assumes that the user is logged in as a teacher for the edition.
	 *
	 * @param publish    Whether to publish (true) or unpublish (false).
	 * @param courseName The name of the course to (un)publish an edition in.
	 * @param edition    The name of the edition to (un)publish.
	 */
	protected void setEditionState(boolean publish, String courseName, String edition) {
		navigateToEditionSetup(courseName, edition);

		// Check current publish state
		Locator publishBtn = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("publish edition"));
		boolean isPublished = publishBtn.innerText().contains("Unpublish");

		// If it is not the desired state (un)publish the edition
		if (isPublished && !publish || !isPublished && publish) {
			clickAndWaitForPageLoad(publishBtn);
		}
	}

	/**
	 * Performs the actions to toggle whether an edition is published or unpublished. This method assumes that
	 * the user is logged in as a teacher for the edition.
	 *
	 * @param courseName The name of the course to (un)publish an edition in.
	 * @param edition    The name of the edition to (un)publish.
	 */
	protected void togglePublishUnpublish(String courseName, String edition) {
		navigateToEditionSetup(courseName, edition);
		// Wait until timeout or (un)publish button is visible
		// Locator matches on both "Unpublish" and "Publish"
		Locator publish = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("publish edition"));
		clickAndWaitForPageLoad(publish);
	}

	/**
	 * Creates a new module. This method assumes that the user is logged in as a teacher for the edition.
	 *
	 * @param  courseName The name of the course.
	 * @param  edition    The name of the edition.
	 * @return            The name of the module (UUID).
	 */
	protected String createModule(String courseName, String edition) {
		String moduleName = UUID.randomUUID().toString();

		navigateToEditionSetup(courseName, edition);
		page.getByPlaceholder("New Module").click();
		page.getByPlaceholder("New Module").fill(moduleName);
		page.locator("#new-module-form").getByRole(AriaRole.BUTTON,
				new Locator.GetByRoleOptions().setName("create")).click();

		return moduleName;
	}
}
