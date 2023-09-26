/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
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
package nl.tudelft.skills.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;

public class ExpandedSkillTest extends IntegrationTest {

	@Test
	void testOpenCloseSkillOverlay() {
		navigateTo("");
		publishEdition("CSE1100", "NOW");
		logInAs("csestudent1", "csestudent1", "CSE Student 1");

		Locator course = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("CSE1100"));
		assertThat(course.isVisible()).isTrue();
		course.click();

		Locator module = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName("Proof Techniques"));
		module.waitFor();
		assertThat(module.isVisible()).isTrue();
		module.click();

		Locator skillBox = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Implication")
				.setExact(true));
		skillBox.waitFor();
		assertThat(skillBox.isVisible()).isTrue();

		Locator skillOverlay = page.getByRole(AriaRole.DIALOG);
		assertThat(skillOverlay.isVisible()).isFalse();

		// Click skill and check that the overlay appears
		skillBox.click();
		skillOverlay.waitFor();
		assertThat(skillOverlay.isVisible()).isTrue();

		// Click outside of box and check that the box disappears
		BoundingBox skillBoundingBox = skillOverlay.boundingBox();
		page.mouse().click(skillBoundingBox.x + skillBoundingBox.width + 10,
				skillBoundingBox.y + skillBoundingBox.height + 10);
		skillOverlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
		assertThat(skillOverlay.isVisible()).isFalse();

		logOutAs("CSE Student 1");
		navigateTo("");
		unpublishEdition("CSE1100", "NOW");
	}

	@Test
	void testCompleteTask() {
		navigateTo("");
		publishEdition("CSE1100", "NOW");
		logInAs("csestudent1", "csestudent1", "CSE Student 1");

		page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("CSE1100")).click();

		Locator module = page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName("Proof Techniques"));
		module.waitFor();
		module.click();

		Locator skillBlock = page.locator(".block")
				.filter(new Locator.FilterOptions().setHasText("Logic Basics Implication"));
		skillBlock.waitFor();

		List<Locator> outerTaskIcons = skillBlock.locator(".item__icon").all();
		// Sort by rendering position (x)
		outerTaskIcons.sort(Comparator.comparingDouble((Locator l) -> l.boundingBox().x));

		assertThat(skillBlock.locator(".completed").all()).hasSize(0);
		// For the test to work, at least one task needs to exist
		assertThat(outerTaskIcons).hasSizeGreaterThan(0);

		// Click skill
		Locator skillOverlay = page.getByRole(AriaRole.DIALOG);
		skillBlock.click();
		skillOverlay.waitFor();

		// Check that number of tasks and task types match up
		List<Locator> innerTaskElements = skillOverlay.getByRole(AriaRole.LISTITEM).all();
		List<Locator> innerTaskIcons = skillOverlay.locator(".task-icon").all();
		// Sort by rendering position (y)

		// TODO: This should be added back to check if the order of the icons is correctly matched up.
		//      Currently, it fails.
		/*
		 * innerTaskIcons.sort(Comparator.comparingDouble((Locator l) -> l.boundingBox().y));
		 * assertThat(innerTaskElements.size()).isEqualTo(outerTaskIcons.size()); int idx = 0; for (Locator
		 * innerTaskIcon : innerTaskIcons) { assertThat(innerTaskIcon.getAttribute("data-type"))
		 * .isEqualTo(outerTaskIcons.get(idx).getAttribute("data-type")); idx++; }
		 */

		// Complete a task and assert visual output
		assertThat(innerTaskElements.get(0).getAttribute("class")).doesNotContain("completed");
		innerTaskElements.get(0).locator(".item__button").click();
		assertThat(innerTaskElements.get(0).getAttribute("class")).contains("completed");
		assertThat(outerTaskIcons.get(0).getAttribute("class")).contains("completed");
		for (int i = 1; i < outerTaskIcons.size(); i++) {
			assertThat(outerTaskIcons.get(i).getAttribute("class")).doesNotContain("completed");
		}

		// Uncomplete the task again
		innerTaskElements.get(0).locator(".item__button").click();
		assertThat(skillBlock.locator(".completed").all()).hasSize(0);

		// Click outside of box
		BoundingBox skillBoundingBox = skillOverlay.boundingBox();
		page.mouse().click(skillBoundingBox.x + skillBoundingBox.width + 10,
				skillBoundingBox.y + skillBoundingBox.height + 10);
		skillOverlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));

		logOutAs("CSE Student 1");
		navigateTo("");
		unpublishEdition("CSE1100", "NOW");
	}

}
