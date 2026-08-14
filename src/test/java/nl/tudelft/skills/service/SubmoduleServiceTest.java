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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.dto.patch.SubmodulePositionUpdate;
import nl.tudelft.skills.dto.patch.SubmodulePositionUpdates;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.SubmoduleRepository;

public class SubmoduleServiceTest {

	private SubmoduleService submoduleService;

	private SubmoduleRepository submoduleRepository;
	private DTOConverter dtoConverter;

	@BeforeEach
	public void setUp() {
		submoduleRepository = mock(SubmoduleRepository.class);
		dtoConverter = mock(DTOConverter.class);

		submoduleService = new SubmoduleService(submoduleRepository, dtoConverter);
	}

	@Test
	public void updatePositionsSuccess() {
		Submodule submoduleOne = submodule(1L, 0);
		Submodule submoduleTwo = submodule(2L, 1);

		when(dtoConverter.apply(new SubmoduleId(1L))).thenReturn(submoduleOne);
		when(dtoConverter.apply(new SubmoduleId(2L))).thenReturn(submoduleTwo);

		SubmodulePositionUpdates positions = new SubmodulePositionUpdates(
				List.of(new SubmodulePositionUpdate(new SubmoduleId(1L), 2),
						new SubmodulePositionUpdate(new SubmoduleId(2L), 4)));

		submoduleService.updatePositions(positions);

		assertEquals(2, submoduleOne.getColumn());
		assertEquals(4, submoduleTwo.getColumn());
		verify(submoduleRepository, times(1)).saveAll(anyList());
	}

	private static Submodule submodule(Long id, Integer column) {
		return Submodule.builder()
				.id(id)
				.name("Submodule " + id)
				.column(column)
				.build();
	}
}
