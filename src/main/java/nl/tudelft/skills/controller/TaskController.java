package nl.tudelft.skills.controller;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.TaskCompletionService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/task")
public class TaskController {

    private final TaskCompletionService taskCompletionService;

    @ResponseBody
    @PostMapping("{task}/complete")
    public void updatePosition(@AuthenticatedSCPerson SCPerson person, @PathEntity Task task, @RequestParam boolean completed) {
        if (completed) {
            taskCompletionService.completeTask(person, task);
        } else {
            taskCompletionService.uncompleteTask(person, task);
        }
    }

}
