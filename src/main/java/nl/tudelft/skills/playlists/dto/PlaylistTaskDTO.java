package nl.tudelft.skills.playlists.dto;

import lombok.*;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.TaskType;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import org.aspectj.weaver.ast.Not;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistTaskDTO extends View<PlaylistTask> {
    @NotNull
    private Long id;

    @NotNull
    private String taskName;

    @NotNull
    private TaskType type;

    @NotNull
    private Long moduleId;

    @NotNull
    private Long skillId;

    private LocalDateTime started;
    private LocalDateTime completed;
    private Integer completionTime;
}
