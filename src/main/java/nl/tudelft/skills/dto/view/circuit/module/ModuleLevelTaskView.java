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
package nl.tudelft.skills.dto.view.circuit.module;

import nl.tudelft.skills.dto.view.circuit.ItemView;
import nl.tudelft.skills.enums.TaskType;

import java.util.List;

public sealed interface ModuleLevelTaskView extends ItemView {

    record Regular(
            long id,
            long infoId,
            String name,
            TaskType type,
            int time,
            String link,
            boolean completed,
            List<Long> paths
    ) implements ModuleLevelTaskView {
        @Override
        public String getTaskType() {
            return "regular";
        }
    }

    record Choice(
            long id,
            String name,
            int minTasks,
            List<ChoiceTaskChoiceView> tasks,
            List<Long> paths
    ) implements ModuleLevelTaskView {
        @Override
        public boolean completed() {
            return tasks.stream().filter(ChoiceTaskChoiceView::completed).count() >= minTasks;
        }
        public boolean getCompleted() {
            return completed();
        }
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
            String link,
            boolean completed
    ) {
        public String getTaskType() {
            return "choice";
        }
    }

    List<Long> paths();

    String getTaskType();

    @Override
    default boolean locked() {
        return false;
    }

    default boolean getLocked() {
        return locked();
    }

    @Override
    default String getItemType() {
        return "task";
    }

}
