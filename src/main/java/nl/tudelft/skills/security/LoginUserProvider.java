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

import static nl.tudelft.labracore.lib.security.user.DefaultRole.*;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import nl.tudelft.labracore.lib.security.memory.InMemoryUserProvider;

/**
 * Provider for in-memory user information.
 *
 * For more information, consult LabraDoor implementation and documentation at
 * https://gitlab.ewi.tudelft.nl/eip/labrador/labradoor/-/wikis/Introduction.
 */

@Service
@Profile("!production")
public class LoginUserProvider extends InMemoryUserProvider {

	/**
	 * Adds the users created and added inside the constructor to the in-memory database.
	 */
	public LoginUserProvider() {
		super("pass");
		// default password for all users is "pass" (used when no password for user is provided)

		add("Admin 1", 7, ADMIN, "admin1");
		// username: admin1, password: admin1

		add("CSE Teacher 1", 5, TEACHER, "cseteacher1");
		// username: cseteacher1, password: cseteacher1

		add("CSE Student 1", 11, STUDENT, "csestudent1");
		// username: csestudent1, password: csestudent1

	}

}
