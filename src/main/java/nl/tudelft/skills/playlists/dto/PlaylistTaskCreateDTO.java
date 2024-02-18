package nl.tudelft.skills.playlists.dto;

import lombok.*;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import nl.tudelft.skills.playlists.model.ResearchParticipant;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistTaskCreateDTO extends Create<PlaylistTask> {

    private ResearchParticipant participant;

    @NotNull
    private Long taskId;


    @NotNull
    @Builder.Default
    private Integer idx = 0;
    @Override
    public Class<PlaylistTask> clazz() {
        return PlaylistTask.class;
    }

}
