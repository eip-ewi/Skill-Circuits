/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.dto.create.CheckpointCreate;
import nl.tudelft.skills.dto.patch.CheckpointPatch;
import nl.tudelft.skills.dto.view.CheckpointView;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.service.CheckpointService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/checkpoints")
public class CheckpointController {

	private final CheckpointService checkpointService;

	@PostMapping
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#create.edition.id)")
	public CheckpointView createCheckpoint(@RequestBody CheckpointCreate create) {
		Checkpoint checkpoint = checkpointService.createCheckpoint(create);
		return new CheckpointView(checkpoint.getId(), checkpoint.getName(), checkpoint.getDeadline());
	}

	@PatchMapping("{checkpoint}")
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#checkpoint.edition.id)")
	public void patchSkill(@PathEntity Checkpoint checkpoint, @RequestBody CheckpointPatch patch) {
		checkpointService.patchCheckpoint(checkpoint, patch);
	}

	@DeleteMapping("{checkpoint}")
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#checkpoint.edition.id)")
	public void deleteCheckpoint(@PathEntity Checkpoint checkpoint) {
		checkpointService.deleteCheckpoint(checkpoint);
	}

}
