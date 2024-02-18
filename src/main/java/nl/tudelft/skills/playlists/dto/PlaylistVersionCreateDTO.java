package nl.tudelft.skills.playlists.dto;

import lombok.*;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.playlists.model.Playlist;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import nl.tudelft.skills.playlists.model.PlaylistVersion;

import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistVersionCreateDTO extends Create<PlaylistVersion> {

    @NotNull
    private List<PlaylistTaskCreateDTO> taskCreates;

    private Set<PlaylistTask> tasks;


    @NotNull
    @Builder.Default
    private Integer totalTime = 0;
    @Override
    public Class<PlaylistVersion> clazz() {
        return PlaylistVersion.class;
    }
}
