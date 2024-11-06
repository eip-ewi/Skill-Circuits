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
package nl.tudelft.skills.config;

import static org.modelmapper.convention.MatchingStrategies.STRICT;

import nl.tudelft.skills.dto.old.id.*;
import nl.tudelft.skills.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.tudelft.librador.EnableLibrador;
import nl.tudelft.librador.LibradorConfigAdapter;
import nl.tudelft.librador.dto.id.IdMapperBuilder;

@Configuration
@EnableLibrador
public class LibradorConfiguration extends LibradorConfigAdapter {

	@Override
	protected void configure(IdMapperBuilder builder) {
		builder.register(SCEditionIdDTO.class, SCEdition.class);
		builder.register(SCModuleIdDTO.class, SCModule.class);
		builder.register(SubmoduleIdDTO.class, Submodule.class);
		builder.register(CheckpointIdDTO.class, Checkpoint.class);
		builder.register(SkillIdDTO.class, Skill.class);
		builder.register(TaskIdDTO.class, Task.class);
	}

	@Bean
	@Override
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = super.modelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(STRICT);
		return modelMapper;
	}

}
