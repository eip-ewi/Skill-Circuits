/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
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
package nl.tudelft.skills.smoke;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import nl.tudelft.skills.DevSkillCircuitsApplication;

@Nested
@SpringBootTest(classes = DevSkillCircuitsApplication.class)
public class DevSubmitApplicationTest extends ApplicationTestClass {

	@Autowired
	private LocaleResolver localeResolver;

	@Autowired
	private LocaleChangeInterceptor localeChangeInterceptor;

	@Test
	void localeConfigTest() {
		assertThat(localeResolver).isNotNull();
		assertThat(localeChangeInterceptor).isNotNull();
	}

}
