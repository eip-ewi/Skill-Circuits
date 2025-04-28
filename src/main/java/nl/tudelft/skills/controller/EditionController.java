package nl.tudelft.skills.controller;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.EditionView;
import nl.tudelft.skills.dto.view.EditionsView;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelEditionView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.EditionCircuitService;
import nl.tudelft.skills.service.EditionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/editions")
public class EditionController {

    private final EditionService editionService;
    private final EditionCircuitService editionCircuitService;

    @GetMapping
    public EditionsView getEditions(@AuthenticatedPerson Person person) {
        return editionService.getSortedEditions(person);
    }

    @GetMapping("{editionId}")
    public EditionView getEdition(@PathVariable Long editionId) {
        return editionService.getEdition(editionId);
    }

    @GetMapping("{editionId}/circuit")
    public EditionLevelEditionView getEditionCircuit(@AuthenticatedSCPerson SCPerson person, @PathVariable Long editionId) {
        return editionCircuitService.getEditionCircuit(editionId, person);
    }

}
