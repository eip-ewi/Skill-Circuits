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

import static nl.tudelft.skills.e2e.User.cseteacher1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CheckpointTest extends EndToEndTest
		implements BasicScripts.WithBasicScripts, EditionScripts.WithEditionScripts {

	@Test
	@DisplayName("Checkpoint total time updates when a task's time is changed")
	public void checkpointTotalTimeUpdatesWhenTaskTimeIsChanged() {
		basic().logIn(cseteacher1);

		Edition edition = edition().findAnyManagingEdition();

		String uniqueId = UUID.randomUUID().toString().substring(0, 8);
		String submoduleName = "Test submodule " + uniqueId;
		String skillName = "Test skill " + uniqueId;
		String taskName = "Test task " + uniqueId;
		String checkpointName = "Test checkpoint " + uniqueId;

		boolean createdSubmodule = edition().submodules(edition).isEmpty();
		if (createdSubmodule) {
			edition().addSubmodule(edition, submoduleName);
		}

		basic().makeEditor();
		edition().enterFirstSubmodule(edition);

		page().waitForURL("**/modules/*");
		locators().query(".circuit").waitFor();

		edition().addSkill(skillName);
		edition().addTask(skillName, taskName, 30);
		edition().addCheckpoint(checkpointName);
		edition().addCheckpointToSkill(checkpointName, skillName);
		int time = edition().getCheckpointTime(checkpointName);
		assertEquals(30, time);

		edition().deleteSkill(skillName);
		edition().deleteCheckpoint(checkpointName);
		if (createdSubmodule) {
			edition().deleteSubmodule(edition, submoduleName);
		}
	}

}
