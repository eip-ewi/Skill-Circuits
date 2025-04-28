package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleEditionDetailsDTO;
import nl.tudelft.labracore.api.dto.RolePersonDetailsDTO;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelModuleView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class EditionService {

    private final EditionControllerApi editionApi;
    private final PersonControllerApi personApi;

    private final EditionRepository editionRepository;
    private final RoleControllerApi roleControllerApi;

    public EditionView getEdition(Long editionId) {
        EditionDetailsDTO edition = requireNonNull(editionApi.getEditionById(editionId).block());
        SCEdition scEdition = editionRepository.getOrCreate(editionId);
        return new EditionView(
                edition.getId(),
                edition.getCourse().getName() + " - " + edition.getName(),
                new EditionView.CourseView(edition.getCourse().getId(), edition.getCourse().getName(), edition.getCourse().getCode()),
                scEdition.getCheckpoints().stream().map(checkpoint -> new CheckpointView(checkpoint.getId(), checkpoint.getName(), checkpoint.getDeadline())).toList(),
                scEdition.getModules().stream().map(m -> new ModuleView(m.getId(), m.getName())).toList(),
                scEdition.getPaths().stream().map(path -> new PathView(path.getId(), path.getName(), path.getDescription())).toList(),
                roleControllerApi.getRolesByEditions(Set.of(editionId)).filter(role -> role.getType() == RolePersonDetailsDTO.TypeEnum.TEACHER).map(RolePersonDetailsDTO::getPerson).sort(Comparator.comparing(PersonSummaryDTO::getDisplayName)).collectList().block(),
                scEdition.getEditors().isEmpty() ? Collections.emptyList() : personApi.getPeopleById(scEdition.getEditors().stream().map(SCPerson::getId).toList()).sort(Comparator.comparing(PersonSummaryDTO::getDisplayName)).collectList().block()
        );
    }

    public Set<Long> getManagedEditionIds(Person person) {
        return requireNonNull(personApi.getRolesForPerson(person.getId())
                .filter(role -> role.getType() == RoleEditionDetailsDTO.TypeEnum.TEACHER)
                .map(role -> role.getEdition().getId())
                .collect(Collectors.toSet())
                .block());
    }

    public EditionsView getSortedEditions(Person person) {
        Map<Long, RoleEditionDetailsDTO.TypeEnum> roles = requireNonNull(personApi.getRolesForPerson(person.getId())
                .collect(Collectors.toMap(role -> role.getEdition().getId(), RoleEditionDetailsDTO::getType))
                .block());
        List<EditionRoleView> editions = roles.isEmpty() ? Collections.emptyList() :
                requireNonNull(editionApi.getEditionsById(new ArrayList<>(roles.keySet()))
                        .map(edition -> new EditionRoleView(roles.get(edition.getId()), edition))
                        .collectList().block());

        List<EditionRoleView> current = new ArrayList<>();
        List<EditionRoleView> upcoming = new ArrayList<>();
        List<EditionRoleView> finished = new ArrayList<>();
        List<EditionRoleView> archived = new ArrayList<>();

        editions.stream().sorted(Comparator.comparing(view -> view.edition().getStartDate())).forEach(view -> {
            if (view.edition().getIsArchived()) {
                archived.addFirst(view);
            } else if (view.edition().getEndDate().isBefore(LocalDateTime.now())) {
                finished.addFirst(view);
            } else if (view.edition().getStartDate().isAfter(LocalDateTime.now())) {
                upcoming.addLast(view);
            } else {
                current.addFirst(view);
            }
        });

        return new EditionsView(current, upcoming, finished, archived);
    }

}
