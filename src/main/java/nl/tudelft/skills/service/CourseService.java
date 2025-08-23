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
package nl.tudelft.skills.service;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;

@Service
@AllArgsConstructor
public class CourseService {

	private final CourseControllerApi courseApi;

	public Set<Long> getManagedCourseIds(Person person) {
		if (person.getDefaultRole() == DefaultRole.ADMIN) {
			return requireNonNull(courseApi.getAllCourses().map(CourseSummaryDTO::getId)
					.collect(Collectors.toSet()).block());
		}
		return requireNonNull(courseApi.getAllCoursesByManager(person.getId()).map(CourseSummaryDTO::getId)
				.collect(Collectors.toSet()).block());
	}

}
