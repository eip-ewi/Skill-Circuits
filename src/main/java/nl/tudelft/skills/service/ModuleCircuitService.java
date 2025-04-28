package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.dto.view.CheckpointView;
import nl.tudelft.skills.dto.view.ModuleView;
import nl.tudelft.skills.dto.view.PathView;
import nl.tudelft.skills.dto.view.circuit.module.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class ModuleCircuitService {

    private final TaskCompletionRepository taskCompletionRepository;

    public ModuleLevelModuleView getModuleCircuit(SCModule module, SCPerson person) {
        return new ModuleLevelModuleView(
                module.getId(),
                module.getName(),
                module.getEdition().getId(),
                module.getExternalSkills().stream().map(skill -> convertToSkillView(skill, person)).toList(),
                module.getSubmodules().stream().map(submodule -> convertToSubmoduleView(submodule, person)).toList()
        );
    }

    private ModuleLevelSubmoduleView convertToSubmoduleView(Submodule submodule, SCPerson person) {
        return new ModuleLevelSubmoduleView(
                submodule.getId(),
                submodule.getName(),
                submodule.getSkills().stream().map(skill -> convertToSkillView(skill, person)).toList()
        );
    }

    public ModuleLevelSkillView convertToSkillView(AbstractSkill abstractSkill, SCPerson person) {
        Skill skill = switch (abstractSkill) {
            case ExternalSkill externalSkill -> externalSkill.getSkill();
            case Skill s -> s;
            default -> throw new IllegalStateException(); // Unreachable
        };
        return new ModuleLevelSkillView(
                abstractSkill.getId(),
                skill.getName(),
                abstractSkill.getColumn(),
                abstractSkill.isEssential(),
                abstractSkill instanceof ExternalSkill,
                abstractSkill instanceof Skill s && s.getCheckpoint() != null ? s.getCheckpoint().getId() : null,
                abstractSkill.getParents().stream().map(AbstractSkill::getId).toList(),
                abstractSkill.getChildren().stream().map(AbstractSkill::getId).toList(),
                skill.getTasks().stream()
                        .map(task -> convertToTaskView(task, person))
                        .toList()
        );
    }

    public ModuleLevelTaskView convertToTaskView(Task task, SCPerson person) {
        return switch (task) {
            case RegularTask regularTask -> convertToTaskView(regularTask, person);
            case ChoiceTask choiceTask -> convertToTaskView(choiceTask, person);
            default -> null; // Unreachable
        };
    }

    public ModuleLevelTaskView convertToTaskView(RegularTask task, SCPerson person) {
        return new ModuleLevelTaskView.Regular(
                task.getId(),
                task.getTaskInfo().getId(),
                task.getName(),
                task.getType(),
                task.getTime(),
                task.getLink(),
                taskCompletionRepository.hasCompleted(person, task.getTaskInfo()),
                task.getPaths().stream().map(Path::getId).toList()
        );
    }

    public ModuleLevelTaskView convertToTaskView(ChoiceTask task, SCPerson person) {
        return new ModuleLevelTaskView.Choice(
                task.getId(),
                task.getName(),
                task.getMinTasks(),
                task.getTasks().stream().map(info -> convertToChoiceView(info, person)).toList(),
                task.getPaths().stream().map(Path::getId).toList()
        );
    }

    public ModuleLevelTaskView.ChoiceTaskChoiceView convertToChoiceView(TaskInfo info, SCPerson person) {
        return new ModuleLevelTaskView.ChoiceTaskChoiceView(
                info.getId(),
                info.getName(),
                info.getType(),
                info.getTime(),
                info.getLink(),
                taskCompletionRepository.hasCompleted(person, info)
        );
    }

}
