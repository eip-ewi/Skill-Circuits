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
package nl.tudelft.skills.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.TaskCompletionRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class InfoboxControllerTest extends ControllerTest {

	private final EditionControllerApi editionApi;
	private final TaskCompletionRepository taskCompletionRepository;

	@Autowired
	public InfoboxControllerTest(TaskCompletionRepository taskCompletionRepository,
			EditionControllerApi editionApi) {
		this.editionApi = editionApi;
		this.taskCompletionRepository = taskCompletionRepository;
	}

	private void resetTaskCompletions() {
		db.getPerson().setTaskCompletions(new HashSet<>());
		db.getTaskDo11ad().setCompletedBy(new HashSet<>());
		db.getTaskRead12().setCompletedBy(new HashSet<>());
		db.getTaskDo12ae().setCompletedBy(new HashSet<>());
		db.getTaskRead11().setCompletedBy(new HashSet<>());
		taskCompletionRepository.deleteAll();
	}

	@Test
	@WithAnonymousUser
	public void testUnauthenticatedUser() throws Exception {
		mvc.perform(get("/infobox"))
				.andExpect(status().isOk())
				.andExpect(content().json("{'studentAndAuthenticated':false}"));
	}

	@Test
	@WithUserDetails("teacher")
	public void testTeacherUserNoCompletions() throws Exception {
		// Remove all task completions from the repository
		// In the setup the teacher has task completions
		resetTaskCompletions();

		mvc.perform(get("/infobox"))
				.andExpect(status().isOk())
				.andExpect(content().json("{'studentAndAuthenticated':false}"));
	}

	@Test
	@WithUserDetails("username")
	public void testStudentWithCompletions() throws Exception {
		SCEdition dbEdition = db.getEditionRL();
		SCCourse dbCourse = db.getCourseRL();
		EditionDetailsDTO editionDetailsDTO = new EditionDetailsDTO().id(dbEdition.getId()).name("edition")
				.course(new CourseSummaryDTO().id(dbCourse.getId()).name("Reasoning and Logic"));

		when(editionApi.getEditionById(db.getEditionRL().getId())).thenReturn(Mono.just(editionDetailsDTO));

		Long moduleId = db.getModuleProofTechniques().getId();
		Long skillId = db.getSkillNegation().getId();

		mvc.perform(get("/infobox"))
				.andExpect(status().isOk())
				.andExpect(content().json("{'studentAndAuthenticated':true, 'completedSomeTask':true," +
						" 'taskInfo':'Last worked on: Read chapter 1.1', " +
						"'link':'/module/" + moduleId + "#block-" + skillId + "-name', " +
						"'locationString':'In Reasoning and Logic - Proof Techniques - Logic Basics - Negation'}"));
	}

	@Test
	@WithUserDetails("username")
	public void testStudentNoCompletions() throws Exception {
		// Remove all task completions from the repository
		// In the setup the student has task completions
		resetTaskCompletions();

		mvc.perform(get("/infobox"))
				.andExpect(status().isOk())
				.andExpect(content().json("{'studentAndAuthenticated':true, 'completedSomeTask':false}"));
	}

}
