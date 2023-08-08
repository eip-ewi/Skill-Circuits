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
package nl.tudelft.skills.model;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class CustomClickedLinkSerializer extends StdSerializer<ClickedLink> {
	public CustomClickedLinkSerializer() {
		this(null);
	}

	public CustomClickedLinkSerializer(Class<ClickedLink> t) {
		super(t);
	}

	@Override
	public void serialize(
			ClickedLink clickedLink,
			JsonGenerator jgen,
			SerializerProvider provider)
			throws IOException {
		jgen.writeStartObject();
		jgen.writeNumberField("id", clickedLink.getId());
		jgen.writeNumberField("taskId", clickedLink.getTask().getId());
		jgen.writeStringField("taskName", clickedLink.getTask().getName());
		jgen.writeStringField("skillName", clickedLink.getTask().getSkill().getName());
		jgen.writeNumberField("editionId",
				clickedLink.getTask().getSkill().getSubmodule().getModule().getEdition().getId());
		jgen.writeNumberField("personId", clickedLink.getPerson().getId());
		jgen.writeStringField("timestamp",
				clickedLink.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		jgen.writeEndObject();
	}
}
