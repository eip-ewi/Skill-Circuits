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

import java.util.List;

import nl.tudelft.skills.enums.TaskType;

public sealed interface TaskListTaskView {

    record Regular(
            long id,
            long infoId,
            String name,
            TaskType type,
            int time,
            String link,
            String skillName,
            String submoduleName,
            String moduleName,
            List<Long> paths) implements TaskListTaskView {
        @Override
        public String getTaskType() {
            return "regular";
        }
    }

    record Choice(
            long id,
            String name,
            int minTasks,
            String skillName,
            String submoduleName,
            String moduleName,
            List<ChoiceTaskChoiceView> tasks,
            List<Long> paths) implements TaskListTaskView {
        @Override
        public String getTaskType() {
            return "choice";
        }
    }

    record ChoiceTaskChoiceView(
            long infoId,
            String name,
            TaskType type,
            int time,
            String link) {
        public String getTaskType() {
            return "choice";
        }
    }

    List<Long> paths();

    String getTaskType();

    default String getItemType() {
        return "task";
    }

}
