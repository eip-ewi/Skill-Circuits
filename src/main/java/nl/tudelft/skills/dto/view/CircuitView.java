package nl.tudelft.skills.dto.view;

import java.util.List;

public sealed interface CircuitView permits ModuleLevelModuleViewDTO {

    Long id();
    String name();
    List<? extends GroupView> groups();

}
