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

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

public class SkillsSession implements AutoCloseable {

	private final BrowserContext context;
	private final Page page;

	public SkillsSession(SkillsBrowser browser, String baseUrl, double defaultTimeout) {
		context = browser.browser()
				.newContext(new Browser.NewContextOptions().setBaseURL(baseUrl).setViewportSize(1920, 1080));
		context.setDefaultTimeout(defaultTimeout);
		page = context.newPage();
	}

	public void navigate(String path) {
		page.navigate(path, new Page.NavigateOptions().setTimeout(30_000.0));
		page.waitForLoadState(LoadState.DOMCONTENTLOADED);
	}

	public Page page() {
		return page;
	}

	@Override
	public void close() {
		context.close();
	}

}
