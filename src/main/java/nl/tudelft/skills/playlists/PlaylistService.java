package nl.tudelft.skills.playlists;

import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlaylistService {

    private PersonRepository personRepository;

    @Autowired
    public PlaylistService(PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    public boolean getEnrolledACC(Long personId){
        SCPerson scPerson = personRepository.findByIdOrThrow(personId);
        return false;
    }
}
