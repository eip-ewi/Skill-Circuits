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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.create.CheckpointCreateDTO;
import nl.tudelft.skills.dto.patch.CheckpointPatchDTO;
import nl.tudelft.skills.dto.view.checkpoint.ChangeCheckpointDTO;
import nl.tudelft.skills.dto.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.service.CheckpointService;

@Controller
@RequestMapping("checkpoint")
public class CheckpointController {

	private final CheckpointRepository checkpointRepository;
	private final CheckpointService checkpointService;
	private final SkillRepository skillRepository;

	@Autowired
	public CheckpointController(CheckpointRepository checkpointRepository,
			CheckpointService checkpointService, SkillRepository skillRepository) {
		this.checkpointRepository = checkpointRepository;
		this.checkpointService = checkpointService;
		this.skillRepository = skillRepository;
	}

	@Transactional
	@PatchMapping
	@PreAuthorize("@authorisationService.canEditCheckpoint(#patch.id)")
	public ResponseEntity<Void> patchCheckpoint(CheckpointPatchDTO patch) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(patch.getId());
		checkpointRepository.save(patch.apply(checkpoint));
		return ResponseEntity.ok().build();
	}

	@Transactional
	@PutMapping("{id}/skills")
	@PreAuthorize("@authorisationService.canEditCheckpoint({#id})")
	public ResponseEntity<Void> setSkillsForCheckpoint(@PathVariable Long id,
			@RequestBody List<Long> skillIds) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(id);
		Set<Skill> skills = skillRepository.findAllByIdIn(skillIds);
		checkpoint.setSkills(skills);
		skills.forEach(skill -> skill.setCheckpoint(checkpoint));

		return ResponseEntity.ok().build();
	}

	@Transactional
	@PostMapping("{id}/skills")
	@PreAuthorize("@authorisationService.canEditCheckpoint(#id)")
	public ResponseEntity<Void> addSkillsToCheckpoint(@PathVariable Long id,
			@RequestBody List<Long> skillIds) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(id);
		Set<Skill> skills = skillRepository.findAllByIdIn(skillIds);
		checkpoint.getSkills().addAll(skills);
		skills.forEach(skill -> skill.setCheckpoint(checkpoint));

		return ResponseEntity.ok().build();
	}

	/**
	 * Changes the skills belonging to a certain checkpoint in a given module to have another checkpoint.
	 *
	 * @param  changeCheckpointDTO The DTO for changing the checkpoint, contains old/new checkpoint id as well
	 *                             as module id.
	 *
	 * @return                     Redirects to the module page, regardless of whether it was a valid or
	 *                             invalid request. In practice, the frontend implementation should prevent
	 *                             requests for invalid changes.
	 */
	@Transactional
	@PatchMapping("/change-checkpoint")
	@PreAuthorize("@authorisationService.canEditModule(#changeCheckpointDTO.moduleId)")
	public String changeToCheckpoint(ChangeCheckpointDTO changeCheckpointDTO) {
		Checkpoint checkpointPrev = checkpointRepository.findByIdOrThrow(changeCheckpointDTO.getPrevId());
		Checkpoint checkpointNew = checkpointRepository.findByIdOrThrow(changeCheckpointDTO.getNewId());
		Long moduleId = changeCheckpointDTO.getModuleId();

		// If the new checkpoint is already used in the module, or if the checkpoints are
		// not in the same edition, redirect to the module page without any changes.
		// In practice this should already be prevented from the frontend side.
		if (!skillRepository.findAllBySubmoduleModuleIdAndCheckpointId(moduleId,
				changeCheckpointDTO.getNewId()).isEmpty()
				|| checkpointPrev.getEdition() != checkpointNew.getEdition()) {
			return "redirect:/module/" + moduleId;
		}

		Set<Skill> skills = skillRepository.findAllBySubmoduleModuleIdAndCheckpointId(
				moduleId, changeCheckpointDTO.getPrevId());
		checkpointNew.getSkills().addAll(skills);
		checkpointPrev.getSkills().removeAll(skills);
		skills.forEach(skill -> skill.setCheckpoint(checkpointNew));

		return "redirect:/module/" + moduleId;
	}

	@Transactional
	@DeleteMapping("{id}/skills")
	@PreAuthorize("@authorisationService.canEditCheckpoint(#id)")
	public ResponseEntity<Void> deleteSkillsFromCheckpoint(@PathVariable Long id,
			@RequestBody List<Long> skillIds) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(id);
		Optional<Checkpoint> nextCheckpoint = checkpointService.findNextCheckpoint(checkpoint);
		if (nextCheckpoint.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		Set<Skill> skills = skillRepository.findAllByIdIn(skillIds);
		checkpoint.getSkills().removeAll(skills);
		skills.forEach(skill -> {
			skill.setCheckpoint(nextCheckpoint.get());
			skillRepository.save(skill);
		});

		//if checkpoint has no skills left, delete it
		if (checkpoint.getSkills().isEmpty()) {
			checkpointRepository.delete(checkpoint);
		}

		return ResponseEntity.ok().build();
	}

	@Transactional
	@DeleteMapping
	@PreAuthorize("@authorisationService.canDeleteCheckpoint(#id)")
	public ResponseEntity<Void> deleteCheckpoint(@RequestParam Long id) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(id);
		Optional<Checkpoint> nextCheckpoint = checkpointService.findNextCheckpoint(checkpoint);
		if (!checkpoint.getSkills().isEmpty() && nextCheckpoint.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		checkpoint.getSkills().forEach(skill -> {
			skill.setCheckpoint(nextCheckpoint.get());
			skillRepository.save(skill);
		});

		checkpointRepository.delete(checkpoint);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Transactional
	@PostMapping
	@PreAuthorize("@authorisationService.canCreateCheckpointInEdition(#dto.edition.id)")
	public String createCheckpoint(CheckpointCreateDTO dto, @RequestParam Long moduleId) {
		Checkpoint checkpoint = checkpointRepository.save(dto.apply());
		skillRepository.findAllByIdIn(dto.getSkillIds()).forEach(skill -> skill.setCheckpoint(checkpoint));

		return "redirect:module/" + moduleId;
	}

	/**
	 * Creates a checkpoint, and returns a checkpoint element for the setup sidebar.
	 *
	 * @param  dto   the checkpoint to create.
	 * @param  model the model to add data to.
	 * @return       A new checkpoint html element.
	 */
	@Transactional
	@PostMapping("setup")
	@PreAuthorize("@authorisationService.canCreateCheckpointInEdition(#dto.edition.id)")
	public String createCheckpointSetup(CheckpointCreateDTO dto, Model model) {
		Checkpoint checkpoint = checkpointRepository.saveAndFlush(dto.apply());

		model.addAttribute("checkpoint", View.convert(checkpoint, CheckpointViewDTO.class));

		return "edition_setup/checkpoint";

	}

}
