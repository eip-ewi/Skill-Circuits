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
import com.microsoft.playwright.options.AriaRole;

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

	public void enterFirstSubmodule(Edition edition) {
		navigateTo(edition);
		locators.query(".circuit").waitFor();

		LocatorLocators firstSubmodule = locators.query(".block-wrapper").apply(Locator::first);
		firstSubmodule.waitFor();
		firstSubmodule.click();
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

	}

	public void openEditing(String skill) {
		LocatorLocators wrapper = locators.query(".block-wrapper")
				.withChild(locators.heading(skill));
		wrapper.hover();
		wrapper.query(".controls").button("Edit").click();
	}

	public void addSkill(String skill) {
		locators.button("Open tray").click();

		LocatorLocators newSkillBlock = locators.query(".panel")
				.withChild(locators.heading("Tray"))
				.query(".block").heading("New skill");

		newSkillBlock.waitFor();

		Locator targetColumn = locators.query(".column").apply(Locator::first).locator();

		newSkillBlock.locator().dragTo(targetColumn, new com.microsoft.playwright.Locator.DragToOptions().setForce(true));


		LocatorLocators newlyCreated = locators.query(".block-wrapper").withChild(locators.text("New skill"));
		newlyCreated.hover();
		newlyCreated.query(".controls").locator().locator("button[aria-label='Edit']").click();
		session.page().getByLabel("Edit skill name").fill(skill);
		locators.query(".controls").button("Stop editing").click();
		session.page().locator(".panel")
				.filter(new Locator.FilterOptions().setHasText("Tray"))
				.locator("button[aria-label='Close panel']")
				.click();
	}

	public void addTask(String skill, String task, int duration) {
		LocatorLocators skillWrapper = locators.query(".circuit .block-wrapper")
				.withChild(locators.query(".name").text(skill))
				.apply(Locator::first);

		skillWrapper.waitFor();
		skillWrapper.hover();
		skillWrapper.query(".controls").locator().locator("button[aria-label='Edit']").click();

		session.page().locator("button:has-text('Create a new task')")
				.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

		Locator newTaskWrapper = session.page().locator(".item-wrapper").last();

		newTaskWrapper.locator("input[name='item-name']").fill(task);
		newTaskWrapper.locator("input[name='time']").fill(String.valueOf(duration));

		newTaskWrapper.locator("input[name='time']").press("Enter");
		locators.query(".controls").button("Stop editing").click();
	}

	public void addCheckpoint(String checkpoint) {
		locators.button("Open checkpoints panel").click();
		locators.button("Add checkpoint").click();
		Locator newCheckpoint = session.page().locator(".checkpoint").first();
		newCheckpoint.locator("input[name='name']").fill("Test checkpoint");
		locators.button("Stop editing").click();
		session.page().locator(".panel")
				.filter(new Locator.FilterOptions().setHasText("Checkpoints"))
				.locator("button[aria-label='Close panel']")
				.click();
	}

	public void addCheckpointToSkill(String checkpoint, String skill) {
		LocatorLocators skillWrapper = locators.query(".circuit .block-wrapper")
				.withChild(locators.query(".name").text(skill))
				.apply(Locator::first);

		skillWrapper.waitFor();
		skillWrapper.hover();
		skillWrapper.query(".controls").locator().locator("button[aria-label='Edit']").click();
		Locator editingBlock = session.page().locator(".circuit .block-wrapper[data-editing='true']").first();

		Locator checkpointDropdown = editingBlock.locator(".heading div[role='listbox']").nth(1);

		checkpointDropdown.locator("button.button")
				.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

		Locator targetOption = checkpointDropdown.locator(".options button[role='option']")
				.filter(new Locator.FilterOptions().setHasText(checkpoint));

		targetOption.evaluate("node => node.click()");

		locators.query(".controls").button("Stop editing").click();
	}

	public int getCheckpointTime(String checkpoint) {
		Locator lectureInfoBlock = session.page().locator(".info")
				.filter(new Locator.FilterOptions().setHasText(checkpoint))
				.locator(".time-estimate");
		lectureInfoBlock.waitFor();
		String time = lectureInfoBlock.textContent().trim();

		int totalMinutes = 0;
		String[] parts = time.split(" ");
		for (String part : parts) {
			if (part.endsWith("h")) {
				int hours = Integer.parseInt(part.substring(0, part.length() - 1));
				totalMinutes += hours * 60;
			} else if (part.endsWith("m")) {
				int minutes = Integer.parseInt(part.substring(0, part.length() - 1));
				totalMinutes += minutes;
			}
		}

		return totalMinutes;
	}

	public void deleteSkill(String skill) {
		LocatorLocators skillWrapper = locators.query(".circuit .block-wrapper")
				.withChild(locators.query(".name").text(skill))
				.apply(Locator::first);

		skillWrapper.waitFor();
		skillWrapper.hover();
		skillWrapper.query(".controls").locator().locator("button[aria-label='Delete']").click();
		Locator dialog = session.page().locator("dialog[open]");

		// 4. Click the final "Delete" button to confirm
		dialog.locator("button")
				.filter(new Locator.FilterOptions().setHasText("Delete"))
				.click();
	}

	public void deleteCheckpoint(String checkpoint) {
		locators.button("Open checkpoints panel").click();
		Locator checkpointRow = session.page().locator(".checkpoint")
				.filter(new Locator.FilterOptions().setHasText(checkpoint))
				.first();

		// 2. Click the initial "Delete checkpoint" trash icon
		checkpointRow.locator("button[aria-label='Delete checkpoint']").click();

		// 3. Locate the confirmation dialog that appears inside that specific row
		Locator dialog = session.page().locator("dialog[open]");

		// 4. Click the final "Delete" button to confirm
		dialog.locator("button")
				.filter(new Locator.FilterOptions().setHasText("Delete"))
				.click();

		session.page().locator(".panel")
				.filter(new Locator.FilterOptions().setHasText("Checkpoints"))
				.locator("button[aria-label='Close panel']")
				.click();
	}
}
