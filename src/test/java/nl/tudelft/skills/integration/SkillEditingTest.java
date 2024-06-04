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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SkillEditingTest extends IntegrationTest {
	@Test
	void testCancelEditing() {
		String editionName = getActiveEdition(oopCourse).name();
		logInAs(teacherUserInfo);
		closeChangelogBoxIfOpen();

		navigateToEditionSetup(oopCourse.code(), editionName);
		page.locator("#close-edition-setup-sidebar").click();
		clickAndWaitForPageLoad(page
				.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Proof Techniques")).first());
		var blocks = page.locator(".circuit").locator(".block").all();
		for (var block : blocks) {
			var taskListLocator = block.locator(".items").first().locator(".item");
			int taskListSize = taskListLocator.all().size();
			block.hover();
			block.locator(".block__edit").first().click();
			clickAndWaitForPageLoad(
					page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")));

			assertThat(taskListLocator.all().size()).isEqualTo(taskListSize);
		}
	}
}
