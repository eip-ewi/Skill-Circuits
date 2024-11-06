package nl.tudelft.skills.controller;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.CheckpointViewDTO;
import nl.tudelft.skills.dto.view.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.ModuleViewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/module")
public class ModuleController {

    private final ModuleViewService moduleViewService;

    @GetMapping("{module}")
    public ModuleLevelModuleViewDTO getCircuit(@AuthenticatedSCPerson SCPerson person, @PathEntity SCModule module) {
        return moduleViewService.getCircuitView(module, person);
    }

    @GetMapping("{module}/checkpoints")
    public List<CheckpointViewDTO> getCheckpoints(@PathEntity SCModule module) {
        return moduleViewService.getCheckpointViews(module);
    }

}
