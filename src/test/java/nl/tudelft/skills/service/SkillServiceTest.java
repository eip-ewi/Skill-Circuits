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
import nl.tudelft.skills.dto.id.AbstractSkillId;
import nl.tudelft.skills.dto.patch.SkillPositionUpdate;
import nl.tudelft.skills.dto.patch.SkillPositionUpdates;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.ExternalSkillRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.bookmark.HiddenSkillBookmarkListRepository;

public class SkillServiceTest {

	private SkillService skillService;

	private AbstractSkillRepository abstractSkillRepository;
	private ExternalSkillRepository externalSkillRepository;
	private HiddenSkillBookmarkListRepository hiddenSkillBookmarkListRepository;
	private SkillRepository skillRepository;
	private DTOConverter dtoConverter;

	@BeforeEach
	public void setUp() {
		abstractSkillRepository = mock(AbstractSkillRepository.class);
		externalSkillRepository = mock(ExternalSkillRepository.class);
		hiddenSkillBookmarkListRepository = mock(HiddenSkillBookmarkListRepository.class);
		skillRepository = mock(SkillRepository.class);
		dtoConverter = mock(DTOConverter.class);

		skillService = new SkillService(
				abstractSkillRepository,
				externalSkillRepository,
				hiddenSkillBookmarkListRepository,
				skillRepository,
				dtoConverter);
	}

	@Test
	public void updatePositionsSuccess() {
		Skill skillOne = skill(1L, 0);
		Skill skillTwo = skill(2L, 1);

		when(dtoConverter.apply(new AbstractSkillId(1L))).thenReturn(skillOne);
		when(dtoConverter.apply(new AbstractSkillId(2L))).thenReturn(skillTwo);

		SkillPositionUpdates positions = new SkillPositionUpdates(
				List.of(new SkillPositionUpdate(new AbstractSkillId(1L), 3),
						new SkillPositionUpdate(new AbstractSkillId(2L), 5)));

		skillService.updatePositions(positions);

		assertEquals(3, skillOne.getColumn());
		assertEquals(5, skillTwo.getColumn());
		verify(abstractSkillRepository, times(1)).saveAll(anyList());
	}

	private static Skill skill(Long id, Integer column) {
		return Skill.builder()
				.id(id)
				.column(column)
				.name("Skill " + id)
				.submodule(Submodule.builder().id(100L + id).name("Submodule " + id).build())
				.build();
	}
}
