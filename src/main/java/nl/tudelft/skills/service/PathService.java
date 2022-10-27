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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nl.tudelft.skills.dto.patch.PathPatchDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PathService {

	private final TaskRepository taskRepository;
	private final PathRepository pathRepository;

	@Autowired
	public PathService(TaskRepository taskRepository, PathRepository pathRepository) {
		this.taskRepository = taskRepository;
		this.pathRepository = pathRepository;
	}

	/**
	 * Updates tasks in a path.
	 *
	 * @param patch New path patch dto.
	 * @param path  New path.
	 */
	@Transactional
	public void updateTasksInPathManyToMany(PathPatchDTO patch, Path path) {

		Set<Task> updtTasks = taskRepository.findAllByIdIn(patch.getTaskIds()).stream()
				.collect(Collectors.toSet());

		// update tasks in path
		List<Task> oldTasks = taskRepository
				.findAllByIdIn(path.getTasks().stream().map(Task::getId).toList());

		// remove tasks that are not in path
		oldTasks.stream().filter(t -> !patch.getTaskIds().contains(t.getId()))
				.forEach(t -> t.setPaths(t.getPaths().stream().filter(p -> !p.getId().equals(path.getId()))
						.collect(Collectors.toSet())));

		taskRepository.saveAllAndFlush(oldTasks);

		// add tasks that were not previously in path
		HashSet<Task> newTasks = new HashSet<>(taskRepository.findAllByIdIn(patch.getTaskIds()));
		newTasks.stream().filter(t -> !path.getTasks().contains(t))
				.forEach(t -> t.getPaths().add(path));
		taskRepository.saveAllAndFlush(newTasks);

		path.setTasks(updtTasks);
		pathRepository.save(path);
	}
}
