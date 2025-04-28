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

import nl.tudelft.labracore.lib.security.LabradorSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Order(22)
@Configuration
public class SkillCircuitsSecurityConfig extends LabradorSecurityConfig {

	/**
	 * Configures which endpoints need authentication and which don't.
	 *
	 * Currently configured to only not request authentication on home page, but requests it everywhere else.
	 *
	 * @param  http      the HttpSecurity object that is being configured
	 * @throws Exception exception that might be thrown
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/skill-circuits-frontend.*", "/webjars/**", "/font/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/api/auth").permitAll()
                .anyRequest().authenticated());

	}
}
