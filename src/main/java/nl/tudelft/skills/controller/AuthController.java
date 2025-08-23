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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.AuthorisationView;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.security.AuthorisationService;
import nl.tudelft.skills.service.CourseService;
import nl.tudelft.skills.service.EditionService;
import nl.tudelft.skills.service.SCPersonService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthorisationService authorisationService;
	private final CourseService courseService;
	private final EditionService editionService;
	private final SCPersonService scPersonService;

	@GetMapping
	public Person getAuthenticatedPerson() {
		return authorisationService.getAuthenticatedPerson();
	}

	@GetMapping("authorisation")
	public AuthorisationView getAuthorisation(@AuthenticatedPerson Person person,
			@AuthenticatedSCPerson SCPerson scPerson) {
		return new AuthorisationView(
				scPerson.getViewMode(),
				person.getDefaultRole() == DefaultRole.ADMIN,
				editionService.getManagedEditionIds(scPerson, true),
				courseService.getManagedCourseIds(person));
	}

	@GetMapping("status")
	public ResponseEntity<Void> getAuthStatus() {
		if (!authorisationService.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("view-mode")
	public void setViewMode(@AuthenticatedSCPerson SCPerson scPerson, @RequestParam ViewMode viewMode) {
		scPersonService.setViewMode(scPerson, viewMode);
	}

}
