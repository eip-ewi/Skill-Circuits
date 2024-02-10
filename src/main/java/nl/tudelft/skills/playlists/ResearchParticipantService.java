package nl.tudelft.skills.playlists;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ResearchParticipantService {

    private PersonRepository personRepository;
    private ResearchParticipantRepository researchParticipantRepository;

    @Autowired
    public ResearchParticipantService(PersonRepository personRepository, ResearchParticipantRepository researchParticipantRepository){

        this.personRepository = personRepository;
        this.researchParticipantRepository = researchParticipantRepository;
    }


    @Transactional
    public ResearchParticipant saveOptIn(SCPerson scPerson){

        return researchParticipantRepository.save(ResearchParticipant.builder().person(scPerson).build());

    }

    @Transactional
    public String toggleOpt(SCPerson scPerson){
        Optional<Boolean> opt = optedIn(scPerson);
        ResearchParticipant rp = researchParticipantRepository.findByPerson(scPerson);

        if(opt.isPresent()){
            if(opt.get()){
                rp.setOptOut(LocalDateTime.now());
                return "Opted out";
            }
            else {
                rp.setOptIn(LocalDateTime.now());
                return "Opted in";
            }
        } else{
//            TODO: Throw exception or refactor code
            return "This should not happen";
        }

    }

    public Optional<Boolean> optedIn(SCPerson scPerson){
        ResearchParticipant rp = researchParticipantRepository.findByPerson(scPerson);

        if(rp != null){
            LocalDateTime optInDate = rp.getOptIn();
            LocalDateTime optOutDate = rp.getOptOut();

            if (optOutDate == null){
                return Optional.of(true);
            } else {
                return Optional.of(!optOutDate.isAfter(optInDate));
            }
        } else {
            return Optional.empty();
        }
    }

    public void addRPInfoToModel(Person person, Model model){
        if(person != null ){
            SCPerson scPerson = personRepository.findByIdOrThrow(person.getId());
            model.addAttribute("studentOptedIn", optedIn(scPerson));
        } else{
            model.addAttribute("studentOptedIn", Optional.empty());
        }

    }

}
