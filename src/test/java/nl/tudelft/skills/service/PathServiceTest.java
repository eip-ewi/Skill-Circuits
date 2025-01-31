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

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.patch.PathTasksPatchDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.repository.PathRepository;
import nl.tudelft.skills.repository.RegularTaskRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PathServiceTest {

	private final TestDatabaseLoader db;
	private final PathService pathService;
	private final PathRepository pathRepository;
	private final RegularTaskRepository regularTaskRepository;

	@Autowired
	public PathServiceTest(TestDatabaseLoader db, PathService pathService, PathRepository pathRepository,
			RegularTaskRepository regularTaskRepository) {
		this.db = db;
		this.pathService = pathService;
		this.pathRepository = pathRepository;
		this.regularTaskRepository = regularTaskRepository;
	}

	@Test
	void updateTasksInPathManyToManyWithoutModuleId() {
		// No module id, so the tasks should be reset
		Long pathId = db.getPathFinderPath().getId();
		PathTasksPatchDTO dto = PathTasksPatchDTO.builder()
				.id(pathId)
				.taskIds(Set.of(db.getTaskRead11().getId()))
				.build();

		// Assert that task read 12 is the only task in the pathfinder path
		assertThat(db.getTaskRead12().getPaths()).containsExactly(db.getPathFinderPath());
		assertThat(db.getPathFinderPath().getTasks()).containsExactly(db.getTaskRead12());
		assertThat(db.getTaskRead11().getPaths()).isEmpty();

		// Patch the path tasks
		pathService.updateTasksInPathManyToMany(dto, db.getPathFinderPath());

		// Assert that task read 11 is the only task in the pathfinder path
		Path pathAfter = pathRepository.findByIdOrThrow(pathId);
		assertThat(db.getTaskRead11().getPaths()).containsExactly(pathAfter);
		assertThat(pathAfter.getTasks()).containsExactly(db.getTaskRead11());
		assertThat(db.getTaskRead12().getPaths()).isEmpty();
	}

	@Test
	@WithUserDetails("admin")
	void updateTasksInPathManyToManyWithModuleId() {
		// Create a new module with a task
		RegularTask newTask = db.createTaskInNewModule();

		// Add new task to path
		Path path = db.getPathFinderPath();
		path.getTasks().add(newTask);
		newTask.getPaths().add(path);
		pathRepository.save(path);
		regularTaskRepository.save(newTask);

		// Set module to module of task read 12
		PathTasksPatchDTO dto = PathTasksPatchDTO.builder()
				.id(path.getId())
				.moduleId(db.getModuleProofTechniques().getId())
				.taskIds(Set.of(db.getTaskRead11().getId()))
				.build();

		// Assert that task read 12 and the new task are the only tasks in the pathfinder path
		assertThat(db.getTaskRead12().getPaths()).containsExactly(db.getPathFinderPath());
		assertThat(newTask.getPaths()).containsExactly(db.getPathFinderPath());
		assertThat(db.getPathFinderPath().getTasks()).containsExactlyInAnyOrder(db.getTaskRead12(), newTask);
		assertThat(db.getTaskRead11().getPaths()).isEmpty();

		// Patch the path tasks
		pathService.updateTasksInPathManyToMany(dto, db.getPathFinderPath());

		// Assert that the new task and task read 11 are the only tasks in the pathfinder path
		// Module id was set to the path of task read 12 and 11 -> remove task read 12, add task read 11
		Path pathAfter = pathRepository.findByIdOrThrow(path.getId());
		RegularTask taskNewAfter = regularTaskRepository.findByIdOrThrow(newTask.getId());
		RegularTask taskRead11After = db.getTaskRead11();
		assertThat(taskNewAfter.getPaths()).containsExactly(pathAfter);
		assertThat(taskRead11After.getPaths()).containsExactly(pathAfter);
		assertThat(pathAfter.getTasks()).containsExactlyInAnyOrder(taskNewAfter, taskRead11After);
		assertThat(db.getTaskRead12().getPaths()).isEmpty();
	}

}
