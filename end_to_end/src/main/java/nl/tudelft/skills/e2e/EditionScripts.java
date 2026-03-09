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

	public Edition findEditionByName(String courseName, String editionName) {
		session.navigate("/");

		LocatorLocators editions = locators.query(".editions").apply(Locator::first);
		editions.query(".edition").apply(Locator::first).waitFor();
		List<LocatorLocators> allEditions = editions.query(".edition").all();

		return allEditions.stream()
				.filter(edition -> {
					String course = edition.query("h3 > :first-child").text();
					String name = edition.query("h3 > :last-child").text();
					return course.equals(courseName) && name.equals(editionName);
				})
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

	public void addCheckpoint(String name) {
		// Open the checkpoints panel
		locators.button("Open checkpoints panel").click();

		// Wait for panel to open and click Add checkpoint button
		LocatorLocators checkpointsPanel = locators.query(".panel").withChild(locators.heading("Checkpoints"));
		checkpointsPanel.waitFor();

		checkpointsPanel.button("Add checkpoint").click();

		// Wait for the editing form to appear and fill in the name
		LocatorLocators nameInput = locators.query(".checkpoint .edit input[aria-label='Name']").apply(Locator::first);
		nameInput.fill(name);

		checkpointsPanel.button("Stop editing").click();
		checkpointsPanel.waitFor();

		checkpointsPanel.button("Close panel").click();
		checkpointsPanel.waitFor();
	}

	public int checkCheckpointTime() {
		String time = locators.query(".checkpoint .time-estimate")
				.apply(Locator::first)
				.text();
		String[] numbers = time.split(" ");

		return Integer.parseInt(numbers[0].substring(0, numbers[0].length() - 1)) * 60 + Integer.parseInt(numbers[1]);
	}

//	public void changeCheckpointTime(String skill) {
//		LocatorLocators wrapper = locators.query(".block-wrapper")
//				.withChild(locators.heading(skill));
//		wrapper.hover();
//		wrapper.query(".controls").button("Edit").click();
//
//		// Find the first task's time input and increase it by 1
//		LocatorLocators timeInput = locators.query(".task").apply(Locator::first)
//				.query("input[name='time']");
//		timeInput.waitFor();
//
//		String currentValue = timeInput.locator().inputValue();
//		int currentTime = currentValue.isEmpty() ? 0 : Integer.parseInt(currentValue);
//		timeInput.fill(String.valueOf(currentTime + 1));
//
//		// Click Stop editing to save changes
//		wrapper.hover();
//		wrapper.query(".controls").button("Stop editing").click();
//	}

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

	public void addColumn(){
		locators.button("+Column").apply(Locator::first).click();
	}

	public void addSkillToSubmodule(String skill) {
		LocatorLocators skillInTray = locators.query(".panel").withChild(locators.heading("Tray"))
				.query(".block").heading(skill);

		skillInTray.hover();

		session.page().mouse().down();
		locators.query(".header").hover();
		locators.query(".column").hover();
		session.page().mouse().up();
		LocatorLocators created = locators.query(".block-wrapper").withChild(locators.text("New skill"));
		created.hover();
		created.hover(1, 1);
		created.query(".controls").button("Edit").click();
		locators.label("Edit submodule name").fill(skill);
		locators.query(".controls").button("Stop editing").click();
	}

	public void openEditing(String skill) {
		LocatorLocators wrapper = locators.query(".block-wrapper")
				.withChild(locators.heading(skill));
		wrapper.hover();
		wrapper.query(".controls").button("Edit").click();
	}

	public void createTask(String skill, String taskName, int time) {
		openEditing(skill);

		locators.button("Create a new task").click();

		LocatorLocators task = locators.query(".task").withChild(locators.label("Task name"));
		task.waitFor();
		task.label("Task name").fill(taskName);
		task.query("input[name='time']").fill(String.valueOf(time));

		locators.button("Stop editing").click();
	}

	public void editCheckpoint(String skill, String checkpoint){
		openEditing(skill);

		// Open the third select listbox (the checkpoint box)
		LocatorLocators thirdSelect = locators.query(".select").apply(l -> l.nth(2));
		thirdSelect.waitFor();
		thirdSelect.query("button").apply(Locator::first).click();

		// Click the option whose text contains the checkpoint string
		LocatorLocators option = thirdSelect.query(".options .option")
				.withChild(locators.text(checkpoint));
		option.waitFor();
		option.click();

		locators.button("Stop editing").click();
	}

	public void editCheckpointTime(String skill, int newTime) {
		openEditing(skill);

		// Find the first task's time input and set it to newTime
		LocatorLocators timeInput = locators.query(".task").apply(Locator::first)
				.query("input[name='time']");
		timeInput.waitFor();
		timeInput.fill(String.valueOf(newTime));

		locators.button("Stop editing").click();
	}

}
