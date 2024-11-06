package nl.tudelft.skills.dto.view;

import java.util.List;

public sealed interface GroupView permits ModuleLevelSubmoduleViewDTO {

    Long id();
    String name();
    List<? extends BlockView> blocks();

}
