package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.patch.SubtaskMove;
import nl.tudelft.skills.dto.patch.TaskInfoPatch;
import nl.tudelft.skills.model.ChoiceTask;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.repository.TaskInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TaskInfoService {

    private final TaskInfoRepository taskInfoRepository;

    private final DTOConverter dtoConverter;

    @Transactional
    public void patchTaskInfo(TaskInfo taskInfo, TaskInfoPatch patch) {
        patch.apply(taskInfo, dtoConverter);
        taskInfoRepository.save(taskInfo);
    }

    @Transactional
    public void moveSubtask(TaskInfo subtask, SubtaskMove move) {
        subtask.setChoiceTask(dtoConverter.apply(move.getChoiceTask()));
        taskInfoRepository.save(subtask);
    }

    @Transactional
    public void deleteTaskInfo(TaskInfo taskInfo) {
        taskInfoRepository.delete(taskInfo);
    }
}
