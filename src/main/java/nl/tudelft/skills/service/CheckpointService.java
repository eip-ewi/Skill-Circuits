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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.CheckpointRepository;

@Service
public class CheckpointService {

	private final CheckpointRepository checkpointRepository;

	@Autowired
	public CheckpointService(CheckpointRepository checkpointRepository) {
		this.checkpointRepository = checkpointRepository;
	}

	/**
	 * Finds the checkpoint following a given one, in a specific module. The method considers the rows of
	 * skills in the module.
	 *
	 * @param  checkpoint the checkpoint of which to find the next one in the module.
	 * @param  module     the module in which to find the next checkpoint.
	 * @return            an optional containing the next checkpoint in the module iff it exists.
	 */
	public Optional<Checkpoint> findNextCheckpointInModule(Checkpoint checkpoint, SCModule module) {
		List<Skill> sortedSkills = module.getSubmodules().stream().flatMap(sm -> sm.getSkills().stream())
				.sorted(Comparator.comparing(Skill::getRow).reversed()).toList();

		// If checkpoint is the last checkpoint (or there are no skills), return empty optional
		if (sortedSkills.size() == 0 || sortedSkills.get(0).getCheckpoint().equals(checkpoint)) {
			return Optional.empty();
		}

		return Optional
				.of(sortedSkills.stream().filter(s -> !s.getCheckpoint().equals(checkpoint)).findFirst()
						.get().getCheckpoint());
	}

	/**
	 * Deletes a given checkpoint, iff it is never the last checkpoint in a module. Re-assigns the skills of
	 * the checkpoint to the next checkpoint on a module by module basis.
	 *
	 * @param  checkpoint The checkpoint to delete.
	 * @return            Whether the checkpoint could be deleted.
	 */
	@Transactional
	public boolean deleteCheckpoint(Checkpoint checkpoint) {
		Set<SCModule> modules = checkpoint.getSkills().stream().map(s -> s.getSubmodule().getModule())
				.collect(Collectors.toSet());
		Map<SCModule, Checkpoint> reAssignSkills = new HashMap<>();
		for (SCModule module : modules) {
			Optional<Checkpoint> nextCheckpointInModule = findNextCheckpointInModule(checkpoint, module);

			// If it is the last checkpoint, do not delete the checkpoint and return false
			if (nextCheckpointInModule.isEmpty()) {
				return false;
			}

			reAssignSkills.put(module, nextCheckpointInModule.get());
		}

		// Re-assign skills to their next checkpoint (module by module basis)
		for (Skill skill : checkpoint.getSkills()) {
			skill.setCheckpoint(reAssignSkills.get(skill.getSubmodule().getModule()));
		}

		// If for all modules it is never the last checkpoint, delete the checkpoint and return true
		checkpointRepository.delete(checkpoint);
		return true;
	}
}
