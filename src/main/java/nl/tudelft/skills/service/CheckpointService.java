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

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.CheckpointRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckpointService {

	private final CheckpointRepository checkpointRepository;

	@Autowired
	public CheckpointService(CheckpointRepository checkpointRepository) {
		this.checkpointRepository = checkpointRepository;
	}

	/**
	 * Performs a breadth first search to find the next checkpoint, if it exists.
	 *
	 * @param  checkpoint the checkpoint to find the next of.
	 * @return            an optional containing the next checkpoint iff it exists
	 */
	public Optional<Checkpoint> findNextCheckpoint(Checkpoint checkpoint) {
		Queue<Skill> queue = new ArrayDeque<>(checkpoint.getSkills());

		while (!queue.isEmpty()) {
			Skill curr = queue.remove();
			if (!curr.getCheckpoint().equals(checkpoint)) {
				return Optional.of(curr.getCheckpoint());
			}
			queue.addAll(
					curr.getChildren().stream().filter(s -> s instanceof Skill).map(s -> (Skill) s).toList());
		}
		return Optional.empty();
	}
}
