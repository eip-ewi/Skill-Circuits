package nl.tudelft.skills.dto.view;

import nl.tudelft.skills.dto.old.view.GroupView;

import java.util.List;

public record ModuleLevelSkillViewDTO(
    Long id,
    String name,
    Integer column,
    List<Long> parents,
    List<Long> children,
    List<ModuleLevelTaskViewDTO> items
) implements BlockView { }
