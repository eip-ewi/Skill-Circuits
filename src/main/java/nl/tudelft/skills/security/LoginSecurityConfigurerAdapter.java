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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import nl.tudelft.labracore.lib.security.LabradorSecurityConfigurerAdapter;

/**
 * Security configuration adapter required to make use of the LabraDoor login.
 *
 * For more information, consult LabraDoor implementation and documentation at
 * https://gitlab.ewi.tudelft.nl/eip/labrador/labradoor/-/wikis/Introduction.
 */

@Order(22)
@Configuration
public class LoginSecurityConfigurerAdapter extends LabradorSecurityConfigurerAdapter {

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
		super.configure(http);

		http
				.authorizeRequests()
				// authenticate for these urls
				.antMatchers("/admin/**")
				.authenticated()
				.anyRequest().permitAll();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}
