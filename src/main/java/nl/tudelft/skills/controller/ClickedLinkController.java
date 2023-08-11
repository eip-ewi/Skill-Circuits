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

import javax.transaction.Transactional;

import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.ClickedLink;
import nl.tudelft.skills.model.ClickedLinkSerializer;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.ClickedLinkRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/clicked_links")
public class ClickedLinkController {
	private final TaskRepository taskRepository;
	private final PersonRepository scPersonRepository;
	private final ClickedLinkRepository clickedLinkRepository;

	public ClickedLinkController(TaskRepository taskRepository, PersonRepository scPersonRepository,
			ClickedLinkRepository clickedLinkRepository) {
		this.taskRepository = taskRepository;
		this.scPersonRepository = scPersonRepository;
		this.clickedLinkRepository = clickedLinkRepository;
	}

	/**
	 * Saves the clicked link by a person
	 *
	 * @param authPerson the person clicking the link
	 * @param taskId     the id of the task the link is part of
	 */
	@PutMapping("clicked/{taskId}")
	@Transactional
	public void logClickedLinkByPerson(@AuthenticatedPerson Person authPerson,
			@PathVariable Long taskId) {
		SCPerson person = scPersonRepository.findByIdOrThrow(authPerson.getId());
		Task task = taskRepository.findByIdOrThrow(taskId);

		clickedLinkRepository.save(ClickedLink.builder()
				.task(task).person(person).build());

	}

	/**
	 * Displays the content of the Clicked link table in json format. Only accessible to admins.
	 *
	 * @return                         the entries in the Clicked link table in json format
	 * @throws JsonProcessingException
	 */
	@GetMapping("download")
	@Transactional
	@PreAuthorize("@authorisationService.isAdmin()")
	public String downloadClickedLinks() throws JsonProcessingException {
		var logs = clickedLinkRepository.findAll();

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		SimpleModule module = new SimpleModule();
		module.addSerializer(ClickedLink.class, new ClickedLinkSerializer());
		mapper.registerModule(module);

		return mapper.writeValueAsString(logs);
	}
}
