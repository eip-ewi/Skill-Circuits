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
package nl.tudelft.skills.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;

import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.labracore.SCPerson;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

class ClickedLinkSerializerTest {

	@Test
	void serialize() throws IOException {
		ClickedLinkSerializer serializer = new ClickedLinkSerializer();
		SCEdition scEdition = SCEdition.builder().id((long) 9).build();
		SCModule scModule = SCModule.builder().edition(scEdition).build();
		Submodule submodule = Submodule.builder().module(scModule).build();
		Skill skill = Skill.builder().name("Skill 1").submodule(submodule).build();
		Task task = Task.builder().id((long) 2).skill(skill).name("Task 1").build();
		SCPerson person = SCPerson.builder().id((long) 3).build();
		ClickedLink click = ClickedLink.builder().id((long) 1).task(task).person(person)
				.timestamp(LocalDateTime.of(2023, 8, 9, 10, 30, 50)).build();

		Writer jsonWriter = new StringWriter();
		JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
		SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();
		serializer.serialize(click, jsonGenerator, serializerProvider);
		jsonGenerator.flush();
		assertEquals(
				"{\"id\":1,\"taskId\":2,\"taskName\":\"Task 1\",\"skillName\":\"Skill 1\",\"editionId\":9,\"personId\":3,\"timestamp\":\"2023-08-09 10:30:50\"}",
				jsonWriter.toString());
	}
}
