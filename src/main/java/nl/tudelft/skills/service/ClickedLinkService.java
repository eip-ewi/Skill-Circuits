package nl.tudelft.skills.service;

import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.ClickedLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ClickedLinkService {
    private final ClickedLinkRepository clickedLinkRepository;

    @Autowired
    public ClickedLinkService(ClickedLinkRepository clickedLinkRepository){
        this.clickedLinkRepository = clickedLinkRepository;
    }

    /**
     * Deletes the clicked links belonging to specific tasks
     *
     * @param tasks The list of tasks the clicked links should be deleted for
     */
    @Transactional
    public void deleteClickedLinksForTasks(List<Task> tasks){
        var taskIds = tasks.stream().map(Task::getId).toList();
        var clicks = clickedLinkRepository.findAll();
        var deletedClicks = clicks.stream().filter(c -> taskIds.contains(c.getTask().getId()));
        deletedClicks.forEach(clickedLinkRepository::delete);
    }
}
