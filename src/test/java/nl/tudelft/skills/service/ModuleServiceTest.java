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

import java.util.stream.Stream;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class ModuleServiceTest {

	@Autowired
	private TestDatabaseLoader db;
	@Autowired
	private ModuleService moduleService;

	@Test
	void setCompletedTasksForPerson() {
		ModuleLevelModuleViewDTO module = View.convert(db.getModuleProofTechniques(),
				ModuleLevelModuleViewDTO.class);
		moduleService.setCompletedTasksForPerson(module, TestUserDetailsService.id);
		assertThat(module.getSubmodules().stream()
				.flatMap(sub -> sub.getSkills().stream())
				.flatMap(skill -> skill.getTasks().stream())
				.filter(TaskViewDTO::isCompleted)
				.map(TaskViewDTO::getId).toList())
						.containsExactlyInAnyOrderElementsOf(
								Stream.of(db.taskRead11, db.taskDo11ad, db.taskRead12, db.taskDo12ae)
										.map(Task::getId).toList());

	}

}
