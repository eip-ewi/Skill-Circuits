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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import nl.tudelft.skills.enums.DeadlineDisplay;
import nl.tudelft.skills.enums.Theme;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.PersonalPreferences;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.PersonRepository;
import nl.tudelft.skills.service.PersonalPreferencesService;

@Transactional
@SpringBootTest(classes = SkillCircuitsApplication.class)
public class PersonalPreferencesIntegrationTest {

	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private PersonalPreferencesService preferencesService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ObjectMapper objectMapper;

	@ParameterizedTest
	@EnumSource(DeadlineDisplay.class)
	public void deadlineAndAccessibilityPreferencesSurviveIndependentUpdates(DeadlineDisplay display) {
		SCPerson person = personRepository.saveAndFlush(SCPerson.builder()
				.id(-267L)
				.viewMode(ViewMode.VIEWER)
				.preferences(PersonalPreferences.builder().build())
				.build());

		preferencesService.setAdditionalIcons(person, true);
		preferencesService.setTheme(person, Theme.HIGH_CONTRAST_DARK);
		preferencesService.setDeadlineDisplay(person, display);
		JsonNode enabled = persistedPreferences();
		assertThat(enabled.get("deadlineDisplay").asText()).isEqualTo(display.name());
		assertThat(enabled.get("additionalIcons").asBoolean()).isTrue();
		assertThat(enabled.get("blurBlocks").asBoolean()).isTrue();
		assertThat(enabled.get("theme").get("name").asText()).isEqualTo(Theme.HIGH_CONTRAST_DARK.getName());

		person = personRepository.findByIdOrThrow(person.getId());
		preferencesService.setAdditionalIcons(person, false);
		preferencesService.setBlurBlocks(person, false);
		JsonNode disabled = persistedPreferences();
		assertThat(disabled.get("deadlineDisplay").asText()).isEqualTo(display.name());
		assertThat(disabled.get("additionalIcons").asBoolean()).isFalse();
		assertThat(disabled.get("blurBlocks").asBoolean()).isFalse();
		assertThat(disabled.get("theme").get("name").asText()).isEqualTo(Theme.HIGH_CONTRAST_DARK.getName());
	}

	private JsonNode persistedPreferences() {
		entityManager.flush();
		entityManager.clear();
		PersonalPreferences preferences = personRepository.findByIdOrThrow(-267L).getPreferences();
		return objectMapper.valueToTree(preferencesService.convertToPreferencesView(preferences));
	}
}
