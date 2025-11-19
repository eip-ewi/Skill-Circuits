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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.skills.dto.stats.StudentStatsDTO;
import nl.tudelft.skills.dto.stats.TaskStatsDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;

@Service
@AllArgsConstructor
public class EditionStatisticsService {
	private final TaskRepository taskRepository;
	private final ClickedLinkRepository clickedLinkRepository;
	private final PathPreferenceRepository pathPreferenceRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final PersonRepository personRepository;
	private final PersonControllerApi personApi;

	public Resource getTaskStatisticsCSV(SCEdition edition) throws IOException {
		List<TaskStatsDTO> teacherStats = teacherStatsTaskLevel(edition);
		StringWriter sw = new StringWriter();
		try (CSVWriter writer = new CSVWriter(sw)) {
			writer.writeNext(new String[] { "Task id", "Task name", "Skill name", "Checkpoint name",
					"Submodule name", "Module name", "Student completions",
					"Students with this task on their path", "Student link clicks",
					"Unique student link clicks", "Students clicked the link and completed" });

			for (TaskStatsDTO t : teacherStats) {
				writer.writeNext(new String[] { String.valueOf(t.getId()), t.getTaskName(), t.getSkillName(),
						t.getCheckpointName(), t.getSubModuleName(), t.getModuleName(),
						String.valueOf(t.getNumOfStudentCompletions()),
						String.valueOf(t.getNumOfStudentsHaveTaskOnPath()),
						String.valueOf(t.getNumOfStudentLinkClicks()),
						String.valueOf(t.getNumOfUniqueStudentsClickedLink()),
						String.valueOf(t.getNumOfStudentsClickedLinkAndCompleted()) });
			}
		}

		return new ByteArrayResource(sw.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Collects statistics for each task in a given edition. The statistics contain information about the name
	 * of the tasks, skills, checkpoints, submodules and modules, counts of link clicks, completions and path
	 * inclusions.
	 *
	 * @param  edition The edition the statistics needs to be collected for
	 * @return         a list of {@link TaskStatsDTO} objects, containing the task level statistics for the
	 *                 given edition
	 */
	public List<TaskStatsDTO> teacherStatsTaskLevel(SCEdition edition) {
		Set<Task> tasks = taskRepository.findAllByEdition(edition);

		// Collect all students in the edition
		List<Long> studentsInEditionIds = requireNonNull(
				personApi.getPeopleByEditionAndRoleType(edition.getId(), "STUDENT")
						.map(PersonSummaryDTO::getId)
						.collectList().block());

		// Collect all the TaskInfo into a list from the existing tasks
		List<TaskInfo> taskInfos = tasks.stream().sorted(Comparator.comparing(Task::getId)).flatMap(t -> {
			List<TaskInfo> info = new ArrayList<>();
			if (t instanceof RegularTask) {
				info = List.of(((RegularTask) t).getTaskInfo());
			} else if (t instanceof ChoiceTask) {
				info = ((ChoiceTask) t).getTasks();
			}
			return info.stream();
		}).toList();

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
					.toList();

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

	public Resource getStudentStatisticsCSV(SCEdition edition) throws IOException {
		List<StudentStatsDTO> studentStats = teacherStatsStudentLevel(edition);
		StringWriter sw = new StringWriter();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		try (CSVWriter writer = new CSVWriter(sw)) {
			writer.writeNext(new String[] { "Student id", "Username", "Chosen path", "Completed tasks",
					"Time of last task completion",
					"Last completed task",
					"Furthest checkpoint" });

			for (StudentStatsDTO t : studentStats) {
				writer.writeNext(new String[] { String.valueOf(t.getId()), t.getUserName(), t.getChosenPath(),
						String.valueOf(t.getNumberOfCompletedTasks()),
						t.getLastCompletedTaskTimestamp() == null ? "No activity"
								: t.getLastCompletedTaskTimestamp().format(formatter),
						t.getLastCompletedTask(), t.getFurthestCheckpoint() });
			}
		}

		return new ByteArrayResource(sw.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Collects statistics for each student in a given edition. The statistics contain information about the
	 * username of the student, the chosen path, number of completed tasks by the student, time of last task
	 * completion, name of last completed task, name of the furthest checkpoint in which the student completed
	 * a task in.
	 *
	 * @param  edition The edition the statistics needs to be collected for
	 * @return         a list of {@link StudentStatsDTO} objects, containing the student level statistics for
	 *                 the given edition
	 */
	public List<StudentStatsDTO> teacherStatsStudentLevel(SCEdition edition) {
		List<PersonSummaryDTO> studentsInEdition = requireNonNull(
				personApi.getPeopleByEditionAndRoleType(edition.getId(), "STUDENT").collectList().block());
		List<Long> studentIds = studentsInEdition.stream().map(PersonSummaryDTO::getId)
				.collect(Collectors.toList());
		List<SCPerson> studentsInEditionSCPerson = personRepository.findAllById(studentIds);

		return studentsInEditionSCPerson.stream().map(student -> {
			Optional<PersonSummaryDTO> personSummary = studentsInEdition.stream()
					.filter(x -> x.getId().equals(student.getId())).findAny();

			// Path preference of the student in the given edition
			Optional<PathPreference> pathPreference = student.getPathPreferences().stream()
					.filter(p -> Objects.equals(p.getEdition().getId(), edition.getId())).findFirst();

			Set<TaskCompletion> taskCompletions = taskCompletionRepository.findAllByPersonAndEdition(student,
					edition);
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
					student.getId(),
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
