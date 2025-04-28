package nl.tudelft.skills.dto.view;

import java.util.List;

public record EditionsView(
        List<EditionRoleView> currentEditions,
        List<EditionRoleView> upcomingEditions,
        List<EditionRoleView> finishedEditions,
        List<EditionRoleView> archivedEditions
) {
    public boolean isEmpty() {
        return currentEditions.isEmpty() && upcomingEditions.isEmpty() && finishedEditions.isEmpty() && archivedEditions.isEmpty();
    }
}
