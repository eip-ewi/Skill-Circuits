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
package nl.tudelft.skills.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.repository.ModuleRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Flux;

@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class HomeControllerTest extends ControllerTest {

	private final HomeController homeController;
	private final CourseControllerApi courseApi;

	@Autowired
	public HomeControllerTest(ModuleRepository moduleRepository, CourseControllerApi courseApi) {
		this.courseApi = courseApi;
		this.homeController = new HomeController(moduleRepository, courseApi);
	}

	@Test
	void getLoginPage() {
		assertThat(homeController.getLoginPage()).isEqualTo("login");
	}

	@Test
	@SuppressWarnings("unchecked")
	void getHomePage() {
		CourseSummaryDTO course = new CourseSummaryDTO();

		when(courseApi.getAllCourses()).thenReturn(Flux.just(course));

		homeController.getHomePage(model);

		assertThat((List<CourseSummaryDTO>) model.getAttribute("courses")).containsExactly(course);

		verify(courseApi).getAllCourses();
	}
}
