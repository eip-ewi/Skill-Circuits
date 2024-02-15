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
package nl.tudelft.skills.playlists.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.SkillSummaryDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PersonService;

@Service
public class PlaylistService {
	private PlaylistRepository playlistRepository;

	private ResearchParticipantService researchParticipantService;

	private ModuleRepository moduleRepository;

	private PersonService personService;
	private TaskRepository taskRepository;
	private CheckpointRepository checkpointRepository;
	private PersonRepository personRepository;
	private Long ACCId = 643L;

	@Autowired
	public PlaylistService(PlaylistRepository playlistRepository,
			ResearchParticipantService researchParticipantService,
			ModuleRepository moduleRepository, PersonService personService, TaskRepository taskRepository,
			CheckpointRepository checkpointRepository, PersonRepository personRepository) {
		this.playlistRepository = playlistRepository;
		this.researchParticipantService = researchParticipantService;
		this.moduleRepository = moduleRepository;
		this.personService = personService;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;
		this.personRepository = personRepository;
	}

	public String getDefaultPathForEdition(Long personId) {
		Long editionId = 2L;
		Optional<PathPreference> pathPref = personService.getPathForEdition(personId, editionId);
		if (pathPref.isEmpty()) {
			return "not selected";
		} else {
			PathPreference pp = pathPref.get();
			if (pp.getPath() != null) {
				return pp.getPath().getName();
			}
			return "not selected";
		}
	}

	public Long getModuleId(Long taskId) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		return task.getSkill().getSubmodule().getModule().getId();
	}

	public String getTaskName(Long taskId) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		return task.getName();
	}

	public Map<Long, List<Long>> getTasks(Set<TaskCompletion> taskCompletions, Skill skill) {

		Map<Long, List<Long>> tasks = new HashMap<>();

		//		For each task, get the module it belongs to and whether the student has already completed it
		for (Task t : skill.getTasks()) {
			boolean completed = taskCompletions.stream()
					.anyMatch(tC -> tC.getTask().getId().equals(t.getId()));
			tasks.put(t.getId(), List.of(getModuleId(t.getId()), completed ? 1L : 0L));
		}

		return tasks;
	}

	public Map<SkillSummaryDTO, Map<Long, List<Long>>> getSkills(Long personId, Long checkpointId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(checkpointId);
		List<Skill> skills = checkpoint.getSkills().stream().toList();
		Map<SkillSummaryDTO, Map<Long, List<Long>>> items = new HashMap<>();

		Set<TaskCompletion> taskCompletions = person.getTaskCompletions();
		for (Skill skill : skills) {
			items.put(View.convert(skill, SkillSummaryDTO.class),
					getTasks(taskCompletions, skill));
		}
		return items;
	}
}
