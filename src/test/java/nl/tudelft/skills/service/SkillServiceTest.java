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
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SkillServiceTest {

	private final AbstractSkillRepository skillRepository;
	private final SkillService skillService;
	private final TaskCompletionRepository taskCompletionRepository;

	private TestDatabaseLoader db;

	@Autowired
	public SkillServiceTest(AbstractSkillRepository skillRepository, TaskRepository taskRepository,
			TestDatabaseLoader db, TaskCompletionRepository taskCompletionRepository) {
		this.skillRepository = skillRepository;
		this.db = db;
		this.taskCompletionRepository = taskCompletionRepository;
		this.skillService = new SkillService(skillRepository, taskCompletionRepository);
	}

	@Test
	public void deleteSkill() {
		Long id = db.getSkillVariables().getId();

		skillService.deleteSkill(id);
		assertThat(skillRepository.existsById(id)).isFalse();

		// Both of the previously saved TaskCompletions were in the deleted Skill, so
		// the size should be 0 now
		assertThat(taskCompletionRepository.findAll()).hasSize(0);
	}

	@Test
	public void deleteSkillTaskCompletionsNotImpacted() {
		Long id = db.getSkillAssumption().getId();

		skillService.deleteSkill(id);
		assertThat(skillRepository.existsById(id)).isFalse();

		// There are two TaskCompletions saved which are in another Skill, so they should
		// not have been deleted
		assertThat(taskCompletionRepository.findAll()).hasSize(2);
	}

}
