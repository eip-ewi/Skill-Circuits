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
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.playlists.dto.PlaylistCheckpointDTO;
import nl.tudelft.skills.playlists.dto.PlaylistSkillViewDTO;
import nl.tudelft.skills.playlists.dto.PlaylistTaskViewDTO;
import nl.tudelft.skills.playlists.dto.PlaylistViewDTO;
import nl.tudelft.skills.playlists.model.Playlist;
import nl.tudelft.skills.playlists.model.PlaylistTask;
import nl.tudelft.skills.playlists.model.PlaylistVersion;
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

@Slf4j
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
	private final Long ACCId = 643L;

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

	/**
	 * Gets the active playlist for a given ResearchParticipant
	 *
	 * @param  personId person that should be ResearchParticipant
	 * @return          a PlaylistViewDTO for the found active playlist
	 */
	public PlaylistViewDTO getPlaylist(Long personId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		ResearchParticipant participant = researchParticipantRepository
				.findByPerson(person);
		Playlist playlist = playlistRepository.findByParticipantAndActive(participant, true);

		//		Group selected tasks based on the skill they belong to
		Map<Long, List<PlaylistTaskViewDTO>> skills = new HashMap<>();
		for (PlaylistTask task : playlist.getLatestVersion().getTasks()) {
			Task t = taskRepository.findByIdOrThrow(task.getTaskId());
			int cTime = playlistTaskRepository.findByParticipantAndTaskId(participant, t.getId())
					.getCompletionTime();
			//			Using existing task id for now
			PlaylistTaskViewDTO taskViewDTO = PlaylistTaskViewDTO.builder().taskId(t.getId())
					.type(t.getType()).idx(t.getIdx())
					.estTime(t.getTime()).completed(task.getCompleted())
					.moduleId(getModuleId(t.getId())).skillId(t.getSkill().getId())
					.taskName(t.getName())
					.completionTime(cTime)
					.build();
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
				.estTime(playlist.getLatestVersion().getEstimatedTime())
				.completed(playlistCompleted(playlist.getLatestVersion()))
				.tasks(tasks).build();
	}

	/**
	 * Returns whether a playlist's version is completed
	 *
	 * @param  version The playlist version to check
	 * @return         true if the playlist version is completed, else false
	 */
	private boolean playlistCompleted(PlaylistVersion version) {
		return version.getTasks().stream().allMatch(PlaylistTask::getCompleted);

	}

	/**
	 * Gets the ResearchParticipant's selected path for the ACC edition
	 *
	 * @param  personId person that is a ResearchParticipant
	 * @return          the name of the selected path or a default string if not set
	 */
	public String getDefaultPathForEdition(Long personId) {
		Optional<PathPreference> pathPref = personService.getPathForEdition(personId, ACCId);
		if (pathPref.isPresent() && pathPref.get().getPath() != null) {
			return pathPref.get().getPath().getName();
		} else {
			return "Path not selected";
		}
	}

	/**
	 * Get the moduleId of a task
	 *
	 * @param  taskId id of the task
	 * @return        Module id
	 */
	public Long getModuleId(Long taskId) {
		Task task = taskRepository.findByIdOrThrow(taskId);
		return task.getSkill().getSubmodule().getModule().getId();
	}

	/**
	 * Order a list of skill IDs based on their row in the skill circuit. The higher the row number, the
	 * higher the skill's position in the list
	 *
	 * @param  skillIds a list of skill IDs
	 * @return          the given list skill IDs ordered
	 */
	private List<Long> getSkillOrder(List<Long> skillIds) {
		//		Does not take into account module order
		List<Skill> skills = skillIds.stream().map(skillRepository::findByIdOrThrow)
				.sorted(Comparator.comparingInt(Skill::getRow)).toList();
		return skills.stream().map(Skill::getId).toList();

	}

	/**
	 * Get PlaylistTaskViewDTOs for each uncompleted task of a skill
	 *
	 * @param  taskCompletions list of TaskCompletions for a ResearchParticipant
	 * @param  skill           the skill to retrieve uncompleted tasks from
	 * @return                 a list of PlaylistTaskViewDTOs for each uncompleted task
	 */
	public List<PlaylistTaskViewDTO> getTaskDTOs(Set<TaskCompletion> taskCompletions, Skill skill) {
		List<PlaylistTaskViewDTO> tasks = new LinkedList<>();

		// TODO: Only temporary solution for compiling. This does not handle tasks correctly.

		//		For each task, get the module it belongs to and whether the student has already completed it
		for (Task t : skill.getTasks().stream().filter(t -> t instanceof Task).map(t -> (Task) t)
				.collect(Collectors.toList())) {
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

	/**
	 * Update the completion status of a Task's playlist counterpart
	 *
	 * @param person    person that is a ResearchParticipant
	 * @param task      the Task that is updated
	 * @param completed boolean representing whether the task has been completed (true) or not (false)
	 */
	@Transactional
	public void setPlTaskCompleted(SCPerson person, Task task, boolean completed) {
		ResearchParticipant participant = researchParticipantRepository.findByPerson(person);
		if (participant != null) {
			PlaylistTask plTask = playlistTaskRepository
					.findByParticipantAndTaskId(participant, task.getId());
			if (plTask != null) {
				plTask.setCompleted(completed);
				log.trace("Succesfully completed a playlist task");
			}
		}
	}

	/**
	 * Get a list of PlaylistTaskViewDTOs for a list of Task IDs
	 *
	 * @param  taskIds a list of Task IDs
	 * @return         list of PlaylistTaskViewDTOs
	 */
	public List<PlaylistTaskViewDTO> getTaskDTOs(List<Long> taskIds) {
		List<PlaylistTaskViewDTO> tasks = new LinkedList<>();
		log.trace("Transforming tasks to playlistTaskDTOs");
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

	/**
	 * Get the uncompleted skills of a given checkpoint as a list of PlaylistSkillViewDTOs
	 *
	 * @param  checkpoint   Checkpoint to retrieve uncompleted skills of
	 * @param  complTaskIds TaskCompletions of ResearchParticipant
	 * @return              a list of PlaylistSkillViewDTOs
	 */
	private List<PlaylistSkillViewDTO> getCheckpointRemainingSkills(Checkpoint checkpoint,
			List<Long> complTaskIds) {
		log.trace("Getting uncompleted skills for checkpoint:" + checkpoint.getName());
		List<PlaylistSkillViewDTO> skills = new LinkedList<>();
		for (Skill s : checkpoint.getSkills()) {
			// TODO: Only temporary solution for compiling. This does not handle tasks correctly.
			//	Filter out skills that are still hidden for the participant
			if (!complTaskIds.containsAll(s.getRequiredTasks().stream().map(AbstractTask::getId).toList())) {
				log.trace("Filtered out a hidden skill");
				continue;
			}
			PlaylistSkillViewDTO skillDTO = View.convert(s, PlaylistSkillViewDTO.class);
			skillDTO.postApply(taskRepository, getTaskDTOs(getSkillRemainingTasks(s, complTaskIds)));
			skills.add(skillDTO);
			log.debug("Skill '" + s.getName() + "' has " + skillDTO.getTasks().size() + "uncompleted tasks");
		}
		log.debug("Checkpoint '" + checkpoint.getName() + "' has " + skills.size() + " uncompleted skills");
		return Collections.unmodifiableList(skills);
	}

	/**
	 * Get the remaining uncompleted Tasks of a given Skill
	 *
	 * @param  skill        Skill to retrieve uncompleted Tasks from
	 * @param  complTaskIds A list of IDs for all the tasks completed by a ResearchParticipant
	 * @return              a list containing the IDs for each uncompleted task of the given Skill
	 */
	private List<Long> getSkillRemainingTasks(Skill skill, List<Long> complTaskIds) {
		// TODO: Only temporary solution for compiling. This does not handle tasks correctly.
		log.trace("Getting uncompleted tasks for skill: " + skill.getName());
		log.trace("Skill '" + skill.getName() + "' has " + skill.getTasks().size() + " tasks in total");
		return skill.getTasks().stream().map(AbstractTask::getId).filter(t -> !complTaskIds.contains(t))
				.toList();

	}

	/**
	 * Retrieve the PlaylistCheckpointDTOs for each checkpoint in the ACC edition that has uncompleted skills
	 * for the given ResearchParticipant
	 *
	 * @param  personId person that is a ResearchParticipant
	 * @return          a list of PlaylistCheckpointDTOs
	 */
	public List<PlaylistCheckpointDTO> getCheckpointDTOs(Long personId) {
		SCPerson person = personRepository.findByIdOrThrow(personId);
		SCEdition edition = editionRepository.findByIdOrThrow(ACCId);

		//		Sort checkpoint according to their deadline
		List<Checkpoint> checkpoints = edition.getCheckpoints().stream()
				.sorted(Comparator.comparing(Checkpoint::getDeadline)).toList();
		log.trace("Found " + checkpoints.size() + "checkpoints");
		//		Get all taskcompletions for a student
		List<Long> complTaskIds = person.getTaskCompletions().stream().map(TaskCompletion::getTask)
				.map(Task::getId).toList();
		List<PlaylistCheckpointDTO> checkpointDTOs = new LinkedList<>();

		//		For each checkpoint, get uncompleted skills
		for (Checkpoint cp : checkpoints) {
			log.trace("Checkpoint '" + cp.getName() + "' has in total " + cp.getSkills().size() + " skills");
			List<PlaylistSkillViewDTO> remainingSkills = getCheckpointRemainingSkills(cp, complTaskIds);
			if (!remainingSkills.isEmpty()) {
				PlaylistCheckpointDTO cpDTO = View.convert(cp, PlaylistCheckpointDTO.class);
				cpDTO.postApply(remainingSkills);

				checkpointDTOs.add(cpDTO);
			}
		}
		return Collections.unmodifiableList(checkpointDTOs);
	}

	/**
	 * Deactivate the currently active playlist of a ResearchParticipant
	 *
	 * @param participant ResearchParticipant
	 */
	@Transactional
	public void deactivateActivePlaylist(ResearchParticipant participant) {
		Playlist playlist = playlistRepository.findByParticipantAndActive(participant, true);
		if (playlist != null) {
			playlist.setActive(false);
			playlistRepository.saveAndFlush(playlist);
		}

	}
}
