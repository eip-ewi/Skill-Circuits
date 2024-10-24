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
package nl.tudelft.skills.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.edition.EditionLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.repository.RegularTaskRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TestEditionLevelModuleViewDTO {
	private final TestDatabaseLoader db;

	private final RegularTaskRepository regularTaskRepository;

	@Autowired
	public TestEditionLevelModuleViewDTO(TestDatabaseLoader db, RegularTaskRepository regularTaskRepository) {
		this.db = db;
		this.regularTaskRepository = regularTaskRepository;
	}

	@Test
	public void testTasksWithLinksEmpty() {
		EditionLevelModuleViewDTO view = View.convert(db.getModuleProofTechniques(),
				EditionLevelModuleViewDTO.class);
		assertThat(view.getTasksWithLinks()).isEmpty();
	}

	@Test
	public void testTasksWithLinks() {
		RegularTask task = db.getTaskDo10a();
		task.setLink("www.test.com");
		regularTaskRepository.save(task);

		EditionLevelModuleViewDTO view = View.convert(db.getModuleProofTechniques(),
				EditionLevelModuleViewDTO.class);
		TaskViewDTO taskViewDTO = View.convert(task, TaskViewDTO.class);
		assertThat(view.getTasksWithLinks()).containsExactly(taskViewDTO);
	}
}
