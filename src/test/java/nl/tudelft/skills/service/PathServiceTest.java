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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.patch.PathPatchDTO;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PathServiceTest {

	private TestDatabaseLoader db;
	private PathService pathService;

	@Autowired
	public PathServiceTest(TestDatabaseLoader db, PathService pathService) {
		this.db = db;
		this.pathService = pathService;
	}

	@Test
	void updateTasksInPathManyToMany() {
		Path path = db.getPathFinderPath();
		PathPatchDTO patchDTO = PathPatchDTO.builder().name("pathfinder").id(path.getId())
				.taskIds(List.of(db.getTaskRead11().getId())).build();
		pathService.updateTasksInPathManyToMany(patchDTO, path);

		assertTrue(db.getTaskRead11().getPaths().stream().anyMatch(p -> p.getId().equals(path.getId())));
		assertFalse(db.getTaskRead12().getPaths().stream().anyMatch(p -> p.getId().equals(path.getId())));

		assertFalse(db.getPathFinderPath().getTasks().contains(db.getTaskRead12()));
		assertTrue(db.getPathFinderPath().getTasks().contains(db.getTaskRead11()));
	}

}
