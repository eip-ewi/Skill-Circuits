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

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.dto.PlaylistCheckpointDTO;
import nl.tudelft.skills.playlists.dto.PlaylistSkillDTO;
import nl.tudelft.skills.playlists.dto.PlaylistTaskDTO;
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

	public List<PlaylistTaskDTO> getTaskDTOs(Set<TaskCompletion> taskCompletions, Skill skill) {
		List<PlaylistTaskDTO> tasks = new LinkedList<>();

		//		For each task, get the module it belongs to and whether the student has already completed it
		for (Task t : skill.getTasks()) {
			boolean completed = taskCompletions.stream()
					.anyMatch(tC -> tC.getTask().getId().equals(t.getId()));
			if (completed) {
				//				Using existing task id for now
				tasks.add(PlaylistTaskDTO.builder().id(t.getId()).type(t.getType())
						.moduleId(getModuleId(t.getId())).skillId(skill.getId())
						.completed(LocalDateTime.now()).taskName(t.getName()).build());
			} else {
				//				Using existing task id for now
				tasks.add(PlaylistTaskDTO.builder().id(t.getId()).type(t.getType())
						.moduleId(getModuleId(t.getId())).skillId(skill.getId())
						.started(LocalDateTime.now()).taskName(t.getName()).build());
			}

		}
		return tasks;
	}

	public List<PlaylistTaskDTO> getTaskDTOs(List<Long> taskIds) {
		List<PlaylistTaskDTO> tasks = new LinkedList<>();
		for (Long id : taskIds) {
			Task t = taskRepository.findByIdOrThrow(id);
			//			Using existing task id for now
			tasks.add(PlaylistTaskDTO.builder().id(t.getId()).type(t.getType()).idx(t.getIdx())
					.estTime(t.getTime())
					.moduleId(getModuleId(t.getId())).skillId(t.getSkill().getId())
					.taskName(t.getName()).build());
		}
		return tasks;
	}

	public Map<PlaylistSkillDTO, List<PlaylistTaskDTO>> getSkills(Long personId, Long checkpointId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(checkpointId);
		//		TODO: filter out skills not revealed yet
		List<Skill> skills = checkpoint.getSkills().stream().toList();
		Map<PlaylistSkillDTO, List<PlaylistTaskDTO>> items = new HashMap<>();

		Set<TaskCompletion> taskCompletions = person.getTaskCompletions();
		List<Long> complTaskIds = taskCompletions.stream().map(TaskCompletion::getTask).map(Task::getId)
				.toList();
		for (Skill skill : skills) {
			int remainingTime = getSkillRemainingTasks(skill, complTaskIds).stream()
					.map(taskRepository::findByIdOrThrow).map(Task::getTime).reduce(0, Integer::sum);
			PlaylistSkillDTO view = View.convert(skill, PlaylistSkillDTO.class);
			view.setTotalTime(remainingTime);
			items.put(view, getTaskDTOs(taskCompletions, skill));
		}
		return items;
	}

	private List<PlaylistSkillDTO> getCheckpointRemainingSkills(Checkpoint checkpoint,
			List<Long> complTaskIds) {
		List<PlaylistSkillDTO> skills = new LinkedList<>();
		for (Skill s : checkpoint.getSkills()) {
			//			TODO: don't include hidden skills
			PlaylistSkillDTO skillDTO = View.convert(s, PlaylistSkillDTO.class);
			skillDTO.postApply(taskRepository, getTaskDTOs(getSkillRemainingTasks(s, complTaskIds)));
			skills.add(skillDTO);
		}
		return Collections.unmodifiableList(skills);
	}

	private List<Long> getSkillRemainingTasks(Skill skill, List<Long> complTaskIds) {
		return skill.getTasks().stream().map(Task::getId).filter(t -> !complTaskIds.contains(t)).toList();

	}

	public List<PlaylistCheckpointDTO> getCheckpointDTOs(Long personId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		SCEdition edition = editionRepository.findByIdOrThrow(2L);

		//		Sort checkpoint according to their deadline
		List<Checkpoint> checkpoints = edition.getCheckpoints().stream()
				.sorted(Comparator.comparing(Checkpoint::getDeadline)).toList();

		//		Get all taskcompletions for a student
		List<Long> complTaskIds = person.getTaskCompletions().stream().map(TaskCompletion::getTask)
				.map(Task::getId).toList();
		List<PlaylistCheckpointDTO> checkpointDTOs = new LinkedList<>();

		//		For each checkpoint, get uncompleted skills
		for (Checkpoint cp : checkpoints) {

			List<PlaylistSkillDTO> remainingSkills = getCheckpointRemainingSkills(cp, complTaskIds);
			if (!remainingSkills.isEmpty()) {
				PlaylistCheckpointDTO cpDTO = View.convert(cp, PlaylistCheckpointDTO.class);
				cpDTO.postApply(remainingSkills);
				int remainingTime = remainingSkills.stream().map(PlaylistSkillDTO::getTotalTime).reduce(0,
						Integer::sum);
				checkpointDTOs.add(cpDTO);
			}
		}
		return Collections.unmodifiableList(checkpointDTOs);
	}

	public int getPlaylistTotalTime(Long id) {
		PlaylistVersion playlist = playlistRepository.findByIdOrThrow(id).getLatestVersion();
		return playlist.getTotalTime();
	}
}
