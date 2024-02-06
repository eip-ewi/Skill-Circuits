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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.patch.PathPatchDTO;
import nl.tudelft.skills.dto.view.edition.PathViewDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.TaskRepository;

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
	 * Updates tasks in a path. If the patch has a moduleId this only creates or deletes tasks in that module.
	 *
	 * @param patch New path patch dto.
	 * @param path  New path.
	 */
	@Transactional
	public void updateTasksInPathManyToMany(PathPatchDTO patch, Path path) {
		List<Task> oldTasks = taskRepository
				.findAllByIdIn(path.getTasks().stream().map(Task::getId).toList());

		Set<Task> selectedTasks = new HashSet<>(taskRepository.findAllByIdIn(patch.getTaskIds()));

		// remove tasks that are not in path
		// if the patch has a moduleId, remove only tasks that are in this module
		Set<Task> removedTasks = oldTasks.stream().filter(t -> !patch.getTaskIds().contains(t.getId())
				&& (patch.getModuleId() == null
						|| t.getSkill().getSubmodule().getModule().getId().equals(patch.getModuleId())))
				.collect(Collectors.toSet());
		removedTasks
				.forEach(t -> t.setPaths(t.getPaths().stream().filter(p -> !p.getId().equals(path.getId()))
						.collect(Collectors.toSet())));
		taskRepository.saveAllAndFlush(oldTasks);

		// add tasks that were not previously in path
		selectedTasks.forEach(t -> t.getPaths().add(path));
		taskRepository.saveAllAndFlush(selectedTasks);

		Set<Task> newTasks = new HashSet<>(oldTasks);
		newTasks.removeAll(removedTasks);
		newTasks.addAll(selectedTasks);
		path.setTasks(newTasks);
		pathRepository.save(path);
	}

	/**
	 * Checks if a task is in a certain path.
	 *
	 * @param  taskId Id of the task.
	 * @param  pathId Id of the path.
	 * @return        True if task belongs to this path, false otherwise.
	 */
	public boolean isTaskInPath(Long taskId, Long pathId) {
		return pathRepository.findByIdOrThrow(pathId).getTasks().stream()
				.map(Task::getId)
				.anyMatch(taskId::equals);
	}

	/**
	 * Gets the PathViewDTO of a path, if the path id is not null. Otherwise, returns null.
	 *
	 * @param  pathId The path id, or null.
	 * @return        The PathViewDTO of the path, if the path id is not null. Otherwise, null.
	 */
	public PathViewDTO getPath(Long pathId) {
		if (pathId == null)
			return null;
		return View.convert(pathRepository.getById(pathId), PathViewDTO.class);
	}

}
