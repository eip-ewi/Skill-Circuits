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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.PersonDetailsDTO;
import nl.tudelft.labracore.lib.security.memory.InMemoryUserProvider;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;
import reactor.core.publisher.Mono;

/**
 * Provider for in-memory user information.
 *
 * For more information, consult LabraDoor implementation and documentation at
 * https://gitlab.ewi.tudelft.nl/eip/labrador/labradoor/-/wikis/Introduction.
 */

@Service
@Profile("!production")
@AllArgsConstructor
public class LoginUserProvider extends InMemoryUserProvider {

	private final PersonControllerApi personApi;

	@Override
	public Person findByUsername(String username) {
		PersonDetailsDTO person = personApi.getPersonByUsername(username).onErrorResume(e -> Mono.empty())
				.block();
		if (person == null)
			return null;
		return Person.builder()
				.id(person.getId())
				.externalId(person.getExternalId())
				.username(person.getUsername())
				.email(person.getEmail())
				.displayName(person.getDisplayName())
				.defaultRole(DefaultRole.valueOf(person.getDefaultRole().name()))
				.number(person.getNumber())
				.build();
	}

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public String getPasswordFor(Person person) {
		return passwordEncoder.encode(person.getUsername());
	}

}
