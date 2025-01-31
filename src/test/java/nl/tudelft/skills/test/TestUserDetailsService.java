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
package nl.tudelft.skills.test;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;

@Primary
@Service
public class TestUserDetailsService implements UserDetailsService {

	public static Long id = 329476L;

	public static Person assemblePerson(String username) {
		Person person = Person.builder()
				.id(id)
				.number(username.hashCode())
				.username(username)
				.displayName(username)
				.externalId(username + "@tudelft.nl")
				.email(username + "@tudelft.nl")
				.defaultRole(DefaultRole.STUDENT)
				.build();
		return person;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Person user = assemblePerson(username);
		if (username.equals("admin")) {
			user.setDefaultRole(DefaultRole.ADMIN);
		}
		if (username.contains("teacher")) {
			user.setDefaultRole(DefaultRole.TEACHER);
		}
		return new LabradorUserDetails(user);
	}
}
