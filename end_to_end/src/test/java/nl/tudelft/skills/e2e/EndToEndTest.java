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

import org.junit.jupiter.api.BeforeEach;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import nl.tudelft.skills.e2e.docker.SkillCircuitsContainer;

public abstract class EndToEndTest {

	private final SkillsBrowser browser;
	private SkillsSession session;
	private Locators locators;

	public EndToEndTest() {
		SkillsBrowser.Type browserType = EndToEndProperties.getOptional("browser.type")
				.map(SkillsBrowser.Type::valueOf).orElse(SkillsBrowser.Type.Chrome);
		this.browser = new SkillsBrowser(browserType, EndToEndProperties.getBoolean("browser.headless"));

		if (EndToEndProperties.getBoolean("use-docker")) {
			SkillCircuitsContainer.create();
		}
	}

	@BeforeEach
	public void initSession() {
		this.session = new SkillsSession(browser, EndToEndProperties.get("base-url"),
				EndToEndProperties.getDouble("default-timeout"));
		this.session.navigate("/");

		this.locators = new PageLocators(session.page());
	}

	public SkillsBrowser browser() {
		return browser;
	}

	public SkillsSession session() {
		return session;
	}

	public Page page() {
		return session.page();
	}

	public Locators locators() {
		return locators;
	}

	public LocatorAssertions assertThat(LocatorLocators locator) {
		return PlaywrightAssertions.assertThat(locator.locator());
	}

}
