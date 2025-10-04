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
package nl.tudelft.skills.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.opencsv.CSVWriter;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.patch.EditionPatch;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelEditionView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.CopyService;
import nl.tudelft.skills.service.EditionCircuitService;
import nl.tudelft.skills.service.EditionService;
import nl.tudelft.skills.service.ProgressService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/editions")
public class EditionController {

	private final CopyService copyService;
	private final EditionService editionService;
	private final EditionCircuitService editionCircuitService;
	private final ProgressService progressService;

	@GetMapping
	public EditionsView getEditions(@AuthenticatedSCPerson SCPerson person) {
		return editionService.getSortedEditions(person);
	}

	@GetMapping("open")
	public List<EditionDetailsDTO> getOpenEditions(@AuthenticatedSCPerson SCPerson person) {
		return editionService.getOpenEditions(person);
	}

	@GetMapping("managed")
	public List<ManagedEditionView> getManagedEditions(@AuthenticatedSCPerson SCPerson person) {
		return editionService.getManagedEditions(person, false);
	}

	@GetMapping("{editionId}")
	@PreAuthorize("@authorisationService.canViewEdition(#editionId)")
	public EditionView getEdition(@PathVariable Long editionId) {
		return editionService.getEdition(editionId);
	}

	@GetMapping("{editionId}/circuit")
	@PreAuthorize("@authorisationService.canViewEdition(#editionId)")
	public EditionLevelEditionView getEditionCircuit(@AuthenticatedSCPerson SCPerson person,
			@PathVariable Long editionId) {
		return editionCircuitService.getEditionCircuit(editionId, person);
	}

	@PostMapping("{editionId}/join")
	@PreAuthorize("@authorisationService.canViewEdition(#editionId)")
	public void joinEdition(@AuthenticatedSCPerson SCPerson person, @PathVariable Long editionId) {
		editionService.addPersonToEdition(person, editionId);
	}

	@PatchMapping("{edition}")
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#edition.id)")
	public void patchEdition(@PathEntity SCEdition edition, @RequestBody EditionPatch patch) {
		editionService.patchEdition(edition, patch);
	}

	@PostMapping("{edition}/editors/{editorId}")
	@PreAuthorize("@authorisationService.canManageEditionEditors(#edition.id)")
	public void addEditor(@PathEntity SCEdition edition, @PathVariable Long editorId) {
		editionService.addEditor(edition, editorId);
	}

	@DeleteMapping("{edition}/editors/{editor}")
	@PreAuthorize("@authorisationService.canManageEditionEditors(#edition.id)")
	public void removeEditor(@PathEntity SCEdition edition, @PathEntity SCPerson editor) {
		editionService.removeEditor(edition, editor);
	}

	@PostMapping("{edition}/reset-progress")
	public void resetProgress(@AuthenticatedSCPerson SCPerson person, @PathEntity SCEdition edition) {
		progressService.resetProgress(edition, person);
	}

	@PostMapping("{original}/copy-to/{copy}")
	@PreAuthorize("@authorisationService.canViewEdition(#original.id) and @authorisationService.canEditEditionCircuit(#copy.id)")
	public void copyEditionTo(@PathEntity SCEdition original, @PathEntity SCEdition copy) {
		copyService.copyEdition(original, copy);
	}

	@GetMapping(value = "{editionId}/task_stats", produces = "text/csv")
	@ResponseBody
	@PreAuthorize("@authorisationService.isTeacher(#editionId)")
	public ResponseEntity<String> showEditionTaskStats(@PathVariable long editionId) throws IOException {
		List<TaskStatsDTO> teacherStats = editionService.teacherStatsTaskLevel(editionId);
		StringWriter sw = new StringWriter();
		EditionDetailsDTO editionDetails = editionService.getEditionById(editionId);
		try (CSVWriter writer = new CSVWriter(sw)) {
			// header
			writer.writeNext(new String[] { "Task id", "Task name", "Skill name", "Checkpoint name",
					"Submodule name", "Module name", "Student completions",
					"Students with this task on their path", "Student link clicks",
					"Unique student link clicks", "Students clicked the link and completed" });

			// rows
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

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"task_stats_" + editionDetails.getCourse().getName() + "_"
								+ editionDetails.getName() + ".csv\"")
				.contentType(MediaType.valueOf("text/csv"))
				.body(sw.toString());
	}

	@GetMapping(value = "{editionId}/student_stats", produces = "text/csv")
	@ResponseBody
	@PreAuthorize("@authorisationService.isTeacher(#editionId)")
	public ResponseEntity<String> showEditionStudentStats(@PathVariable long editionId) throws IOException {
		List<StudentStatsDTO> studentStats = editionService.teacherStatsStudentLevel(editionId);
		StringWriter sw = new StringWriter();
		EditionDetailsDTO editionDetails = editionService.getEditionById(editionId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		try (CSVWriter writer = new CSVWriter(sw)) {
			// header
			writer.writeNext(new String[] { "Student id", "Username", "Chosen path", "Completed tasks",
					"Time of last task completion",
					"Last completed task",
					"Furthest checkpoint" });

			// rows
			for (StudentStatsDTO t : studentStats) {
				writer.writeNext(new String[] { String.valueOf(t.getId()), t.getUserName(), t.getChosenPath(),
						String.valueOf(t.getNumberOfCompletedTasks()),
						t.getLastCompletedTaskTimestamp() == null ? "No activity"
								: t.getLastCompletedTaskTimestamp().format(formatter),
						t.getLastCompletedTask(), t.getFurthestCheckpoint() });
			}
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"student_stats_" + editionDetails.getCourse().getName() + "_"
								+ editionDetails.getName() + ".csv\"")
				.contentType(MediaType.valueOf("text/csv"))
				.body(sw.toString());
	}

}
