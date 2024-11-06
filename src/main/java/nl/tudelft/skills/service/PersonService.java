package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    /**
     * Returns a SCPerson by person id. If it doesn't exist, creates one.
     *
     * @param  personId The id of the person
     * @return          The SCPerson with id.
     */
    @Transactional
    public SCPerson getSCPerson(Long personId) {
        return personRepository.findById(personId)
                .orElseGet(() -> personRepository.save(SCPerson.builder().id(personId).build()));
    }

}
