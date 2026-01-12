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

public final class BasicScripts {

	public interface WithBasicScripts {
		default BasicScripts basic() {
			SkillsSession session = session();
			return new BasicScripts(session, new PageLocators(session.page()));
		}

		SkillsSession session();
	}

	private final SkillsSession session;
	private final Locators locators;

	public BasicScripts(SkillsSession session, Locators locators) {
		this.session = session;
		this.locators = locators;
	}

	public void logIn(User user) {
		session.navigate("/");

		locators.link("Click here to log in").click();

		locators.label("Username").fill(user.username());
		locators.label("Password").fill(user.password());
		locators.button("Log in").click();

		locators.query(".header").waitFor();
		locators.page().waitForTimeout(500);
		closeChangelog();
	}

	private void closeChangelog() {
		LocatorLocators okayButton = locators.button("OK");
		if (okayButton.isVisible()) {
			okayButton.click();
			okayButton.waitForDetach();
		}

	}

	public void logOut() {
		locators.query(".header > .button:last-child").click();
		locators.button("Log out").click();
	}

}
