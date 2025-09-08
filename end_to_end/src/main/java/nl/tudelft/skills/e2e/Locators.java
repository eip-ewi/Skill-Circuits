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

import org.intellij.lang.annotations.Language;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public interface Locators {

	default LocatorLocators query(@Language("JQuery-CSS") String css) {
		return new LocatorLocators(getByCss(css));
	}

	default LocatorLocators button(String text) {
		return new LocatorLocators(getByRole(AriaRole.BUTTON, text));
	}

	default LocatorLocators link(String text) {
		return new LocatorLocators(getByRole(AriaRole.LINK, text));
	}

	default LocatorLocators textbox(String text) {
		return new LocatorLocators(getByRole(AriaRole.TEXTBOX, text));
	}

	default LocatorLocators heading(String text) {
		return new LocatorLocators(getByRole(AriaRole.HEADING, text));
	}

	default LocatorLocators text(String text) {
		return new LocatorLocators(getByText(text));
	}

	default LocatorLocators label(String text) {
		return new LocatorLocators(getByLabel(text));
	}

	Locator getByCss(String css);

	Locator getByRole(AriaRole role, String text);

	Locator getByText(String text);

	Locator getByLabel(String text);

	Page page();

}
