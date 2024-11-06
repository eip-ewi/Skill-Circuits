package nl.tudelft.skills.dto.view;

import nl.tudelft.skills.dto.view.ModuleLevelSubmoduleViewDTO;

import java.util.List;

public record ModuleLevelModuleViewDTO(
    Long id,
    String name,
    List<ModuleLevelSubmoduleViewDTO> groups
) implements CircuitView { }
