package nl.tudelft.skills.playlists;

import nl.tudelft.skills.model.labracore.SCPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

public interface ResearchParticipantRepository extends JpaRepository<ResearchParticipant, Long> {
    default ResearchParticipant findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("ResearchParticipant was not found: " + id));
    }

    ResearchParticipant findByPerson(SCPerson person);

}
