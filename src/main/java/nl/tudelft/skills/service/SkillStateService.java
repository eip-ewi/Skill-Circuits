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
package nl.tudelft.skills.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.*;

/**
 * Responsible for determining whether skills are unlocked or completed.
 */
@Service
@AllArgsConstructor
public class SkillStateService {

	///
    /// A skill in unlocked if:
    ///  - Any of its tasks are completed
    ///  - **or**, all its essential parents are completed **and** all its parents are unlocked
    ///
    /// Completion implies unlock:
    ///  - For empty skills, unlock is sufficient and necessary to completion by definition
    ///  - For non-empty skills, all tasks completed implies any task completed
    ///
    /// Note that we use the abstract skill's parents to determine this,
    /// not the actual parents of an external skill's reference.
    ///
	public boolean isSkillUnlocked(AbstractSkill abstractSkill, Set<Long> completedTaskIds, Set<Long> revealedSkillIds) {
        if (abstractSkill.getColumn() == null) {
            return false;
        }

		List<Task> tasks = switch (abstractSkill) {
			case ExternalSkill externalSkill -> externalSkill.getSkill().getTasks();
			case Skill skill -> skill.getTasks();
			default -> Collections.emptyList(); // Unreachable
		};
		if (tasks.stream().anyMatch(task -> isTaskOrAnySubtaskCompleted(task, completedTaskIds))) {
			return true;
		}

		boolean allEssentialParentsCompleted = abstractSkill.getParents().stream()
				.filter(AbstractSkill::isEssential)
				.allMatch(parent -> isSkillCompleted(parent, completedTaskIds, revealedSkillIds));
		if (!allEssentialParentsCompleted) {
			return false;
		}

        if (abstractSkill instanceof Skill skill && skill.isHidden() && !revealedSkillIds.contains(skill.getId())) {
            return false;
        }

		return abstractSkill.getParents().stream()
				.allMatch(parent -> isSkillUnlocked(parent, completedTaskIds, revealedSkillIds));
	}

	///
    /// A skill is completed if:
    ///  - It has tasks and all its tasks are completed
    ///  - **or**, it has no tasks and it is unlocked
    ///
    /// External skills are evaluated in the context of the reference.
    ///
	public boolean isSkillCompleted(AbstractSkill abstractSkill, Set<Long> completedTaskIds, Set<Long> revealedSkillIds) {
        List<Task> tasks = switch (abstractSkill) {
			case ExternalSkill externalSkill -> externalSkill.getSkill().getTasks();
			case Skill skill -> skill.getTasks();
			default -> Collections.emptyList(); // Unreachable
		};
		if (tasks.isEmpty()) {
			return isSkillUnlocked(abstractSkill, completedTaskIds, revealedSkillIds);
		}
		return tasks.stream().allMatch(task -> isTaskCompleted(task, completedTaskIds));
	}

	public boolean isTaskOrAnySubtaskCompleted(Task task, Set<Long> completedTaskIds) {
		return switch (task) {
			case RegularTask regularTask -> isTaskCompleted(task, completedTaskIds);
			case ChoiceTask choiceTask ->
				choiceTask.getTasks().stream().anyMatch(info -> completedTaskIds.contains(info.getId()));
			default -> false; // Unreachable
		};
	}

	public boolean isTaskCompleted(Task task, Set<Long> completedTaskIds) {
		return switch (task) {
			case RegularTask regularTask -> completedTaskIds.contains(regularTask.getTaskInfo().getId());
			case ChoiceTask choiceTask ->
				choiceTask.getTasks().stream().filter(info -> completedTaskIds.contains(info.getId()))
						.count() >= choiceTask.getMinTasks();
			default -> false; // Unreachable
		};
	}

}
