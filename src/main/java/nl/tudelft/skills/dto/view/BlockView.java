package nl.tudelft.skills.dto.view;

import nl.tudelft.skills.controller.ModuleController;

import java.util.List;

public sealed interface BlockView permits ModuleLevelSkillViewDTO {

    Long id();
    String name();
    Integer column();
    List<Long> parents();
    List<Long> children();
    List<? extends ItemView> items();

}
