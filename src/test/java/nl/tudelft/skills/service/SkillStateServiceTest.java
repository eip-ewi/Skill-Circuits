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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nl.tudelft.skills.model.*;

public class SkillStateServiceTest {

	private final SkillStateService skillStateService;

	public SkillStateServiceTest() {
		this.skillStateService = new SkillStateService();
	}

	@Test
	@DisplayName("Non-placed skills are locked")
	public void nonPlacedSkillsAreNotUnlocked() {
		AbstractSkill skill = Skill.builder().column(null).build();
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of())).isFalse();
	}

	@Test
	@DisplayName("Skills are unlocked if one task is completed")
	public void skillUnlockedIfTaskCompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(10L), Set.of())).isTrue();
	}

	@Test
	@DisplayName("External skills are unlocked if one task is completed")
	public void externalSkillUnlockedIfTaskCompleted() {
		ExternalSkill skill = ExternalSkill.builder().column(1).build();
		skill.setSkill(Skill.builder()
				.tasks(List.of(regularTask(10L), regularTask(20L)))
				.build());
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(10L), Set.of())).isTrue();
	}

	@Test
	@DisplayName("Empty skills with essential parents that are not completed are locked")
	public void essentialParentNotCompletedIsNotUnlocked() {
		Skill skill = Skill.builder().column(1).build();
		skill.setParents(Set.of(
				Skill.builder().essential(true).column(1).tasks(List.of(regularTask(10L))).build(),
				Skill.builder().essential(false).column(1).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of())).isFalse();
	}

	@Test
	@DisplayName("Non-revealed hidden skills are locked")
	public void nonRevealedHiddenSkillsAreNotUnlocked() {
		Skill skill = Skill.builder().id(10L).hidden(true).column(1).build();
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of())).isFalse();
	}

	@Test
	@DisplayName("Skills with one parent locked are locked")
	public void someParentsNotUnlockedIsNotUnlocked() {
		Skill skill = Skill.builder().column(1).build();
		skill.setParents(Set.of(
				Skill.builder().essential(false).column(1).build(),
				Skill.builder().essential(false).column(null).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of())).isFalse();
	}

	@Test
	@DisplayName("Skills with all parents unlocked are unlocked")
	public void allParentsUnlockedIsUnlocked() {
		Skill skill = Skill.builder().column(1).build();
		skill.setParents(Set.of(
				Skill.builder().id(10L).essential(false).column(1).build(),
				Skill.builder().id(20L).essential(false).column(1).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of())).isTrue();
	}

	@Test
	@DisplayName("Empty unlocked skills are completed")
	public void emptyUnlockedSkillIsCompleted() {
		Skill skill = Skill.builder().column(1).build();
		assertThat(skillStateService.isSkillCompleted(skill, Set.of(), Set.of())).isTrue();
	}

	@Test
	@DisplayName("Skills with all tasks completed are completed")
	public void allTasksCompletedIsCompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		assertThat(skillStateService.isSkillCompleted(skill, Set.of(10L, 20L), Set.of())).isTrue();
	}

	private static Task regularTask(long taskInfoId) {
		return RegularTask.builder()
				.taskInfo(TaskInfo.builder()
						.id(taskInfoId)
						.build())
				.build();
	}

}
