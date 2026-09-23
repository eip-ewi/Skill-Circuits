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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.circuit.track.TrackLevelTrackView;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.TrackService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tracks")
public class TrackController {

	private final TrackService trackService;

	@GetMapping("{editionId}/circuit")
	@PreAuthorize("@authorisationService.canViewTrack(#editionId)")
	public TrackLevelTrackView getTrackCircuit(@AuthenticatedSCPerson SCPerson person,
			@PathVariable Long editionId) {
		return trackService.getTrackCircuit(editionId, person);
	}

}
