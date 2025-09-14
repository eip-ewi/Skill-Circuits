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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public record PageLocators(Page page) implements Locators {

	@Override
	public Locator getByCss(String css) {
		return page.locator(css);
	}

	@Override
	public Locator getByRole(AriaRole role, String text) {
		return page.getByRole(role, new Page.GetByRoleOptions().setName(text));
	}

	@Override
	public Locator getByText(String text) {
		return page.getByText(text);
	}

	@Override
	public Locator getByLabel(String text) {
		return page.getByLabel(text);
	}

}
