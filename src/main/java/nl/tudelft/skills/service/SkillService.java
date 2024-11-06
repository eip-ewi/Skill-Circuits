package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.SkillRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@AllArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    private final TaskService taskService;

    @Transactional
    public void patchSkill(SkillPatchDTO patch) {
        Skill skill = skillRepository.findByIdOrThrow(patch.getId());
        patch.apply(skill);
        skillRepository.save(skill);

        for (TaskPatchDTO taskPatch : patch.getTasks()) {
            taskService.patchTask(taskPatch);
        }
    }

}
