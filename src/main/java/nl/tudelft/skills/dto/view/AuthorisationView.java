package nl.tudelft.skills.dto.view;

import nl.tudelft.skills.enums.ViewMode;

import java.util.Set;

public record AuthorisationView(
        ViewMode viewMode,
        boolean isAdmin,
        Set<Long> managedEditions,
        Set<Long> managedCourses
) {
}
