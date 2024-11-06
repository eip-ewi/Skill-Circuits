package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class TaskCompletionService {

    private final TaskCompletionRepository taskCompletionRepository;

    /**
     * Saves a TaskCompletion to the repository, given the corresponding SCPerson and Task.
     *
     * @param  person The SCPerson that completed the Task
     * @param  task   The Task that was completed
     */
    public void completeTask(SCPerson person, Task task) {
        TaskCompletion completion = taskCompletionRepository.save(TaskCompletion.builder().task(task).person(person).build());
        person.getTaskCompletions().add(completion);
        task.getCompletedBy().add(completion);
    }

    /**
     * Deletes a TaskCompletion from the repository, given the corresponding SCPerson and Task. If such a
     * TaskCompletion does not exist, returns null.
     *
     * @param  person The SCPerson that had completed the Task
     * @param  task   The Task that was completed
     */
    public void uncompleteTask(SCPerson person, Task task) {
        person.getTaskCompletions().stream().filter(c -> c.getPerson().equals(person) && c.getTask().equals(task)).findFirst().ifPresent(taskCompletion -> {
            taskCompletionRepository.delete(taskCompletion);
            person.getTaskCompletions().remove(taskCompletion);
            task.getCompletedBy().remove(taskCompletion);
        });
    }

}
