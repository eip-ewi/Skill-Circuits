package nl.tudelft.skills.playlists.dto;

import lombok.*;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.playlists.model.Playlist;
import nl.tudelft.skills.playlists.model.PlaylistState;
import nl.tudelft.skills.playlists.model.PlaylistVersion;
import nl.tudelft.skills.playlists.model.ResearchParticipant;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaylistCreateDTO extends Create<Playlist> {

    @NotNull
    private PlaylistVersionCreateDTO playlistVersionCreate;

    private PlaylistVersion latestVersion;

    @Builder.Default
    private boolean active = true;

    private ResearchParticipant participant;

    @Builder.Default
    private PlaylistState state = PlaylistState.CREATED;

    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();
    @Override
    public Class<Playlist> clazz() {
        return Playlist.class;
    }

}
