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
package nl.tudelft.skills.dto.view;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.annotation.Nullable;
import nl.tudelft.skills.dto.view.circuit.module.ModuleLevelTaskView;
import nl.tudelft.skills.enums.TaskType;

public record BookmarkListView(
		long id,
		String name,
		LocalDateTime lastModified,
		List<BookmarkListSkillView> skills,
		List<BookmarkListTaskView> tasks,

		@Nullable Long skill) {

	public record BookmarkListSkillView(
			long id,
			String name,
			String qualifiedName,
			boolean essential,
			List<ModuleLevelTaskView> items) {
		public String getBlockType() {
			return "skill";
		}

		public boolean getExternal() {
			return false;
		}
	}

	public interface BookmarkListTaskView {
		String name();

		String qualifiedName();

		String getTaskType();
	}

	public record BookmarkListTaskInfoView(
			long infoId,
			String name,
			TaskType type,
			int time,
			@Nullable String link,
			boolean completed,
			String qualifiedName) implements BookmarkListTaskView {
		@Override
		public String getTaskType() {
			return "regular";
		}
	}

	public record BookmarkListChoiceTaskView(
			long id,
			String name,
			int minTasks,
			List<BookmarkListSubtaskView> tasks,
			String qualifiedName) implements BookmarkListTaskView {
		@Override
		public String getTaskType() {
			return "choice";
		}
	}

	public record BookmarkListSubtaskView(
			long infoId,
			String name,
			TaskType type,
			int time,
			@Nullable String link,
			boolean completed) {
	}

}
