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
package nl.tudelft.skills.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.tudelft.skills.controller.old.ControllerTest;
import nl.tudelft.skills.service.old.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.id.SCEditionId;
import nl.tudelft.skills.model.Path;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PathService;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class PathControllerTest extends ControllerTest {
//	private final PathController pathController;
//	private final PathController pathRepository;
//	private final EditionRepository editionRepository;
//	private final TaskRepository taskRepository;
//	private final PathPreferenceRepository pathPreferenceRepository;
//	private final PersonRepository personRepository;
//	private final PersonService personService;
//
//	@Autowired
//	public PathControllerTest(PathController pathRepository, PersonRepository personRepository,
//                              PersonService personService, EditionRepository editionRepository,
//                              PathPreferenceRepository pathPreferenceRepository, TaskRepository taskRepository,
//                              PathService pathService) {
//		this.pathRepository = pathRepository;
//		this.pathPreferenceRepository = pathPreferenceRepository;
//		this.editionRepository = editionRepository;
//		this.taskRepository = taskRepository;
//		this.personRepository = personRepository;
//		this.personService = personService;
//		pathController = new PathController(pathRepository, personRepository, editionRepository,
//				taskRepository, pathPreferenceRepository, personService, pathService);
//	}
//
//	@Test
//	@WithUserDetails("username")
//	void updateUserPathPreference() throws Exception {
//		Long editionId = db.getEditionRL().getId();
//		Long pathId = db.getPathFinderPath().getId();
//
//		mvc.perform(post("/path/" + editionId + "/preference").with(csrf())
//				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
//						new BasicNameValuePair("editionId", editionId.toString()),
//						new BasicNameValuePair("pathId", pathId.toString())))))
//				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
//				.andExpect(status().isOk());
//
//		assertTrue(pathPreferenceRepository.existsByPathId(pathId));
//
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void updateDefaultPathForEdition() throws Exception {
//		Long pathId = db.getPathFinderPath().getId();
//
//		mvc.perform(post("/path/" + db.getEditionRL().getId() + "/default").with(csrf())
//				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
//						new BasicNameValuePair("pathId", pathId.toString())))))
//				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
//				.andExpect(status().isOk());
//
//		// Checks if default path was reset
//		assertEquals(db.getPathFinderPath().getId(), db.getEditionRL().getDefaultPath().getId());
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void deletePath() {
//		Long pathId = db.getPathFinderPath().getId();
//		pathController.deletePath(pathId);
//
//		assertFalse(pathRepository.existsById(pathId));
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void deletePathIsDefault() {
//		Long pathId = db.getPathFinderPath().getId();
//		db.getEditionRL().setDefaultPath(db.getPathFinderPath());
//		pathController.deletePath(pathId);
//
//		// Checks if default path was reset
//		assertNull(db.getEditionRL().getDefaultPath());
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void deletePathHasTask() {
//		Long pathId = db.getPathFinderPath().getId();
//
//		// Setup task with path
//		RegularTask task = db.getTaskRead12();
//		task.setPaths(new HashSet<>(Arrays.asList(db.getPathFinderPath())));
//		taskRepository.save(task);
//		assertEquals(1, db.getTaskRead12().getPaths().size());
//
//		pathController.deletePath(pathId);
//
//		// Checks that deleted path doesn't appear in task
//		assertEquals(0, db.getTaskRead12().getPaths().size());
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void deletePathHasPathPref() {
//		Long pathId = db.getPathFinderPath().getId();
//
//		// Setup path preference with path
//		PathPreference pathPreference = PathPreference.builder().path(db.getPathFinderPath())
//				.edition(db.getEditionRL()).person(db.getPerson()).build();
//		pathPreferenceRepository.save(pathPreference);
//
//		assertTrue(pathPreferenceRepository.existsByPathId(pathId));
//
//		pathController.deletePath(pathId);
//
//		// Checks that deleted path doesn't appear in path preference
//		assertFalse(pathPreferenceRepository.existsByPathId(pathId));
//
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void createPath() {
//		var dto = PathCreateDTO.builder()
//				.name("PathName")
//				.edition(new SCEditionId(db.getEditionRL().getId()))
//				.build();
//		pathController.createPathSetup(dto, mock(Model.class));
//
//		assertTrue(pathRepository.findAll().stream().anyMatch(p -> p.getName().equals("PathName")));
//		// Check that all existing tasks get added to this path
//		assertTrue(db.getTaskRead12().getPaths().stream().anyMatch(p -> p.getName().equals("PathName")));
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void patchPathChangeName() {
//		Long pathId = db.getPathFinderPath().getId();
//		PathNamePatchDTO dto = PathNamePatchDTO.builder()
//				.id(pathId)
//				.name("Pathfinder2")
//				.build();
//
//		// Assert that task read 12 is only in the pathfinder path
//		assertEquals(Set.of(db.getPathFinderPath()), db.getTaskRead12().getPaths());
//		assertTrue(db.getTaskRead12().getPaths().stream().anyMatch(p -> p.getName().equals("Pathfinder")));
//
//		// Rename the path
//		pathController.renamePath(dto);
//
//		// Assert on path name change
//		assertFalse(db.getTaskRead12().getPaths().stream().anyMatch(p -> p.getName().equals("Pathfinder")));
//		assertTrue(db.getTaskRead12().getPaths().stream().anyMatch(p -> p.getName().equals("Pathfinder2")));
//
//		Path pathAfter = pathRepository.findByIdOrThrow(pathId);
//		assertThat(pathAfter.getName()).isEqualTo("Pathfinder2");
//	}
//
//	@Test
//	@WithUserDetails("admin")
//	void patchPathChangeNameOfDefaultPath() {
//		PathNamePatchDTO dto = PathNamePatchDTO.builder()
//				.id(db.getPathFinderPath().getId())
//				.name("Pathfinder2")
//				.build();
//
//		// Set Pathfinder as default path in edition
//		SCEdition edition = db.getEditionRL();
//		edition.setDefaultPath(db.getPathFinderPath());
//		editionRepository.save(edition);
//		assertEquals("Pathfinder", db.getEditionRL().getDefaultPath().getName());
//
//		// Rename the path
//		pathController.renamePath(dto);
//
//		// Default path in edition was updated
//		assertEquals("Pathfinder2", db.getEditionRL().getDefaultPath().getName());
//	}
//
}
