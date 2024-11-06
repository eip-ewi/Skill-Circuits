package nl.tudelft.skills.dto.view;

public sealed interface ItemView permits ModuleLevelTaskViewDTO {

    Long id();
    String name();
    String icon();
    Integer time();
    boolean completed();

}
