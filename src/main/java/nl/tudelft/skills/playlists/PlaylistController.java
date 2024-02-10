package nl.tudelft.skills.playlists;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;

@Controller
@RequestMapping("playlist")
public class PlaylistController {
    private ResearchParticipantService researchParticipantService;
    private PersonService personService;

    @Autowired
    public PlaylistController(ResearchParticipantService researchParticipantService, PersonService personService){
        this.researchParticipantService = researchParticipantService;
        this.personService = personService;
    }

    @PostMapping("optIn")
    @Transactional
    @PreAuthorize("!@authorisationService.canEditEdition(#editionId)")
    public ResponseEntity<Void> optIn(@AuthenticatedPerson Person person, Long editionId){
        SCPerson scPerson = personService.getOrCreateSCPerson(person.getId());
//        TODO: change edition to ACC
        researchParticipantService.saveOptIn(scPerson);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("optIn")
    public ResponseEntity<String> patchOptIn(@AuthenticatedPerson Person person){
        //        TODO: use patch object and rename method
        return ResponseEntity.ok(researchParticipantService.toggleOpt(personService.getOrCreateSCPerson(person.getId())));
    }
}
