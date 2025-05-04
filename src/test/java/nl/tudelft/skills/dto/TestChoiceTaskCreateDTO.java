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
package nl.tudelft.skills.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.librador.exception.DTOValidationException;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.ChoiceTaskCreateDTO;
import nl.tudelft.skills.dto.create.RegularTaskCreateDTO;
import nl.tudelft.skills.dto.create.TaskInfoCreateDTO;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.dto.patch.RegularTaskPatchDTO;
import nl.tudelft.skills.dto.patch.TaskInfoPatchDTO;
import nl.tudelft.skills.model.TaskType;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TestChoiceTaskCreateDTO {
	public TestChoiceTaskCreateDTO() {
	}

	@Test
	public void testNoSubTasks() {
		ChoiceTaskCreateDTO createDTO = getChoiceTaskCreateDTO();
		Exception exception = assertThrows(DTOValidationException.class, createDTO::apply);
		assertThat(exception.getMessage()).contains("ChoiceTask has to contain at least two subtasks");
	}

	@Test
	public void testOneNewSubTask() {
		ChoiceTaskCreateDTO createDTO = getChoiceTaskCreateDTO();
		createDTO.setNewSubTasks(List.of(getTaskCreateDTO()));
		Exception exception = assertThrows(DTOValidationException.class, createDTO::apply);
		assertThat(exception.getMessage()).contains("ChoiceTask has to contain at least two subtasks");
	}

	@Test
	public void testOneUpdatedSubTask() {
		ChoiceTaskCreateDTO createDTO = getChoiceTaskCreateDTO();
		createDTO.setUpdatedSubTasks(List.of(getTaskPatchDTO()));
		Exception exception = assertThrows(DTOValidationException.class, createDTO::apply);
		assertThat(exception.getMessage()).contains("ChoiceTask has to contain at least two subtasks");
	}

	@Test
	public void testNumberMinTasksTooSmall() {
		ChoiceTaskCreateDTO createDTO = getChoiceTaskCreateDTO();
		createDTO.setNewSubTasks(List.of(getTaskCreateDTO(), getTaskCreateDTO()));
		createDTO.setMinTasks(0);
		Exception exception = assertThrows(DTOValidationException.class, createDTO::apply);
		assertThat(exception.getMessage())
				.contains("minTasks should be larger than zero and smaller than the number of subtasks");
	}

	@Test
	public void testNumberMinTasksTooBig() {
		ChoiceTaskCreateDTO createDTO = getChoiceTaskCreateDTO();
		createDTO.setNewSubTasks(List.of(getTaskCreateDTO(), getTaskCreateDTO()));
		createDTO.setMinTasks(2);
		Exception exception = assertThrows(DTOValidationException.class, createDTO::apply);
		assertThat(exception.getMessage())
				.contains("minTasks should be larger than zero and smaller than the number of subtasks");
	}

	@Test
	public void testValidChoiceTaskCreateDTO() {
		ChoiceTaskCreateDTO createDTO = getChoiceTaskCreateDTO();
		createDTO.setNewSubTasks(List.of(getTaskCreateDTO(), getTaskCreateDTO()));
		createDTO.setMinTasks(1);
		assertDoesNotThrow(createDTO::apply);
	}

	/**
	 * Helper method to create a ChoiceTaskCreateDTO.
	 *
	 * @return A ChoiceTaskCreateDTO.
	 */
	public ChoiceTaskCreateDTO getChoiceTaskCreateDTO() {
		return ChoiceTaskCreateDTO
				.builder()
				.minTasks(1)
				.index(1)
				.skill(SkillIdDTO.builder().id(1L).build())
				.build();
	}

	/**
	 * Helper method to create a RegularTaskCreateDTO.
	 *
	 * @return A RegularTaskCreateDTO.
	 */
	public RegularTaskCreateDTO getTaskCreateDTO() {
		return RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("Task").type(TaskType.READING).time(0).build())
				.index(1)
				.skill(SkillIdDTO.builder().id(1L).build())
				.build();
	}

	/**
	 * Helper method to create a RegularTaskPatchDTO.
	 *
	 * @return A RegularTaskPatchDTO.
	 */
	public RegularTaskPatchDTO getTaskPatchDTO() {
		return RegularTaskPatchDTO.builder()
				.taskInfo(TaskInfoPatchDTO.builder().name("Task").type(TaskType.READING).time(0).build())
				.id(2L)
				.index(1)
				.skill(SkillIdDTO.builder().id(1L).build())
				.build();
	}
}
