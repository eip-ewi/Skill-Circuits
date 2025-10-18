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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.patch.SubtaskMove;
import nl.tudelft.skills.dto.patch.TaskInfoPatch;
import nl.tudelft.skills.dto.patch.TaskMove;
import nl.tudelft.skills.model.ChoiceTask;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.repository.RegularTaskRepository;
import nl.tudelft.skills.repository.TaskInfoRepository;
import nl.tudelft.skills.repository.TaskRepository;

@Service
@AllArgsConstructor
public class TaskInfoService {

	private final TaskInfoRepository taskInfoRepository;
	private final TaskRepository taskRepository;
	private final RegularTaskRepository regularTaskRepository;

	private final DTOConverter dtoConverter;

	@Transactional
	public void patchTaskInfo(TaskInfo taskInfo, TaskInfoPatch patch) {
		patch.apply(taskInfo, dtoConverter);
		taskInfoRepository.save(taskInfo);
	}

	@Transactional
	public void moveSubtask(TaskInfo subtask, SubtaskMove move) {
		subtask.setChoiceTask(dtoConverter.apply(move.getChoiceTask()));
		taskInfoRepository.save(subtask);
	}

	/**
	 * Move a subtask outside a choice task. The subtask can be moved to the same or a different skill, but
	 * not to a different choice task.
	 *
	 * @param  subtask The subtask.
	 * @param  move    The TaskMove, describing the new skill and the task index for the move.
	 * @return         The new regular task, created from the subtask.
	 */
	@Transactional
	public RegularTask moveSubtaskOutsideChoiceTask(TaskInfo subtask, TaskMove move) {
		Skill newSkill = dtoConverter.apply(move.getSkill());

		// Update other indices
		newSkill.getTasks().stream().filter(t -> t.getIdx() >= move.getIndex())
				.forEach(t -> t.setIdx(t.getIdx() + 1));
		taskRepository.saveAll(newSkill.getTasks());

		// Create new regular task
		RegularTask task = RegularTask.builder().skill(newSkill).idx(move.getIndex()).build();
		task.setTaskInfo(subtask);

		// Take over paths and additions/removals from choice task
		ChoiceTask choiceTask = subtask.getChoiceTask();
		task.getPaths().addAll(choiceTask.getPaths());
		task.getPersonsThatAddedTask().addAll(choiceTask.getPersonsThatAddedTask());
		task.getPersonsThatRemovedTask().addAll(choiceTask.getPersonsThatRemovedTask());

		task = regularTaskRepository.save(task);

		// Adjust task info
		subtask.setTask(task);
		subtask.setChoiceTask(null);
		taskInfoRepository.save(subtask);

		return task;
	}

	@Transactional
	public void deleteTaskInfo(TaskInfo taskInfo) {
		taskInfoRepository.delete(taskInfo);
	}
}
