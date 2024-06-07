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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.module.TaskViewDTO;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class ModuleServiceTest {

	private TestDatabaseLoader db;
	private final ModuleService moduleService;
	private final ModuleRepository moduleRepository;

	@Autowired
	public ModuleServiceTest(TestDatabaseLoader db, ModuleService moduleService,
			ModuleRepository moduleRepository) {
		this.db = db;
		this.moduleService = moduleService;
		this.moduleRepository = moduleRepository;
	}

	@Test
	void setCompletedTasksForPerson() {
		ModuleLevelModuleViewDTO module = View.convert(
				moduleRepository.findByIdOrThrow(db.getModuleProofTechniques().getId()),
				ModuleLevelModuleViewDTO.class);
		moduleService.setCompletedTasksForPerson(module, TestUserDetailsService.id);
		assertThat(module.getSubmodules().stream()
				.flatMap(sub -> sub.getSkills().stream())
				.flatMap(skill -> skill.getTasks().stream())
				.filter(TaskViewDTO::isCompleted)
				.map(TaskViewDTO::getId).toList())
				.containsExactlyInAnyOrderElementsOf(
						List.of(db.getTaskRead11().getId(),
								db.getTaskDo11ad().getId(),
								db.getTaskRead12().getId(),
								db.getTaskDo12ae().getId()));

	}

}
