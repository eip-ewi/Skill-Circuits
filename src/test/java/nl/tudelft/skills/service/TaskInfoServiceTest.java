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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.repository.RegularTaskRepository;
import nl.tudelft.skills.repository.TaskInfoRepository;
import nl.tudelft.skills.repository.TaskRepository;

public class TaskInfoServiceTest {

	private final TaskInfoRepository taskInfoRepository = mock(TaskInfoRepository.class);
	private final TaskRepository taskRepository = mock(TaskRepository.class);
	private final RegularTaskRepository regularTaskRepository = mock(RegularTaskRepository.class);
	private final DTOConverter dtoConverter = mock(DTOConverter.class);

	private final TaskInfoService taskInfoService = new TaskInfoService(taskInfoRepository, taskRepository,
			regularTaskRepository, dtoConverter);

	@Test
	public void setTaskDeadlineUpdatesAndSavesDeadline() {
		TaskInfo taskInfo = TaskInfo.builder().name("Task").build();
		LocalDateTime deadline = LocalDateTime.of(2026, 4, 13, 10, 30);

		taskInfoService.setTaskDeadline(taskInfo, deadline);

		assertThat(taskInfo.getDeadline()).isEqualTo(deadline);
		verify(taskInfoRepository).save(taskInfo);
	}

	@Test
	public void clearTaskDeadlineClearsAndSavesDeadline() {
		TaskInfo taskInfo = TaskInfo.builder().name("Task").deadline(LocalDateTime.of(2026, 4, 13, 10, 30))
				.build();

		taskInfoService.clearTaskDeadline(taskInfo);

		assertThat(taskInfo.getDeadline()).isNull();
		verify(taskInfoRepository).save(taskInfo);
	}
}
