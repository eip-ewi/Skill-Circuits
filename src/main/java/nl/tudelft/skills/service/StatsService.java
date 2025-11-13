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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.skills.dto.stats.StudentStatsDTO;
import nl.tudelft.skills.dto.stats.TaskStatsDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;

@Service
@AllArgsConstructor
public class StatsService {
	private final TaskRepository taskRepository;
	private final ClickedLinkRepository clickedLinkRepository;
	private final PathPreferenceRepository pathPreferenceRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final PersonRepository personRepository;
	private final PersonControllerApi personApi;

	/**
	 * Collects statistics for each task in a given edition. The statistics contain information about the name
	 * of the tasks, skills, checkpoints, submodules and modules, counts of link clicks, completions and path
	 * inclusions.
	 *
	 * @param  id The id of the edition, the statistics needs to be collected for
	 * @return    a list of {@link TaskStatsDTO} objects, containing the task level statistics for the given
	 *            edition
	 */
	public List<TaskStatsDTO> teacherStatsTaskLevel(Long id) {
		List<Task> tasks = taskRepository.findAllBySkillSubmoduleModuleEditionId(id);

		// Collect all students in the edition
		List<Long> studentsInEditionIds = personApi.getPeopleByEditionAndRoleType(id, "STUDENT")
				.map(PersonSummaryDTO::getId)
				.collectList().block();

		// Collect all the TaskInfo into a list from the existing tasks
		List<TaskInfo> taskInfos = tasks.stream().flatMap(t -> {
			List<TaskInfo> info = new ArrayList<>();
			if (t instanceof RegularTask) {
				info = List.of(((RegularTask) t).getTaskInfo());
			} else if (t instanceof ChoiceTask) {
				info = ((ChoiceTask) t).getTasks();
			}
			return info.stream();
		}).collect(Collectors.toList());

		return taskInfos.stream().map(t -> {
			// Get the ids of the students who completed the current task
			Set<Long> studentsCompletedTask = t.getCompletedBy().stream()
					.map(c -> c.getPerson().getId()).filter(studentsInEditionIds::contains)
					.collect(Collectors.toSet());

			// The TaskInfo should belong to either a RegularTask or a ChoiceTask object
			Task taskCategory = t.getChoiceTask() == null ? t.getTask() : t.getChoiceTask();

			// Count the number of the students who have the current task on their path
			long numOfStudentsHaveTaskOnPath = taskCategory == null ? 0
					: taskCategory.getPaths().stream()
							.flatMap(p -> pathPreferenceRepository
									.findAllByPathId(p.getId()).stream().map(x -> x.getPerson().getId()))
							.distinct().filter(studentsInEditionIds::contains).count();

			// Get the ids of the students from all link clicks
			List<Long> studentsClickedLink = clickedLinkRepository.getByTask(t).stream()
					.map(c -> c.getPerson().getId()).filter(studentsInEditionIds::contains)
					.collect(Collectors.toList());

			// Count students who clicked on the link and completed the task
			long studentsClickedAndCompleted = studentsCompletedTask.stream()
					.filter(studentsClickedLink::contains).count();

			return new TaskStatsDTO(t.getId(),
					t.getName(),
					taskCategory == null ? "Unknown" : taskCategory.getSkill().getName(),
					taskCategory == null ? "Unknown"
							: taskCategory.getSkill().getCheckpoint() == null ? "No checkpoint"
									: taskCategory.getSkill().getCheckpoint().getName(),
					taskCategory == null ? "Unknown" : taskCategory.getSkill().getSubmodule().getName(),
					taskCategory == null ? "Unknown"
							: taskCategory.getSkill().getSubmodule().getModule().getName(),
					studentsCompletedTask.size(),
					numOfStudentsHaveTaskOnPath,
					studentsClickedLink.size(),
					studentsClickedLink.stream().distinct().count(),
					studentsClickedAndCompleted);
		}).collect(Collectors.toList());
	}

	/**
	 * Collects statistics for each student in a given edition. The statistics contain information about the
	 * username of the student, the chosen path, number of completed tasks by the student, time of last task
	 * completion, name of last completed task, name of the furthest checkpoint in which the student completed
	 * a task in.
	 *
	 * @param  id The id of the edition, the statistics needs to be collected for
	 * @return    a list of {@link StudentStatsDTO} objects, containing the student level statistics for the
	 *            given edition
	 */
	public List<StudentStatsDTO> teacherStatsStudentLevel(Long id) {
		List<PersonSummaryDTO> studentsInEdition = personApi.getPeopleByEditionAndRoleType(id, "STUDENT")
				.collectList().block();
		List<Long> studentIds = studentsInEdition.stream().map(PersonSummaryDTO::getId)
				.collect(Collectors.toList());
		List<SCPerson> studentsInEditionSCPerson = personRepository.findAllById(studentIds);

		return studentsInEditionSCPerson.stream().map(s -> {
			Optional<PersonSummaryDTO> personSummary = studentsInEdition.stream()
					.filter(x -> x.getId().equals(s.getId())).findAny();

			// Path preference of the student in the given edition
			Optional<PathPreference> pathPreference = s.getPathPreferences().stream()
					.filter(p -> Objects.equals(p.getEdition().getId(), id)).findFirst();

			Set<TaskCompletion> taskCompletions = taskCompletionRepository.findAllByPersonAndEditionId(s, id);
			Optional<TaskCompletion> lastCompleted = taskCompletions.stream()
					.max(Comparator.comparing(TaskCompletion::getTimestamp));

			// Find checkpoints of the completed tasks
			Set<Checkpoint> checkpointsWithActivity = taskCompletions.stream()
					.map(t -> t.getTask().getChoiceTask() == null ? t.getTask().getTask()
							: t.getTask().getChoiceTask())
					.filter(Objects::nonNull).map(t -> t.getSkill().getCheckpoint()).filter(Objects::nonNull)
					.collect(Collectors.toSet());

			Optional<Checkpoint> furthestCheckpoint = checkpointsWithActivity.stream()
					.max(Comparator.comparing(Checkpoint::getDeadline));

			return new StudentStatsDTO(
					s.getId(),
					personSummary.map(PersonSummaryDTO::getUsername).orElse("Unknown"),
					pathPreference.isEmpty() ? "Path not chosen" : pathPreference.get().getPath().getName(),
					taskCompletions.size(),
					lastCompleted.map(TaskCompletion::getTimestamp).orElse(null),
					lastCompleted.isEmpty() ? "No activity" : lastCompleted.get().getTask().getName(),
					furthestCheckpoint.isEmpty() ? "No checkpoint started"
							: furthestCheckpoint.get().getName());
		}).collect(Collectors.toList());
	}
}
