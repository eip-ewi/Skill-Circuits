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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class SkillsBrowser implements AutoCloseable {

	private final Playwright playwright;
	private final Browser browser;

	public SkillsBrowser(Type type, boolean headless) {
		playwright = Playwright.create();

		BrowserType browserType = switch (type) {
			case Chrome -> playwright.chromium();
			case Firefox -> playwright.firefox();
			case Safari -> playwright.webkit();
		};

		browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(headless));
	}

	public Browser browser() {
		return browser;
	}

	@Override
	public void close() {
		playwright.close();
	}

	public enum Type {
		Chrome, Firefox, Safari;
	}

}
