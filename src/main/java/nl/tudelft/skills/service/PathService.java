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

import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.create.PathCreate;
import nl.tudelft.skills.dto.patch.PathPatch;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.PathPreferenceRepository;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class PathService {

    private final PathRepository pathRepository;
    private final PathPreferenceRepository pathPreferenceRepository;
    private final TaskRepository taskRepository;

    private final DTOConverter dtoConverter;

    public Path getActivePath(SCPerson person, SCEdition edition) {
        return pathPreferenceRepository.findByPersonAndEdition(person, edition).map(PathPreference::getPath).orElse(null);
    }

    @Transactional
    public void setActivePath(SCPerson person, SCEdition edition, Path path) {
        pathPreferenceRepository.findByPersonAndEdition(person, edition).ifPresentOrElse(preference -> {
            preference.setPath(path);
            pathPreferenceRepository.save(preference);
        }, () -> {
            pathPreferenceRepository.save(PathPreference.builder()
                    .person(person)
                    .edition(edition)
                    .path(path)
                    .build());
        });
    }

    @Transactional
    public Path createPath(PathCreate create) {
        Path path = pathRepository.save(create.apply(dtoConverter));
        Set<Task> tasks = taskRepository.findAllByEdition(path.getEdition());
        tasks.forEach(task -> task.getPaths().add(path));
        taskRepository.saveAll(tasks);
        return path;
    }

    @Transactional
    public void patchPath(Path path, PathPatch patch) {
        patch.apply(path, dtoConverter);
        pathRepository.save(path);
    }

    @Transactional
    public void deletePath(Path path) {
        Set<Task> tasks = path.getTasks();
        tasks.forEach(task -> task.getPaths().remove(path));
        taskRepository.saveAll(tasks);

        pathRepository.delete(path);
    }
}
