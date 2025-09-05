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

import java.util.List;
import java.util.function.Function;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public record LocatorLocators(Locator locator) implements Locators {

	public LocatorLocators apply(Function<Locator, Locator> f) {
		return new LocatorLocators(f.apply(locator));
	}

	public List<LocatorLocators> all() {
		return locator.all().stream().map(LocatorLocators::new).toList();
	}

	public LocatorLocators withChild(LocatorLocators locator) {
		return new LocatorLocators(this.locator.filter(new Locator.FilterOptions().setHas(locator.locator)));
	}

	public String text() {
		return locator.textContent();
	}

	public void click() {
		locator.click();
	}

	public void hover() {
		locator.hover();
	}

	public void hover(double x, double y) {
		locator.hover(new Locator.HoverOptions().setPosition(x, y));
	}

	public void fill(String value) {
		locator.fill(value);
	}

	public boolean isVisible() {
		return locator.isVisible();
	}

	public void waitFor() {
		locator.waitFor();
	}

	public void tryWaitFor() {
		try {
			locator.waitFor();
		} catch (TimeoutError ignored) {
		}
	}

	public void waitForDetach() {
		locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
	}

	@Override
	public Locator getByCss(String css) {
		return locator.locator(css);
	}

	@Override
	public Locator getByRole(AriaRole role, String text) {
		return locator.getByRole(role, new Locator.GetByRoleOptions().setName(text));
	}

	@Override
	public Locator getByText(String text) {
		return locator.getByText(text);
	}

	@Override
	public Locator getByLabel(String text) {
		return locator.getByLabel(text);
	}

	@Override
	public Page page() {
		return locator.page();
	}
}
