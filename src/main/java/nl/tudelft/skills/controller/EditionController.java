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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.patch.EditionPatch;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.dto.view.circuit.edition.EditionLevelEditionView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/editions")
public class EditionController {

	private final CopyService copyService;
	private final EditionService editionService;
	private final EditionStatisticsService editionStatisticsService;
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

	@PreAuthorize("@authorisationService.canExportEditionStatistics(#edition.id)")
	@GetMapping(value = "{edition}/statistics/tasks", produces = "text/csv")
	public ResponseEntity<Resource> showEditionTaskStats(@PathEntity SCEdition edition) throws IOException {
		EditionDetailsDTO editionDetails = editionService.getEditionById(edition.getId());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\""
								+ LocalDateTime.now()
										.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
								+ " " + editionDetails.getCourse().getName() + " - "
								+ editionDetails.getName() + " - Task statistics"
								+ ".csv\"")
				.contentType(MediaType.valueOf("text/csv"))
				.body(editionStatisticsService.getTaskStatisticsCSV(edition));
	}

	@PreAuthorize("@authorisationService.canExportEditionStatistics(#edition.id)")
	@GetMapping(value = "{edition}/statistics/students", produces = "text/csv")
	public ResponseEntity<Resource> showEditionStudentStats(@PathEntity SCEdition edition)
			throws IOException {
		EditionDetailsDTO editionDetails = editionService.getEditionById(edition.getId());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\""
								+ LocalDateTime.now()
										.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
								+ " " + editionDetails.getCourse().getName() + " - "
								+ editionDetails.getName() + " - Student statistics"
								+ ".csv\"")
				.contentType(MediaType.valueOf("text/csv"))
				.body(editionStatisticsService.getStudentStatisticsCSV(edition));
	}

}
