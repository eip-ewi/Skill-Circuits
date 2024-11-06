package nl.tudelft.skills.dto.view;

public record ModuleLevelTaskViewDTO(
    Long id,
    String name,
    String icon,
    Integer time,
    boolean completed
) implements ItemView { }
