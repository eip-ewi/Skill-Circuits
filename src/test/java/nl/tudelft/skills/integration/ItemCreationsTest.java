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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ItemCreationsTest extends IntegrationTest {

	@Test
	public void testCreateModule() {
		logInAs(teacherUserInfo);
		closeChangelogBoxIfOpen();
		String moduleName = createModule(oopCourse.code(), getActiveEdition(oopCourse).name());

		// Assert it being added to the list
		Locator linkLocator = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(moduleName));
		linkLocator.waitFor();
		assertThat(linkLocator.isVisible()).isTrue();

		// Assert it is visible in the circuit in student view
		page.locator("#close-edition-setup-sidebar").click();
		clickAndWaitForPageLoad(page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Enter student view")));
		clickAndWaitForPageLoad(
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Modules")));
		Locator moduleLocator = page.getByRole(AriaRole.LINK,
				new Page.GetByRoleOptions().setName(moduleName));
		moduleLocator.waitFor();
		assertThat(moduleLocator.isVisible()).isTrue();
	}

}
