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

import java.util.Random;

import nl.tudelft.skills.test.TestDatabaseLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ControllerTest {

	private static final Random random = new Random(42L);

	@Autowired
	protected ModelMapper mapper;

	@Autowired
	protected TestDatabaseLoader db;

	@Autowired
	protected MockMvc mvc;

	protected Model model;
	protected ObjectMapper objectMapper;

	@BeforeEach
	void initFields() {
		model = new ExtendedModelMap();
		objectMapper = new ObjectMapper();
	}

	protected Long randomId() {
		return random.nextLong(1000000000L);
	}

	protected <T> T map(Object source, Class<T> dest) {
		return mapper.map(source, dest);
	}

}
