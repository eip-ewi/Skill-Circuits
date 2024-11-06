package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.PathPreferenceRepository;
import org.hibernate.annotations.Check;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@AllArgsConstructor
public class ModuleViewService {

    public List<CheckpointViewDTO> getCheckpointViews(SCModule module) {
        return module.getEdition().getCheckpoints().stream()
                .filter(checkpoint -> checkpoint.getSkills().stream().anyMatch(s -> Objects.equals(s.getSubmodule().getModule().getId(), module.getId())))
                .map(checkpoint -> new CheckpointViewDTO(
                        checkpoint.getId(),
                        checkpoint.getName(),
                        checkpoint.getDeadline(),
                        checkpoint.getSkills().stream().map(AbstractSkill::getId).toList())
                ).toList();
    }

    public ModuleLevelModuleViewDTO getCircuitView(SCModule module, SCPerson person) {
        return new ModuleLevelModuleViewDTO(
                module.getId(),
                module.getName(),
                module.getSubmodules().stream().map(s -> getGroupView(s, person)).toList()
        );
    }

    public ModuleLevelSubmoduleViewDTO getGroupView(Submodule submodule, SCPerson person) {
        return new ModuleLevelSubmoduleViewDTO(
                submodule.getId(),
                submodule.getName(),
                submodule.getSkills().stream().map(s -> getBlockView(s, person)).toList()
        );
    }

    public ModuleLevelSkillViewDTO getBlockView(Skill skill, SCPerson person) {
        return new ModuleLevelSkillViewDTO(
                skill.getId(),
                skill.getName(),
                skill.getColumn(),
                skill.getParents().stream().map(AbstractSkill::getId).toList(),
                skill.getChildren().stream().map(AbstractSkill::getId).toList(),
                skill.getTasks().stream().map(t -> getItemView(t, person)).toList()
        );
    }

    public ModuleLevelTaskViewDTO getItemView(Task task, SCPerson person) {
        return new ModuleLevelTaskViewDTO(
                task.getId(),
                task.getName(),
                task.getType().getIcon(),
                task.getTime(),
                task.getCompletedBy().stream().anyMatch(c -> Objects.equals(c.getPerson().getId(), person.getId()))
        );
    }

}
