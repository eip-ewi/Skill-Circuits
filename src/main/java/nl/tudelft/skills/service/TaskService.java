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
package nl.tudelft.skills.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.create.ChoiceTaskCreate;
import nl.tudelft.skills.dto.create.RegularTaskCreate;
import nl.tudelft.skills.dto.patch.ChoiceTaskPatch;
import nl.tudelft.skills.dto.patch.TaskMove;
import nl.tudelft.skills.dto.patch.TaskPatch;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.TaskInfoRepository;
import nl.tudelft.skills.repository.TaskRepository;

@Service
@AllArgsConstructor
public class TaskService {

	private final TaskInfoRepository taskInfoRepository;
	private final TaskRepository taskRepository;

	private final DTOConverter dtoConverter;

	@Transactional
	public RegularTask createTask(RegularTaskCreate create) {
		RegularTask task = new RegularTask();
		Skill skill = dtoConverter.apply(create.getSkill());
		task.setSkill(skill);
		task.setTaskInfo(TaskInfo.builder().task(task).build());
		task.setName(create.getName());
		task.setTime(0);
		task.setType(TaskType.READING);
		task.setIdx(task.getSkill().getTasks().size());
		task.setPaths(new HashSet<>(skill.getSubmodule().getModule().getEdition().getPaths()));
		return taskRepository.save(task);
	}

	@Transactional
	public ChoiceTask createTask(ChoiceTaskCreate create) {
		ChoiceTask task = new ChoiceTask();
		Skill skill = dtoConverter.apply(create.getSkill());
		task.setSkill(skill);
		task.setIdx(task.getSkill().getTasks().size());
		task.setPaths(new HashSet<>(task.getSkill().getSubmodule().getModule().getEdition().getPaths()));
		return taskRepository.save(task);
	}

	@Transactional
	public void patchTask(Task task, TaskPatch patch) {
		patch.apply(task, dtoConverter);
		taskRepository.save(task);
	}

	@Transactional
	public void patchTask(ChoiceTask task, ChoiceTaskPatch patch) {
		patch.apply(task, dtoConverter);
		taskRepository.save(task);
	}

	@Transactional
	public void updateTaskIndex(Task task, Integer newIndex) {
		List<Task> tasks = task.getSkill().getTasks();
		tasks.stream().filter(t -> t.getIdx() > task.getIdx()).forEach(t -> t.setIdx(t.getIdx() - 1));
		tasks.stream().filter(t -> t.getIdx() >= newIndex).forEach(t -> t.setIdx(t.getIdx() + 1));
		task.setIdx(newIndex);
		taskRepository.saveAll(tasks);
	}

	@Transactional
	public void moveTask(Task task, TaskMove move) {
		task.getSkill().getTasks().stream().filter(t -> t.getIdx() > task.getIdx())
				.forEach(t -> t.setIdx(t.getIdx() - 1));
		taskRepository.saveAll(task.getSkill().getTasks());

		Skill newSkill = dtoConverter.apply(move.getSkill());
		newSkill.getTasks().stream().filter(t -> t.getIdx() >= move.getIndex())
				.forEach(t -> t.setIdx(t.getIdx() + 1));
		taskRepository.saveAll(newSkill.getTasks());

		task.setIdx(move.getIndex());
		task.setSkill(newSkill);
		taskRepository.save(task);
	}

	@Transactional
	public TaskInfo moveTaskInsideChoiceTask(ChoiceTask choiceTask, RegularTask subtask) {
		subtask.getSkill().getTasks().stream().filter(t -> t.getIdx() > subtask.getIdx())
				.forEach(t -> t.setIdx(t.getIdx() - 1));
		taskRepository.saveAll(subtask.getSkill().getTasks());

		subtask.getTaskInfo().setTask(null);
		subtask.getTaskInfo().setChoiceTask(choiceTask);
		TaskInfo taskInfo = taskInfoRepository.save(subtask.getTaskInfo());

		subtask.setTaskInfo(null);
		taskRepository.delete(subtask);

		return taskInfo;
	}

	@Transactional
	public void deleteTask(Task task) {
		taskRepository.delete(task);
	}
}
