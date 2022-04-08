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

import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CheckpointServiceTest {

	private final CheckpointService checkpointService;
	private final CheckpointRepository checkpointRepository;
	private final TestDatabaseLoader db;

	@Autowired
	public CheckpointServiceTest(CheckpointService checkpointService,
			CheckpointRepository checkpointRepository, TestDatabaseLoader db) {
		this.checkpointService = checkpointService;
		this.checkpointRepository = checkpointRepository;
		this.db = db;
	}

	@Test
	public void findNextCheckpoint() {
		assertThat(checkpointService
				.findNextCheckpoint(checkpointRepository.findByIdOrThrow(db.checkpointLectureOne.getId())))
						.hasValue(db.checkpointLectureTwo);
	}

	@Test
	public void findNextCheckpointLast() {
		assertThat(checkpointService
				.findNextCheckpoint(checkpointRepository.findByIdOrThrow(db.checkpointLectureTwo.getId())))
						.isEmpty();
	}

}
