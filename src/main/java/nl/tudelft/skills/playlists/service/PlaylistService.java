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

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.dto.PlaylistCheckpointDTO;
import nl.tudelft.skills.playlists.dto.PlaylistSkillViewDTO;
import nl.tudelft.skills.playlists.dto.PlaylistTaskViewDTO;
import nl.tudelft.skills.playlists.dto.PlaylistViewDTO;
import nl.tudelft.skills.playlists.model.Playlist;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import nl.tudelft.skills.playlists.model.ResearchParticipant;
import nl.tudelft.skills.playlists.repository.PlaylistRepository;
import nl.tudelft.skills.playlists.repository.PlaylistTaskRepository;
import nl.tudelft.skills.playlists.repository.ResearchParticipantRepository;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.service.PersonService;

@Service
public class PlaylistService {
	private PlaylistRepository playlistRepository;
	private PlaylistTaskRepository playlistTaskRepository;

	private ResearchParticipantService researchParticipantService;

	private ResearchParticipantRepository researchParticipantRepository;
	private EditionRepository editionRepository;

	private PersonService personService;
	private TaskRepository taskRepository;
	private CheckpointRepository checkpointRepository;
	private PersonRepository personRepository;
	private SkillRepository skillRepository;
	private final Long ACCId = 2L;

	@Autowired
	public PlaylistService(PlaylistRepository playlistRepository,
			PlaylistTaskRepository playlistTaskRepository,
			ResearchParticipantRepository researchParticipantRepository,
			ResearchParticipantService researchParticipantService,
			EditionRepository editionRepository, PersonService personService, TaskRepository taskRepository,
			CheckpointRepository checkpointRepository, PersonRepository personRepository,
			SkillRepository skillRepository) {
		this.playlistRepository = playlistRepository;
		this.playlistTaskRepository = playlistTaskRepository;
		this.researchParticipantRepository = researchParticipantRepository;
		this.researchParticipantService = researchParticipantService;
		this.editionRepository = editionRepository;
		this.personService = personService;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;
		this.personRepository = personRepository;
		this.skillRepository = skillRepository;
	}

	public PlaylistViewDTO getPlaylist(Long personId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		ResearchParticipant participant = researchParticipantRepository
				.findByPerson(person);
		Playlist playlist = playlistRepository.findByParticipantAndActive(participant, true);

		//		Group selected tasks based on the skill they belong to
		Map<Long, List<PlaylistTaskViewDTO>> skills = new HashMap<>();
		for (PlaylistTask task : playlist.getLatestVersion().getTasks()) {
			Task t = taskRepository.findByIdOrThrow(task.getTaskId());

			//			Using existing task id for now
			PlaylistTaskViewDTO taskViewDTO = PlaylistTaskViewDTO.builder().taskId(t.getId())
					.type(t.getType()).idx(t.getIdx())
					.estTime(t.getTime()).completed(task.getCompleted())
					.moduleId(getModuleId(t.getId())).skillId(t.getSkill().getId())
					.taskName(t.getName()).build();
			if (skills.containsKey(t.getSkill().getId())) {
				skills.get(t.getSkill().getId()).add(taskViewDTO);
			} else {

				skills.put(t.getSkill().getId(), new LinkedList<>(List.of(taskViewDTO)));
			}
		}

		//		Sort each list tasks based on their idx (order within the skill)
		for (var entry : skills.entrySet()) {
			entry.getValue().sort(Comparator.comparingInt(PlaylistTaskViewDTO::getIdx).reversed());
		}

		//		Add tasks to one list in order of skills in the circuit
		List<Long> skillOrder = getSkillOrder(skills.keySet().stream().toList());
		List<PlaylistTaskViewDTO> tasks = new ArrayList<>();
		for (Long skillId : skillOrder) {
			tasks.addAll(skills.get(skillId));
		}
		return PlaylistViewDTO.builder()
				.id(playlist.getId())
				.created(playlist.getCreated().toLocalDate())
				.estTime(playlist.getLatestVersion().getTotalTime())
				.tasks(tasks).build();
	}

	public String getDefaultPathForEdition(Long personId) {
		Optional<PathPreference> pathPref = personService.getPathForEdition(personId, ACCId);
		if (pathPref.isPresent()) {
			return pathPref.get().getPath().getName();
		} else {
			return "Path not selected";
		}
	}

	public Long getModuleId(Long taskId) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		return task.getSkill().getSubmodule().getModule().getId();
	}

	private List<Long> getSkillOrder(List<Long> skillIds) {
		//		Does not take into account module order
		List<Skill> skills = skillIds.stream().map(skillRepository::findByIdOrThrow)
				.sorted(Comparator.comparingInt(Skill::getRow)).toList();
		return skills.stream().map(Skill::getId).toList();

	}

	public List<PlaylistTaskViewDTO> getTaskDTOs(Set<TaskCompletion> taskCompletions, Skill skill) {
		List<PlaylistTaskViewDTO> tasks = new LinkedList<>();

		//		For each task, get the module it belongs to and whether the student has already completed it
		for (Task t : skill.getTasks()) {
			boolean completed = taskCompletions.stream()
					.anyMatch(tC -> tC.getTask().getId().equals(t.getId()));
			if (completed) {
				//				Using existing task id for now
				tasks.add(PlaylistTaskViewDTO.builder().taskId(t.getId()).type(t.getType())
						.moduleId(getModuleId(t.getId())).skillId(skill.getId())
						.completed(true).taskName(t.getName()).build());
			} else {
				//				Using existing task id for now
				tasks.add(PlaylistTaskViewDTO.builder().taskId(t.getId()).type(t.getType())
						.moduleId(getModuleId(t.getId())).skillId(skill.getId())
						.started(LocalDateTime.now()).taskName(t.getName()).build());
			}

		}
		return tasks;
	}

	public void setPlTaskCompleted(SCPerson person, Task task, boolean completed) {
		ResearchParticipant participant = researchParticipantRepository.findByPerson(person);
		if (participant != null) {
			PlaylistTask plTask = playlistTaskRepository
					.findByParticipantAndTaskId(participant, task.getId());
			if (plTask != null) {
				plTask.setCompleted(completed);
			}
		}
	}

	public List<PlaylistTaskViewDTO> getTaskDTOs(List<Long> taskIds) {
		List<PlaylistTaskViewDTO> tasks = new LinkedList<>();
		for (Long id : taskIds) {
			Task t = taskRepository.findByIdOrThrow(id);
			//			Using existing task id for now
			tasks.add(PlaylistTaskViewDTO.builder().taskId(t.getId()).type(t.getType()).idx(t.getIdx())
					.estTime(t.getTime())
					.moduleId(getModuleId(t.getId())).skillId(t.getSkill().getId())
					.taskName(t.getName()).build());
		}
		return tasks;
	}

	public Map<PlaylistSkillViewDTO, List<PlaylistTaskViewDTO>> getSkills(Long personId, Long checkpointId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(checkpointId);
		//		TODO: filter out skills not revealed yet
		List<Skill> skills = checkpoint.getSkills().stream().toList();
		Map<PlaylistSkillViewDTO, List<PlaylistTaskViewDTO>> items = new HashMap<>();

		Set<TaskCompletion> taskCompletions = person.getTaskCompletions();
		List<Long> complTaskIds = taskCompletions.stream().map(TaskCompletion::getTask).map(Task::getId)
				.toList();
		for (Skill skill : skills) {
			int remainingTime = getSkillRemainingTasks(skill, complTaskIds).stream()
					.map(taskRepository::findByIdOrThrow).map(Task::getTime).reduce(0, Integer::sum);
			PlaylistSkillViewDTO view = View.convert(skill, PlaylistSkillViewDTO.class);
			view.setTotalTime(remainingTime);
			items.put(view, getTaskDTOs(taskCompletions, skill));
		}
		return items;
	}

	private List<PlaylistSkillViewDTO> getCheckpointRemainingSkills(Checkpoint checkpoint,
			List<Long> complTaskIds) {
		List<PlaylistSkillViewDTO> skills = new LinkedList<>();
		for (Skill s : checkpoint.getSkills()) {
			//	Filter out skills that are still hidden for the participant
			if(!complTaskIds.containsAll(s.getRequiredTasks().stream().map(Task::getId).toList())){
				continue;
			}
			PlaylistSkillViewDTO skillDTO = View.convert(s, PlaylistSkillViewDTO.class);
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

			List<PlaylistSkillViewDTO> remainingSkills = getCheckpointRemainingSkills(cp, complTaskIds);
			if (!remainingSkills.isEmpty()) {
				PlaylistCheckpointDTO cpDTO = View.convert(cp, PlaylistCheckpointDTO.class);
				cpDTO.postApply(remainingSkills);

				checkpointDTOs.add(cpDTO);
			}
		}
		return Collections.unmodifiableList(checkpointDTOs);
	}

	@Transactional
	public void deactivateActivePlaylist(ResearchParticipant participant) {
		Playlist playlist = playlistRepository.findByParticipantAndActive(participant, true);
		if (playlist != null) {
			playlist.setActive(false);
			playlistRepository.saveAndFlush(playlist);
		}

	}
}
