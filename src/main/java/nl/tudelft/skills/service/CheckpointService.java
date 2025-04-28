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
import nl.tudelft.skills.dto.create.CheckpointCreate;
import nl.tudelft.skills.dto.patch.CheckpointPatch;
import nl.tudelft.skills.dto.patch.SkillPatch;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.repository.CheckpointRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CheckpointService {

    private final CheckpointRepository checkpointRepository;

    private final DTOConverter dtoConverter;

    @Transactional
    public Checkpoint createCheckpoint(CheckpointCreate create) {
        return checkpointRepository.save(create.apply(dtoConverter));
    }

    @Transactional
    public void patchCheckpoint(Checkpoint checkpoint, CheckpointPatch patch) {
        patch.apply(checkpoint, dtoConverter);
        checkpointRepository.save(checkpoint);
    }

    @Transactional
    public void deleteCheckpoint(Checkpoint checkpoint) {
        checkpoint.getSkills().forEach(skill -> {
            skill.setCheckpoint(null);
        });
        checkpointRepository.delete(checkpoint);
    }
}
