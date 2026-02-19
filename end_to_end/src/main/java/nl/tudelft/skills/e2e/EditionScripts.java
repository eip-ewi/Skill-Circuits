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

import com.microsoft.playwright.Locator;

public final class EditionScripts {

	public interface WithEditionScripts {
		default EditionScripts edition() {
			SkillsSession session = session();
			return new EditionScripts(session, new PageLocators(session.page()));
		}

		SkillsSession session();
	}

	private final SkillsSession session;
	private final Locators locators;

	public EditionScripts(SkillsSession session, Locators locators) {
		this.session = session;
		this.locators = locators;
	}

	public boolean canSeeEdition(Edition edition) {
		session.navigate("/");

		LocatorLocators editionLocator = locators.query(".edition")
				.text(edition.course().name() + "\n" + edition.name());
		editionLocator.tryWaitFor();

		if (editionLocator.isVisible()) {
			return true;
		}

		session.navigate("/page/editions");
		editionLocator.tryWaitFor();

		return editionLocator.isVisible();
	}

	public Edition findAnyManagingEdition() {
		session.navigate("/");

		LocatorLocators editions = locators.query(".editions").apply(Locator::first);
		editions.query(".edition").apply(Locator::first).waitFor();
		List<LocatorLocators> managing = editions.query(".edition").all();

		return managing.stream()
				.filter(edition -> edition.query(".role").text().equals("Editor"))
				.map(edition -> new Edition(edition.query("h3 > :last-child").text(),
						new Course(edition.query("h3 > :first-child").text(), null)))
				.findAny().orElseThrow();
	}

	public void navigateTo(Edition edition) {
		session.navigate("/");

		LocatorLocators editionButton = locators.button(edition.course().name() + "\n" + edition.name());
		editionButton.click();
		editionButton.waitForDetach();
	}

	public void publish(Edition edition, boolean publish) {
		navigateTo(edition);

		locators.button("Open config panel").click();

		LocatorLocators publishButton = locators.button("Publish");

		if (publishButton.text().trim().equals("Publish") == publish) {
			publishButton.click();
		}

		locators.query(".heading").withChild(locators.heading("Course configuration")).query(".button")
				.click();
	}

	public void publish(Edition edition) {
		publish(edition, true);
	}

	public void unpublish(Edition edition) {
		publish(edition, false);
	}

	public List<String> modules(Edition edition) {
		navigateTo(edition);

		locators.button("Open modules panel").click();

		List<String> modules = locators.query(".panel .modules .module a").all().stream()
				.map(LocatorLocators::text).toList();

		locators.query(".heading").withChild(locators.heading("Modules")).query(".button:last-child").click();

		return modules;
	}

	public void addModule(Edition edition, String name) {
		navigateTo(edition);
		locators.button("Open modules panel").click();

		locators.query(".heading").withChild(locators.heading("Modules")).query(".button:first-child")
				.click();

		LocatorLocators newModule = locators.query(".panel .modules .module")
				.withChild(locators.label("Name"));
		newModule.label("Name").fill(name);
		newModule.button("Stop editing").click();

		locators.query(".heading").withChild(locators.heading("Modules")).query(".button:last-child").click();
	}

	public List<String> submodules(Edition edition) {
		navigateTo(edition);

		locators.query(".circuit").waitFor();

		return locators.query(".block-wrapper").all().stream()
				.map(submodule -> submodule.query(".heading").text().trim()).toList();
	}

	public void addSubmodule(Edition edition, String name) {
		if (modules(edition).isEmpty()) {
			addModule(edition, "Test module");
		}

		navigateTo(edition);

		locators.button("Open tray").click();

		LocatorLocators newSubmodule = locators.query(".panel").withChild(locators.heading("Tray"))
				.query(".block").heading("New submodule");
		newSubmodule.hover();
		session.page().mouse().down();
		locators.query(".header").hover();
		locators.query(".column").hover();
		session.page().mouse().up();

		LocatorLocators created = locators.query(".block-wrapper").withChild(locators.text("New submodule"));
		created.hover();
		created.hover(1, 1);
		created.query(".controls").button("Edit").click();
		locators.label("Edit submodule name").fill(name);
		locators.query(".controls").button("Stop editing").click();
	}

}
