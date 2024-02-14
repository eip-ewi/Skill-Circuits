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
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.service.PersonService;

@Service
public class PlaylistService {
	private PlaylistRepository playlistRepository;

	private ResearchParticipantService researchParticipantService;

	private ModuleRepository moduleRepository;

	private PersonService personService;
	private TaskRepository taskRepository;
	private CheckpointRepository checkpointRepository;
	private SkillRepository skillRepository;

	@Autowired
	public PlaylistService(PlaylistRepository playlistRepository,
			ResearchParticipantService researchParticipantService,
			ModuleRepository moduleRepository, PersonService personService, TaskRepository taskRepository,
			CheckpointRepository checkpointRepository, SkillRepository skillRepository) {
		this.playlistRepository = playlistRepository;
		this.researchParticipantService = researchParticipantService;
		this.moduleRepository = moduleRepository;
		this.personService = personService;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;
		this.skillRepository = skillRepository;
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

	public Set<Long> getTasks(Long personId) {
		ModuleLevelModuleViewDTO module = View.convert(moduleRepository.findByIdOrThrow(3L),
				ModuleLevelModuleViewDTO.class);

		if (personService.getPathForEdition(personId, 2L).isEmpty()) {
			return new HashSet<>();
		}
		Path path = personService.getPathForEdition(personId, 2L).get().getPath();

		Set<Long> taskIds = path == null ? new HashSet<>()
				: path.getTasks().stream().map(Task::getId).collect(Collectors.toSet());

		if (path != null) {
			// if path is selected (doesn't apply for no-path), show only tasks on followed path
			// tasks not in path get visibility property false
			module.getSubmodules().stream().flatMap(s -> s.getSkills().stream()).forEach(
					s -> s.setTasks(s.getTasks().stream().map(t -> {
						t.setVisible(taskIds.contains(t.getId()));
						return t;
					}).toList()));

		}
		return taskIds;
	}

	public Map<String, Set<Long>> getSkills(Long personId) {
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(13L);
		Set<Skill> skills = checkpoint.getSkills();
		Map<String, Set<Long>> items = new HashMap<>();
		for (Skill skill : skills) {
			items.put(skill.getName(),
					skill.getTasks().stream().map(Task::getId).collect(Collectors.toSet()));
		}
		return items;
	}
}
