package nl.tudelft.skills.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.create.ModuleCreate;
import nl.tudelft.skills.dto.patch.ModulePatch;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.ModuleRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;

    private final DTOConverter dtoConverter;

    @Transactional
    public SCModule createModule(ModuleCreate create) {
        return moduleRepository.save(create.apply(dtoConverter));
    }

    @Transactional
    public void patchModule(SCModule module, ModulePatch patch) {
        patch.apply(module, dtoConverter);
        moduleRepository.save(module);
    }

    @Transactional
    public void deleteModule(SCModule module) {
        module.getEdition().getModules().remove(module);
        moduleRepository.delete(module);
    }

}
