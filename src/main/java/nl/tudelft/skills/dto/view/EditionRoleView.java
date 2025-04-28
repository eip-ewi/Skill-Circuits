package nl.tudelft.skills.dto.view;

import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.RoleEditionDetailsDTO;

public record EditionRoleView(RoleEditionDetailsDTO.TypeEnum role, EditionDetailsDTO edition) {
}
