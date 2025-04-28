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
package nl.tudelft.skills.controller;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.ParamEntity;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.create.PathCreate;
import nl.tudelft.skills.dto.patch.PathPatch;
import nl.tudelft.skills.dto.view.PathView;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.PathService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/paths")
public class PathsController {

    private final PathService pathService;

    @GetMapping("active")
    public PathView getActivePath(@AuthenticatedSCPerson SCPerson person, @ParamEntity SCEdition edition) {
        Path path = pathService.getActivePath(person, edition);
        return path == null ? null : new PathView(path.getId(), path.getName(), path.getDescription());
    }

    @PutMapping("active")
    public void setActivePath(@AuthenticatedSCPerson SCPerson person, @ParamEntity SCEdition edition, @ParamEntity Path path) {
        pathService.setActivePath(person, edition, path);
    }

    @PostMapping
    public PathView createPath(@RequestBody PathCreate create) {
        Path path = pathService.createPath(create);
        return new PathView(path.getId(), path.getName(), path.getDescription());
    }

    @PatchMapping("{path}")
    public void patchPath(@PathEntity Path path, @RequestBody PathPatch patch) {
        pathService.patchPath(path, patch);
    }

    @DeleteMapping("{path}")
    public void deletePath(@PathEntity Path path) {
        pathService.deletePath(path);
    }

}
