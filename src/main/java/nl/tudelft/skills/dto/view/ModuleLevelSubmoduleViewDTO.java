package nl.tudelft.skills.dto.view;

import java.util.List;

public record ModuleLevelSubmoduleViewDTO(
    Long id,
    String name,
    List<ModuleLevelSkillViewDTO> blocks
) implements GroupView { }
