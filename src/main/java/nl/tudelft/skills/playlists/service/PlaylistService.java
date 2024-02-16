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
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.dto.PlaylistSkillDTO;
import nl.tudelft.skills.playlists.model.PlaylistVersion;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PersonService;

@Service
public class PlaylistService {
	private PlaylistRepository playlistRepository;

	private ResearchParticipantService researchParticipantService;

	private EditionRepository editionRepository;

	private PersonService personService;
	private TaskRepository taskRepository;
	private CheckpointRepository checkpointRepository;
	private PersonRepository personRepository;
	private Long ACCId = 643L;

	@Autowired
	public PlaylistService(PlaylistRepository playlistRepository,
			ResearchParticipantService researchParticipantService,
			EditionRepository editionRepository, PersonService personService, TaskRepository taskRepository,
			CheckpointRepository checkpointRepository, PersonRepository personRepository) {
		this.playlistRepository = playlistRepository;
		this.researchParticipantService = researchParticipantService;
		this.editionRepository = editionRepository;
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
		//		TODO: use DTOs

		Map<Long, List<Long>> tasks = new HashMap<>();

		//		For each task, get the module it belongs to and whether the student has already completed it
		for (Task t : skill.getTasks()) {
			boolean completed = taskCompletions.stream()
					.anyMatch(tC -> tC.getTask().getId().equals(t.getId()));
			tasks.put(t.getId(), List.of(getModuleId(t.getId()), completed ? 1L : 0L));
		}

		return tasks;
	}

	public Map<PlaylistSkillDTO, Map<Long, List<Long>>> getSkills(Long personId, Long checkpointId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(checkpointId);
		//		TODO: filter out skills not revealed yet
		List<Skill> skills = checkpoint.getSkills().stream().toList();
		Map<PlaylistSkillDTO, Map<Long, List<Long>>> items = new HashMap<>();

		Set<TaskCompletion> taskCompletions = person.getTaskCompletions();
		List<Long> complTaskIds = taskCompletions.stream().map(TaskCompletion::getTask).map(Task::getId)
				.toList();
		for (Skill skill : skills) {
			int remainingTime = getSkillRemainingTasks(skill, complTaskIds).stream()
					.map(taskRepository::findByIdOrThrow).map(Task::getTime).reduce(0, Integer::sum);
			PlaylistSkillDTO view = View.convert(skill, PlaylistSkillDTO.class);
			view.setTotalTime(remainingTime);
			items.put(view, getTasks(taskCompletions, skill));
		}
		return items;
	}

	private List<Long> getCheckpointRemainingTasks(Checkpoint checkpoint, List<Long> complTaskIds) {

		return checkpoint.getSkills().stream().map(s -> getSkillRemainingTasks(s, complTaskIds))
				.flatMap(Collection::stream).toList();
	}

	private List<Long> getSkillRemainingTasks(Skill skill, List<Long> complTaskIds) {
		return skill.getTasks().stream().map(Task::getId).filter(t -> !complTaskIds.contains(t)).toList();

	}

	public Long getCheckpoints(Long personId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		SCEdition edition = editionRepository.findByIdOrThrow(2L);

		//		Sort checkpoint according to their deadline
		List<Checkpoint> checkpoints = edition.getCheckpoints().stream()
				.sorted(Comparator.comparing(Checkpoint::getDeadline)).toList();
		//		Get all taskcompletions for a student
		List<Long> complTaskIds = person.getTaskCompletions().stream().map(TaskCompletion::getTask)
				.map(Task::getId).toList();
		Map<Checkpoint, Integer> times = new HashMap<>();

		for (Checkpoint cp : checkpoints) {

			List<Long> remainingTasks = getCheckpointRemainingTasks(cp, complTaskIds);
			if (!remainingTasks.isEmpty()) {
				int remainingTime = remainingTasks.stream().map(taskRepository::findByIdOrThrow)
						.map(Task::getTime).reduce(0, Integer::sum);
				times.put(cp, remainingTime);
			}
			//check if any skills are not yet completed. if so, return that checkpoint
		}
		return 1L;
	}

	public int getPlaylistTotalTime(Long id) {
		PlaylistVersion playlist = playlistRepository.findByIdOrThrow(id).getLatestVersion();
		return playlist.getTotalTime();
	}
}
