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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.librador.exception.DTOValidationException;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.create.RegularTaskCreateDTO;
import nl.tudelft.skills.dto.create.TaskInfoCreateDTO;
import nl.tudelft.skills.dto.id.SkillIdDTO;
import nl.tudelft.skills.dto.patch.ChoiceTaskPatchDTO;
import nl.tudelft.skills.dto.patch.RegularTaskPatchDTO;
import nl.tudelft.skills.dto.patch.TaskInfoPatchDTO;
import nl.tudelft.skills.model.ChoiceTask;
import nl.tudelft.skills.model.TaskType;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TestChoiceTaskPatchDTO {
	private final TestDatabaseLoader db;

	@Autowired
	public TestChoiceTaskPatchDTO(TestDatabaseLoader db) {
		this.db = db;
	}

	@Test
	public void testNoSubTasks() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage()).contains("ChoiceTask has to contain at least two subtasks");
	}

	@Test
	public void testOneNewSubTask() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setNewSubTasks(List.of(getTaskCreateDTO()));
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage()).contains("ChoiceTask has to contain at least two subtasks");
	}

	@Test
	public void testOneUpdatedSubTask() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setUpdatedSubTasks(List.of(getTaskPatchDTO()));
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage()).contains("ChoiceTask has to contain at least two subtasks");
	}

	@Test
	public void testNumberMinTasksTooSmall() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setNewSubTasks(List.of(getTaskCreateDTO(), getTaskCreateDTO()));
		patchDTO.setMinTasks(0);
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage())
				.contains("minTasks should be larger than zero and smaller than the number of subtasks");
	}

	@Test
	public void testNumberMinTasksTooBig() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setNewSubTasks(List.of(getTaskCreateDTO()));
		patchDTO.setUpdatedSubTasks(List.of(getTaskPatchDTO()));
		patchDTO.setMinTasks(2);
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage())
				.contains("minTasks should be larger than zero and smaller than the number of subtasks");
	}

	@Test
	public void testNewTaskNotInSameSkill() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setUpdatedSubTasks(List.of(getTaskPatchDTO()));
		patchDTO.setMinTasks(2);

		// Not in same skill as choice task
		RegularTaskCreateDTO regularTask = getTaskCreateDTO();
		regularTask.getSkill().setId(patchDTO.getSkill().getId() - 1);
		patchDTO.setNewSubTasks(List.of(regularTask));

		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage())
				.contains("RegularTask is not in same Skill as ChoiceTask");
	}

	@Test
	public void testUpdatedTaskNotInSameSkill() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setNewSubTasks(List.of(getTaskCreateDTO()));
		patchDTO.setMinTasks(2);

		// Not in same skill as choice task
		RegularTaskPatchDTO regularTask = getTaskPatchDTO();
		regularTask.getSkill().setId(patchDTO.getSkill().getId() - 1);
		patchDTO.setUpdatedSubTasks(List.of(regularTask));

		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		Exception exception = assertThrows(DTOValidationException.class, () -> patchDTO.apply(choiceTask));
		assertThat(exception.getMessage())
				.contains("RegularTask is not in same Skill as ChoiceTask");
	}

	@Test
	public void testValidChoiceTaskPatchDTO() {
		ChoiceTaskPatchDTO patchDTO = getChoiceTaskPatchDTO();
		patchDTO.setNewSubTasks(List.of(getTaskCreateDTO()));
		patchDTO.setUpdatedSubTasks(List.of(getTaskPatchDTO()));
		patchDTO.setMinTasks(1);
		ChoiceTask choiceTask = db.getChoiceTaskBookOrVideo();
		assertDoesNotThrow(() -> patchDTO.apply(choiceTask));
	}

	/**
	 * Helper method to create a ChoiceTaskPatchDTO.
	 *
	 * @return A ChoiceTaskPatchDTO.
	 */
	public ChoiceTaskPatchDTO getChoiceTaskPatchDTO() {
		return ChoiceTaskPatchDTO
				.builder()
				.id(db.getChoiceTaskBookOrVideo().getId())
				.name("New name")
				.minTasks(1)
				.index(1)
				.skill(SkillIdDTO.builder().id(db.getSkillVariables().getId()).build())
				.build();
	}

	/**
	 * Helper method to create a RegularTaskCreateDTO.
	 *
	 * @return A RegularTaskCreateDTO.
	 */
	public RegularTaskCreateDTO getTaskCreateDTO() {
		return RegularTaskCreateDTO.builder()
				.taskInfo(TaskInfoCreateDTO.builder().name("New task").type(TaskType.READING).time(0).build())
				.index(1)
				.skill(SkillIdDTO.builder().id(db.getSkillVariables().getId()).build())
				.build();
	}

	/**
	 * Helper method to create a RegularTaskPatchDTO.
	 *
	 * @return A RegularTaskPatchDTO.
	 */
	public RegularTaskPatchDTO getTaskPatchDTO() {
		return RegularTaskPatchDTO.builder()
				.taskInfo(
						TaskInfoPatchDTO.builder().name("Video renamed").type(TaskType.VIDEO).time(0).build())
				.id(db.getTaskVideo().getId())
				.index(1)
				.skill(SkillIdDTO.builder().id(db.getSkillVariables().getId()).build())
				.build();
	}
}
