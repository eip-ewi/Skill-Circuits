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
// package nl.tudelft.skills.controller;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// import java.util.List;
//
// import nl.tudelft.skills.controller.old.ClickedLinkController;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.test.context.support.WithUserDetails;
// import org.springframework.transaction.annotation.Transactional;
//
// import nl.tudelft.labracore.lib.security.user.Person;
// import nl.tudelft.skills.TestSkillCircuitsApplication;
// import nl.tudelft.skills.dto.old.view.ClickedLinkDTO;
// import nl.tudelft.skills.repository.ClickedLinkRepository;
// import nl.tudelft.skills.repository.RegularTaskRepository;
// import nl.tudelft.skills.repository.PersonRepository;
//
// @Transactional
// @AutoConfigureMockMvc
// @SpringBootTest(classes = TestSkillCircuitsApplication.class)
// class ClickedLinkControllerTest extends ControllerTest {
//
// 	private final ClickedLinkController clickedLinkController;
// 	private final PersonRepository personRepository;
// 	private final RegularTaskRepository regularTaskRepository;
// 	private final ClickedLinkRepository clickedLinkRepository;
//
// 	@Autowired
// 	public ClickedLinkControllerTest(ClickedLinkController clickedLinkController,
// 			PersonRepository personRepository, RegularTaskRepository regularTaskRepository,
// 			ClickedLinkRepository clickedLinkRepository) {
// 		this.clickedLinkController = clickedLinkController;
// 		this.personRepository = personRepository;
// 		this.regularTaskRepository = regularTaskRepository;
// 		this.clickedLinkRepository = clickedLinkRepository;
// 	}
//
// 	@Test
// 	void logClickedLinkByPerson() {
// 		Person person = new Person();
// 		person.setId(db.getPerson().getId());
// 		Long taskId = db.getTaskRead11().getId();
// 		clickedLinkController.logClickedLinkByPerson(person, taskId);
// 		assertTrue(
// 				clickedLinkRepository.findAll().stream().anyMatch(s -> s.getTask().getId().equals(taskId)
// 						&& s.getPerson().getId().equals(person.getId())));
// 	}
//
// 	@Test
// 	@WithUserDetails("admin")
// 	void downloadClickedLinks() {
// 		List<ClickedLinkDTO> clicks = clickedLinkController.showAllClickedLinks();
//
// 		assertEquals("Read chapter 1.1", clicks.get(0).getTaskName());
// 		assertEquals("Negation", clicks.get(0).getSkillName());
// 		assertEquals(69, clicks.get(0).getEditionId());
// 	}
// }
