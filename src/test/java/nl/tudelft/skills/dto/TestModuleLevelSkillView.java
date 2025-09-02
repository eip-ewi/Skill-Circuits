// /*
//  * Skill Circuits
//  * Copyright (C) 2025 - Delft University of Technology
//  *
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License as
//  * published by the Free Software Foundation, either version 3 of the
//  * License, or (at your option) any later version.
//  *
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
//  */
// package nl.tudelft.skills.dto;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertTrue;
//
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;
//
// import nl.tudelft.librador.dto.view.View;
// import nl.tudelft.skills.TestSkillCircuitsApplication;
// import nl.tudelft.skills.dto.view.module.ChoiceTaskViewDTO;
// import nl.tudelft.skills.dto.view.module.ModuleLevelSkillViewDTO;
// import nl.tudelft.skills.dto.view.module.RegularTaskViewDTO;
// import nl.tudelft.skills.test.TestDatabaseLoader;
//
// @Transactional
// @SpringBootTest(classes = TestSkillCircuitsApplication.class)
// public class TestModuleLevelSkillView {
// 	private final TestDatabaseLoader db;
//
// 	@Autowired
// 	public TestModuleLevelSkillView(TestDatabaseLoader db) {
// 		this.db = db;
// 	}
//
// 	@Test
// 	public void testRedundantTasksAreRemoved() {
// 		// Check expected initial state
// 		assertThat(db.getSkillVariables().getTasks()).hasSize(5);
//
// 		ModuleLevelSkillViewDTO view = View.convert(db.getSkillVariables(), ModuleLevelSkillViewDTO.class);
//
// 		// Assert that redundant regular tasks are removed
// 		assertThat(view.getTasks()).hasSize(3);
// 		assertTrue(view.getTasks().contains(View.convert(db.getTaskRead10(), RegularTaskViewDTO.class)));
// 		assertTrue(view.getTasks().contains(View.convert(db.getTaskDo10a(), RegularTaskViewDTO.class)));
// 		assertTrue(view.getTasks()
// 				.contains(View.convert(db.getChoiceTaskBookOrVideo(), ChoiceTaskViewDTO.class)));
// 	}
// }
