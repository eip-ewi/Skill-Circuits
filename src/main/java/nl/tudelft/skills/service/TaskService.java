package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public void patchTask(TaskPatchDTO patch) {
        Task task = taskRepository.findByIdOrThrow(patch.getId());
        patch.apply(task);
        taskRepository.save(task);
    }

}
