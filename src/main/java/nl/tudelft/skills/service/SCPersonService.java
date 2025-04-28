package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SCPersonService {

    private final PersonRepository personRepository;

    public void setViewMode(SCPerson person, ViewMode viewMode) {
        person.setViewMode(viewMode);
        personRepository.save(person);
    }

}
