package nl.tudelft.skills.playlists.repository;

import nl.tudelft.skills.playlists.model.Playlist;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

public interface PlaylistTaskRepository extends JpaRepository<PlaylistTask, Long> {

    default PlaylistTask findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlaylistTask was not found: " + id));
    }
}
