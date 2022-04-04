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

import nl.tudelft.skills.dto.patch.CheckpointPatchDTO;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.repository.CheckpointRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("checkpoint")
public class CheckpointController {

	private final CheckpointRepository checkpointRepository;

	@Autowired
	public CheckpointController(CheckpointRepository checkpointRepository) {
		this.checkpointRepository = checkpointRepository;
	}

	@PatchMapping
	@PreAuthorize("@authorisationService.canEditCheckpoint(#patch.id)")
	public ResponseEntity<Void> patchCheckpoint(CheckpointPatchDTO patch) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(patch.getId());
		checkpointRepository.save(patch.apply(checkpoint));
		return ResponseEntity.ok().build();
	}
}
