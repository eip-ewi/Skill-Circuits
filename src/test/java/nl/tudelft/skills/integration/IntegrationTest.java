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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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
		page = context.newPage();
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
	 * @param user     The username.
	 * @param password The password.
	 */
	protected void logInAs(String user, String password) {
		navigateTo("");
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Login")).click();
		page.getByLabel("Username").fill(user);
		page.getByLabel("Password").fill(password);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
	}

}
