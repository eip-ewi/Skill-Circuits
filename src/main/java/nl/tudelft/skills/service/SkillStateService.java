package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public boolean isSkillUnlockedFor(AbstractSkill abstractSkill, Set<Long> completedTaskIds) {
        List<Task> tasks = switch (abstractSkill) {
            case ExternalSkill externalSkill -> externalSkill.getSkill().getTasks();
            case Skill skill -> skill.getTasks();
            default -> Collections.emptyList(); // Unreachable
        };
        if (tasks.stream().anyMatch(task -> isTaskOrAnySubtaskCompleted(task, completedTaskIds))) {
            return true;
        }

        boolean allEssentialParentsCompleted = abstractSkill.getParents().stream().filter(AbstractSkill::isEssential).allMatch(parent -> isSkillCompletedBy(parent, completedTaskIds));
        if (!allEssentialParentsCompleted) {
            return false;
        }

        return abstractSkill.getParents().stream().allMatch(parent -> isSkillUnlockedFor(parent, completedTaskIds));
    }

    ///
    /// A skill is completed if:
    ///  - It has tasks and all its tasks are completed
    ///  - **or**, it has no tasks and it is unlocked
    ///
    /// External skills are evaluated in the context of the reference.
    ///
    public boolean isSkillCompletedBy(AbstractSkill abstractSkill, Set<Long> completedTaskIds) {
        List<Task> tasks = switch (abstractSkill) {
            case ExternalSkill externalSkill -> externalSkill.getSkill().getTasks();
            case Skill skill -> skill.getTasks();
            default -> Collections.emptyList(); // Unreachable
        };
        if (tasks.isEmpty()) {
            return isSkillUnlockedFor(abstractSkill, completedTaskIds);
        }
        return tasks.stream().allMatch(task -> isTaskCompleted(task, completedTaskIds));
    }

    public boolean isTaskOrAnySubtaskCompleted(Task task, Set<Long> completedTaskIds) {
        return switch (task) {
            case RegularTask regularTask -> isTaskCompleted(task, completedTaskIds);
            case ChoiceTask choiceTask -> choiceTask.getTasks().stream().anyMatch(info -> completedTaskIds.contains(info.getId()));
            default -> false; // Unreachable
        };
    }

    public boolean isTaskCompleted(Task task, Set<Long> completedTaskIds) {
        return switch (task) {
            case RegularTask regularTask -> completedTaskIds.contains(regularTask.getTaskInfo().getId());
            case ChoiceTask choiceTask -> choiceTask.getTasks().stream().filter(info -> completedTaskIds.contains(info.getId())).count() >= choiceTask.getMinTasks();
            default -> false; // Unreachable
        };
    }

}
