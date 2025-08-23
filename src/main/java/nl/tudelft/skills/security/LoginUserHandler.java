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
package nl.tudelft.skills.security;

import org.springframework.stereotype.Service;

import io.sentry.Sentry;
import io.sentry.protocol.User;
import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.LabradorUserHandler;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.service.SCPersonService;

/**
 * Interface for handling user logins on the client implementation.
 *
 * For more information, consult LabraDoor implementation and documentation at
 * https://gitlab.ewi.tudelft.nl/eip/labrador/labradoor/-/wikis/Introduction.
 */

@Service
@AllArgsConstructor
public class LoginUserHandler implements LabradorUserHandler {

	private final SCPersonService sCPersonService;

	/**
	 * Makes changes to the DB when someone logs in.
	 *
	 * @param person who has just logged in
	 */
	@Override
	public void handleUserLogin(Person person) {
		Sentry.configureScope(scope -> {
			User user = new User();
			user.setUsername(person.getUsername());
			scope.setTag("DefaultRole", person.getDefaultRole().toString());
		});
		sCPersonService.getOrCreate(person.getId());
	}

}
