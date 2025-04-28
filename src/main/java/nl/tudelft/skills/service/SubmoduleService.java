/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.tudelft.skills.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.create.SubmoduleCreate;
import nl.tudelft.skills.dto.patch.SubmodulePatch;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SubmoduleService {

    private final SubmoduleRepository submoduleRepository;

    private final DTOConverter dtoConverter;

    @Transactional
    public Submodule createSubmodule(SubmoduleCreate create) {
        return submoduleRepository.save(create.apply(dtoConverter));
    }

    @Transactional
    public void patchSubmodule(Submodule submodule, SubmodulePatch patch) {
        patch.apply(submodule, dtoConverter);
        submoduleRepository.save(submodule);
    }

    @Transactional
    public void updatePosition(Submodule submodule, Integer column) {
        submodule.setColumn(column);
        submoduleRepository.save(submodule);
    }

    @Transactional
    public void deleteSubmodule(Submodule submodule) {
        submoduleRepository.delete(submodule);
    }

}
