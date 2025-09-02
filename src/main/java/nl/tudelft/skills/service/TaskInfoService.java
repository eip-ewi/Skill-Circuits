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
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.repository.TaskInfoRepository;

@Service
@AllArgsConstructor
public class TaskInfoService {

	private final TaskInfoRepository taskInfoRepository;

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

	@Transactional
	public void deleteTaskInfo(TaskInfo taskInfo) {
		taskInfoRepository.delete(taskInfo);
	}
}
