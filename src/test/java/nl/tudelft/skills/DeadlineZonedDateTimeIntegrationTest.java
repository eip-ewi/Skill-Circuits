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
package nl.tudelft.skills;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import nl.tudelft.skills.dto.view.CheckpointView;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.repository.TaskInfoRepository;

@Transactional
@SpringBootTest(classes = SkillCircuitsApplication.class)
public class DeadlineZonedDateTimeIntegrationTest {

	private final TaskInfoRepository taskInfoRepository;
	private final EntityManager entityManager;
	private final ObjectMapper objectMapper;

	@Autowired
	public DeadlineZonedDateTimeIntegrationTest(TaskInfoRepository taskInfoRepository,
			EntityManager entityManager, ObjectMapper objectMapper) {
		this.taskInfoRepository = taskInfoRepository;
		this.entityManager = entityManager;
		this.objectMapper = objectMapper;
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"2026-01-15T10:30:00+01:00[Europe/Amsterdam]",
			"2026-07-15T10:30:00+02:00[Europe/Amsterdam]"
	})
	public void deadlineRoundTripsThroughPersistenceAndJson(String value) {
		ZonedDateTime deadline = ZonedDateTime.parse(value);
		TaskInfo saved = taskInfoRepository.saveAndFlush(TaskInfo.builder()
				.name("Zoned deadline")
				.deadline(deadline)
				.build());

		entityManager.clear();
		ZonedDateTime persisted = taskInfoRepository.findByIdOrThrow(saved.getId()).getDeadline();
		String jsonDeadline = objectMapper.valueToTree(
				new CheckpointView(1L, "Checkpoint", persisted)).get("deadline").asText();

		assertThat(persisted.toInstant()).isEqualTo(deadline.toInstant());
		assertThat(ZonedDateTime.parse(jsonDeadline).toInstant()).isEqualTo(deadline.toInstant());
	}
}
