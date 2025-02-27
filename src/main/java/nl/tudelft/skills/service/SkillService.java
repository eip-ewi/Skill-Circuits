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
package nl.tudelft.skills.service;

import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.dto.create.ChoiceTaskCreateDTO;
import nl.tudelft.skills.dto.create.RegularTaskCreateDTO;
import nl.tudelft.skills.dto.create.SkillCreateDTO;
import nl.tudelft.skills.dto.create.TaskCreateDTO;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.dto.patch.ChoiceTaskPatchDTO;
import nl.tudelft.skills.dto.patch.RegularTaskPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.TaskPatchDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import nl.tudelft.skills.security.AuthorisationService;

@Service
@AllArgsConstructor
public class SkillService {

	private final AbstractSkillRepository abstractSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final TaskCompletionService taskCompletionService;
	private final CourseControllerApi courseApi;
	private final AuthorisationService authorisationService;
	private final ClickedLinkService clickedLinkService;
	private final PersonRepository personRepository;
	private final RegularTaskRepository regularTaskRepository;
	private final TaskRepository taskRepository;
	private final ChoiceTaskRepository choiceTaskRepository;
	private final PathRepository pathRepository;
	private final TaskInfoRepository taskInfoRepository;
	private final SkillRepository skillRepository;

	/**
	 * Deletes a skill.
	 *
	 * @param  id The id of the skill
	 * @return    The deleted skill
	 */
	@Transactional
	public AbstractSkill deleteSkill(Long id) {
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(id);
		skill.getChildren().forEach(c -> c.getParents().remove(skill));
		if (skill instanceof Skill s) {
			s.getTasks().forEach(t -> {
				if (t instanceof RegularTask) {
					taskCompletionRepository.deleteAll(((RegularTask) t).getCompletedBy());
				}
			});

			clickedLinkService
					.deleteClickedLinksForTasks(s.getTasks().stream().filter(t -> t instanceof RegularTask)
							.map(t -> (RegularTask) t).collect(Collectors.toList()));

			s.getFutureEditionSkills().forEach(innerSkill -> innerSkill.setPreviousEditionSkill(null));
			if (s.getPreviousEditionSkill() != null) {
				s.getPreviousEditionSkill().getFutureEditionSkills().remove(s);
			}

			// Remove references from user if they were customized skils
			s.getPersonModifiedSkill().forEach(p -> {
				// Remove skill from list of custom modified skills
				SCPerson person = personRepository.findByIdOrThrow(p.getId());
				person.getSkillsModified().remove(s);

				// Remove the tasks from the custom skill
				person.setTasksAdded(
						person.getTasksAdded().stream().filter(t -> !t.getSkill().getId().equals(s.getId()))
								.collect(Collectors.toSet()));
				personRepository.save(person);
			});

			s.getRequiredTasks().forEach(t -> {
				t.getRequiredFor().remove(s);
				taskRepository.save(t);
			});
		}
		abstractSkillRepository.delete(skill);
		return skill;
	}

	/**
	 * Gets the correct skill for an external skill. This is the skill in the most recent edition which the
	 * person has last worked on. If none such edition exists, the most recent edition is chosen. "Most
	 * recent" refers to the start date of the edition, and not the date of activity by the person. If there
	 * is no skill in an edition visible to the user, this returns null.
	 *
	 * @param  personId      The id of the person.
	 * @param  externalSkill The external skill.
	 * @return               The correct skill which the external skill refers to. This is the skill in the
	 *                       most recent edition which the person has last worked on. If none such edition
	 *                       exists, the most recent edition is chosen.
	 */
	public Skill recentActiveEditionForSkillOrLatest(Long personId, ExternalSkill externalSkill) {
		Skill skill = externalSkill.getSkill();
		Long editionId = skill.getSubmodule().getModule().getEdition().getId();
		CourseDetailsDTO course = courseApi.getCourseByEdition(editionId).block();

		Map<Long, EditionSummaryDTO> editionsById = course.getEditions().stream()
				.collect(toMap(EditionSummaryDTO::getId, Function.identity()));

		// Get ids of the editions the person as completed at least one task in
		Set<Long> completedTasksInEditions = taskCompletionRepository.getByPersonId(personId).stream()
				.map(taskCompletion -> taskCompletion.getTask().getSkill().getSubmodule()
						.getModule().getEdition().getId())
				.collect(Collectors.toSet());

		// Do DFS
		List<Skill> traversal = traverseSkillTree(skill);

		// Filter by skills in editions visible to the user
		traversal = traversal.stream()
				.filter(innerSkill -> {
					Long innerEditionId = innerSkill.getSubmodule().getModule().getEdition().getId();
					return authorisationService.canViewEdition(innerEditionId);
				})
				.sorted(Comparator.comparing((Skill innerSkill) -> editionsById.get(innerSkill.getSubmodule()
						.getModule().getEdition().getId()).getStartDate()).reversed())
				.collect(Collectors.toList());

		// If it exists, return the skill from the last edition the person has completed a task in
		Optional<Skill> completedTasksInEdition = traversal.stream()
				.filter(innerSkill -> completedTasksInEditions.contains(innerSkill.getSubmodule()
						.getModule().getEdition().getId()))
				.findFirst();

		// If it does not exist, return the skill in the latest edition
		// If there is no skill visible to the user, returns null
		return completedTasksInEdition.orElse(traversal.size() > 0 ? traversal.get(0) : null);
	}

	/**
	 * Return the DFS traversal of the "skill tree" for the given tree. The tree contains all skill versions
	 * in different editions, linked by the previous/future skill(s) attributes
	 *
	 * @param  skill The skill for which to traverse the tree of skill versions.
	 * @return       The DFS traversal of the skill tree for the given skill.
	 */
	public List<Skill> traverseSkillTree(Skill skill) {
		// Find root of the "skill tree"
		Skill current = skill;
		while (current.getPreviousEditionSkill() != null) {
			current = current.getPreviousEditionSkill();
		}

		// Do DFS
		Stack<Skill> traversal = new Stack<>();
		traversal.push(current);

		List<Skill> skills = new ArrayList<>();

		while (!traversal.isEmpty()) {
			current = traversal.pop();
			skills.add(current);

			// Continue traversal
			Set<Skill> nextSkills = current.getFutureEditionSkills();
			for (Skill nextSkill : nextSkills) {
				traversal.push(nextSkill);
			}
		}

		return skills;
	}

	/**
	 * Collects the customized skills that have at least one task in them
	 *
	 * @param  scperson  The person having the customized skills
	 * @param  editionId The edition
	 * @return           The set of customized skills
	 */
	public Set<Skill> getOwnSkillsWithTask(SCPerson scperson, long editionId) {
		return scperson.getTasksAdded().stream().map(Task::getSkill)
				.filter(s -> Objects.equals(s.getSubmodule().getModule().getEdition().getId(),
						editionId))
				.collect(Collectors.toSet());
	}

	/**
	 * Creates a skill from a given SkillCreateDTO.
	 *
	 * @param  create The SkillCreateDTO.
	 * @return        The created skill.
	 */
	@Transactional
	public Skill createSkill(SkillCreateDTO create) {
		Skill skill = skillRepository.saveAndFlush(create.apply());

		// Save new tasks
		List<Task> allTasks = saveNewTasks(skill, create.getNewItems());

		// Reverse ordering of tasks
		allTasks.sort(Comparator.comparingInt(Task::getIdx).reversed());

		// Set tasks of skill
		skill.setTasks(allTasks);

		// Save skill and return
		return abstractSkillRepository.save(skill);
	}

	/**
	 * Saves a list of new tasks and adds them to a given skill.
	 *
	 * @param  skill       The skill to which the new tasks belong.
	 * @param  newTaskDTOs The TaskCreateDTOs for the new tasks.
	 * @return             A list of the tasks that were created.
	 */
	@Transactional
	public List<Task> saveNewTasks(Skill skill, List<? extends TaskCreateDTO<?>> newTaskDTOs) {
		// Create sub-lists for task types
		List<RegularTaskCreateDTO> newRegularTasks = newTaskDTOs.stream()
				.filter(create -> create instanceof RegularTaskCreateDTO)
				.map(create -> (RegularTaskCreateDTO) create)
				.toList();
		List<ChoiceTaskCreateDTO> newChoiceTasks = newTaskDTOs.stream()
				.filter(create -> create instanceof ChoiceTaskCreateDTO)
				.map(create -> (ChoiceTaskCreateDTO) create)
				.toList();

		// New tasks will be included in all paths by default
		SCEdition edition = skill.getSubmodule().getModule().getEdition();
		Set<Path> paths = new HashSet<>(pathRepository.findAllByEditionId(edition.getId()));

		// Set all task attributes and save to database
		// TODO handling of ChoiceTasks
		List<Task> newTasks = new ArrayList<>();
		newTasks.addAll(newRegularTasks.stream()
				.map(dto -> (Task) saveTaskFromRegularTaskDto(dto, skill, paths)).toList());
		skill.getTasks().addAll(newTasks);
		skillRepository.save(skill);

		return newTasks;
	}

	/**
	 * Patches a skill from a given SkillPatchDTO.
	 *
	 * @param  patch The SkillPatchDTO.
	 * @return       The patched skill.
	 */
	@Transactional
	public Skill patchSkill(SkillPatchDTO patch) {
		// Get skill and apply patch
		Skill skill = skillRepository.findByIdOrThrow(patch.getId());
		skill = patch.apply(skill);

		// Update required tasks for skill
		skill = patchRequiredTasks(skill, skill.getRequiredTasks(),
				taskRepository.findAllByIdIn(patch.getRequiredTaskIds()));

		// Patch, save and remove tasks
		List<Task> allTasks = new ArrayList<>(patchTasks(skill, patch.getItems()));
		allTasks.addAll(saveNewTasks(skill, patch.getNewItems()));
		removeTasks(skill, patch.getRemovedItems());

		// Reverse ordering of tasks by their index and set tasks
		allTasks.sort(Comparator.comparingInt(Task::getIdx).reversed());
		skill.setTasks(allTasks);

		// Save and return skill
		return skillRepository.save(skill);
	}

	/**
	 * Patches the required tasks of a skill.
	 *
	 * @param  skill            The skill for which the required tasks should be patched.
	 * @param  oldRequiredTasks The old required tasks.
	 * @param  newRequiredTasks The new required tasks.
	 * @return                  The updated skill with the patched required tasks.
	 */
	@Transactional
	public Skill patchRequiredTasks(Skill skill, Set<Task> oldRequiredTasks, Set<Task> newRequiredTasks) {
		// If the skill is not hidden, it should not have any required tasks
		if (!skill.isHidden()) {
			newRequiredTasks = new HashSet<>();
		}

		// Remove requirement for newly non-required tasks
		for (Task task : oldRequiredTasks) {
			if (!newRequiredTasks.contains(task)) {
				task.getRequiredFor().remove(skill);
				taskRepository.save(task);
			}
		}

		// Add requirement for newly required tasks
		Set<Task> patchedRequiredTasks = new HashSet<>();
		for (Task task : newRequiredTasks) {
			if (!oldRequiredTasks.contains(task)) {
				task.getRequiredFor().add(skill);
				task = taskRepository.save(task);
			}
			patchedRequiredTasks.add(task);
		}

		// Save to repository and return skill
		skill.setRequiredTasks(patchedRequiredTasks);
		return skillRepository.save(skill);
	}

	/**
	 * Remove tasks by their ids from a given skill.
	 *
	 * @param skill        The skill that contains the tasks.
	 * @param removedTasks The ids of the tasks that should be removed.
	 */
	@Transactional
	public void removeTasks(Skill skill, Set<Long> removedTasks) {
		// TODO handling deletion of ChoiceTasks
		Set<RegularTask> regularTasks = regularTaskRepository.findAllByIdIn(removedTasks);

		// Remove tasks from custom skills
		skill.getPersonModifiedSkill().forEach(p -> {
			p.setTasksAdded(p.getTasksAdded().stream()
					.filter(t -> !removedTasks.contains(t.getId())).collect(Collectors.toSet()));
			personRepository.save(p);
		});

		// Remove task completions, clicked links and task requirements
		regularTasks.forEach(t -> {
			t.getRequiredFor().forEach(s -> {
				s.getRequiredTasks().remove(t);
				skillRepository.save(s);
			});
			taskCompletionService.deleteTaskCompletionsOfTask(t);
		});
		clickedLinkService.deleteClickedLinksForTasks(regularTasks);

		// Remove tasks
		regularTaskRepository
				.deleteAllByIdIn(regularTasks.stream().map(RegularTask::getId).collect(Collectors.toList()));
		skill.getTasks().removeAll(regularTasks);
		skillRepository.save(skill);
	}

	/**
	 * Patches a list of tasks belonging to a given skill.
	 *
	 * @param  skill       The skill to which the tasks belong.
	 * @param  taskPatches The TaskPatchDTOs for the tasks that should be patched.
	 * @return             A list of the tasks that were patched.
	 */
	@Transactional
	public List<Task> patchTasks(Skill skill, List<? extends TaskPatchDTO<?>> taskPatches) {
		Map<Long, Task> idToTask = skill.getTasks().stream()
				.collect(Collectors.toMap(Task::getId, Function.identity()));

		// Create sub-lists for task types
		List<RegularTaskPatchDTO> newRegularTasks = taskPatches.stream()
				.filter(patch -> patch instanceof RegularTaskPatchDTO)
				.map(patch -> (RegularTaskPatchDTO) patch)
				.toList();
		List<ChoiceTaskPatchDTO> newChoiceTasks = taskPatches.stream()
				.filter(patch -> patch instanceof ChoiceTaskPatchDTO)
				.map(patch -> (ChoiceTaskPatchDTO) patch)
				.toList();

		// Patch the tasks
		List<Task> allTasks = new ArrayList<>();
		allTasks.addAll(newRegularTasks.stream()
				.map(patch -> taskRepository.save(patch.apply((RegularTask) idToTask.get(patch.getId()))))
				.toList());
		// TODO patching ChoiceTasks

		return allTasks;
	}

	/**
	 * Saves a ChoiceTask and the RegularTasks that are in the ChoiceTask, specified by a ChoiceTaskCreateDTO.
	 * All new tasks are added to a given skill and paths.
	 *
	 * @param  choiceTaskDTO The ChoiceTaskCreateDTO for creating the ChoiceTask and the RegularTasks in it.
	 * @param  skill         The skill to which the tasks should be added.
	 * @param  paths         The paths to which the tasks should be added.
	 * @return               A list of the created ChoiceTask and the RegularTasks in it.
	 */
	@Transactional
	public List<Task> saveTasksFromChoiceTaskDto(ChoiceTaskCreateDTO choiceTaskDTO, Skill skill,
			Set<Path> paths) {
		// TODO tests for this method will be added when ChoiceTasks are fully implemented

		// Apply task DTO and set attributes
		choiceTaskDTO.setSkill(SkillIdDTO.builder().id(skill.getId()).build());
		ChoiceTask choiceTask = choiceTaskDTO.apply();
		choiceTask.setSkill(skill);
		choiceTask.setPaths(paths);

		// Save all sub-tasks contained in the choice task
		List<RegularTask> subTasks = choiceTaskDTO.getTasks().stream()
				.map(taskDto -> saveTaskFromRegularTaskDto(taskDto, skill, paths))
				.toList();

		// Set the tasks of the choice task
		choiceTask.getTasks().addAll(subTasks.stream().map(RegularTask::getTaskInfo).toList());
		choiceTask = taskRepository.save(choiceTask);

		// Return all tasks (choice task and sub-tasks)
		List<Task> tasks = new ArrayList<>();
		tasks.add(choiceTask);
		tasks.addAll(subTasks);
		return tasks;
	}

	/**
	 * Saves the RegularTask specified by a RegularTaskCreateDTO, and adds it to a given skill and paths.
	 *
	 * @param  regularTaskDTO The RegularTaskCreateDTO for creating the RegularTask.
	 * @param  skill          The skill to which the task should be added.
	 * @param  paths          The paths to which the task should be added.
	 * @return                The created RegularTask.
	 */
	@Transactional
	public RegularTask saveTaskFromRegularTaskDto(RegularTaskCreateDTO regularTaskDTO, Skill skill,
			Set<Path> paths) {
		// Apply task DTO and set attributes
		regularTaskDTO.setSkill(SkillIdDTO.builder().id(skill.getId()).build());
		RegularTask task = regularTaskDTO.apply();
		task.setPaths(paths);
		task.setSkill(skill);

		// Apply taskInfo DTO and set attributes
		TaskInfo taskInfo = regularTaskDTO.getTaskInfo().apply();
		taskInfo.setTask(task);
		task.setTaskInfo(taskInfo);

		// Save and return task
		return taskRepository.save(task);
	}

}
