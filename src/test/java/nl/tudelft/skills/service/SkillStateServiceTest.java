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
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of(), null, Set.of(), Set.of()))
				.isFalse();
	}

	@Test
	@DisplayName("Skills are unlocked if one task on path is completed")
	public void skillUnlockedIfTaskOnPathCompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		skill.setParents(Set.of(Skill.builder().column(1).tasks(List.of(regularTask(30L))).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(10L), Set.of(), null, Set.of(), Set.of()))
				.isTrue();
	}

	@Test
	@DisplayName("Skills are locked if the only completed task is not on the path")
	public void skillLockedIfTaskNotOnPathCompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		skill.setParents(Set.of(Skill.builder().column(1).tasks(List.of(regularTask(30L))).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(10L), Set.of(), null, Set.of(),
				Set.of(regularTask(10L)))).isFalse();
	}

	@Test
	@DisplayName("External skills are unlocked if one task is completed")
	public void externalSkillUnlockedIfTaskCompleted() {
		ExternalSkill skill = ExternalSkill.builder().column(1).build();
		skill.setSkill(Skill.builder()
				.tasks(List.of(regularTask(10L), regularTask(20L)))
				.build());
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(10L), Set.of(), null, Set.of(), Set.of()))
				.isTrue();
	}

	@Test
	@DisplayName("Empty skills with essential parents that are not completed are locked")
	public void essentialParentNotCompletedIsNotUnlocked() {
		Skill skill = Skill.builder().column(1).build();
		skill.setParents(Set.of(
				Skill.builder().essential(true).column(1).tasks(List.of(regularTask(10L))).build(),
				Skill.builder().essential(false).column(1).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of(), null, Set.of(), Set.of()))
				.isFalse();
	}

	@Test
	@DisplayName("Non-revealed hidden skills are locked")
	public void nonRevealedHiddenSkillsAreNotUnlocked() {
		Skill skill = Skill.builder().id(10L).hidden(true).column(1).build();
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of(), null, Set.of(), Set.of()))
				.isFalse();
	}

	@Test
	@DisplayName("Skills with one parent locked are locked")
	public void someParentsNotUnlockedIsNotUnlocked() {
		Skill skill = Skill.builder().column(1).build();
		skill.setParents(Set.of(
				Skill.builder().essential(false).column(1).build(),
				Skill.builder().essential(false).column(null).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of(), null, Set.of(), Set.of()))
				.isFalse();
	}

	@Test
	@DisplayName("Skills with all parents unlocked are unlocked")
	public void allParentsUnlockedIsUnlocked() {
		Skill skill = Skill.builder().column(1).build();
		skill.setParents(Set.of(
				Skill.builder().id(10L).essential(false).column(1).build(),
				Skill.builder().id(20L).essential(false).column(1).build()));
		assertThat(skillStateService.isSkillUnlocked(skill, Set.of(), Set.of(), null, Set.of(), Set.of()))
				.isTrue();
	}

	@Test
	@DisplayName("Empty unlocked skills are completed")
	public void emptyUnlockedSkillIsCompleted() {
		Skill skill = Skill.builder().column(1).build();
		assertThat(skillStateService.isSkillCompleted(skill, Set.of(), Set.of(), null, Set.of(), Set.of()))
				.isTrue();
	}

	@Test
	@DisplayName("Skills with all tasks completed are completed")
	public void allTasksCompletedIsCompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		assertThat(skillStateService.isSkillCompleted(skill, Set.of(10L, 20L), Set.of(), null, Set.of(),
				Set.of())).isTrue();
	}

	@Test
	@DisplayName("Skills with completed tasks on path and removed uncompleted tasks are completed")
	public void allTasksOnPathCompletedUncompletedRemovedIsCompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		// Remove the uncompleted task from the path
		assertThat(skillStateService.isSkillCompleted(skill, Set.of(10L), Set.of(), null, Set.of(),
				Set.of(regularTask(20L)))).isTrue();
	}

	@Test
	@DisplayName("Skills with an uncompleted task on the path and one completed removed task are uncompleted")
	public void completedTaskRemovedIsUncompleted() {
		Skill skill = Skill.builder().column(1).build();
		skill.setTasks(List.of(regularTask(10L), regularTask(20L)));
		// Remove the completed task from the path
		assertThat(skillStateService.isSkillCompleted(skill, Set.of(10L), Set.of(), null, Set.of(),
				Set.of(regularTask(10L)))).isFalse();
	}

	@Test
	@DisplayName("Skills with completed tasks on path and uncompleted tasks not on path are completed")
	public void allTasksOnPathCompletedUncompletedNotOnPathIsCompleted() {
		// The uncompleted task is not on the active path

		Skill skill = Skill.builder().column(1).build();
		Task taskOnPath = regularTask(10L);
		Task taskNotOnPath = regularTask(20L);
		skill.setTasks(List.of(taskOnPath, taskNotOnPath));

		Path activePath = Path.builder().tasks(Set.of(taskOnPath)).build();
		taskOnPath.getPaths().add(activePath);

		assertThat(skillStateService.isSkillCompleted(skill, Set.of(10L), Set.of(), activePath,
				Set.of(),
				Set.of())).isTrue();
	}

	@Test
	@DisplayName("Tasks on path with all on active path, none removed, returns all")
	public void allTasksOnActivePathNoneRemovedReturnsAll() {
		Skill skill = Skill.builder().column(1).build();
		Task taskA = regularTask(10L);
		Task taskB = regularTask(20L);
		skill.setTasks(List.of(taskA, taskB));

		Path activePath = Path.builder().tasks(Set.of(taskA, taskB)).build();
		taskA.getPaths().add(activePath);
		taskB.getPaths().add(activePath);

		assertThat(skillStateService.getTasksOnPath(skill, activePath, Set.of(), Set.of()))
				.containsExactlyInAnyOrder(taskA, taskB);
	}

	@Test
	@DisplayName("Tasks on path of external skill with all on active path, none removed, returns all")
	public void externalSkillAllTasksOnActivePathNoneRemovedReturnsAll() {
		ExternalSkill skill = ExternalSkill.builder().column(1).build();
		Task task = regularTask(10L);
		skill.setSkill(Skill.builder()
				.tasks(List.of(task))
				.build());

		Path activePath = Path.builder().tasks(Set.of(task)).build();
		task.getPaths().add(activePath);

		assertThat(skillStateService.getTasksOnPath(skill, activePath, Set.of(), Set.of()))
				.containsExactly(task);
	}

	@Test
	@DisplayName("Tasks on path with one removed one added only returns the added")
	public void allTasksOnPathOneAddedOneRemovedReturnsAdded() {
		Skill skill = Skill.builder().column(1).build();
		Task taskAdded = regularTask(10L);
		Task taskRemoved = regularTask(20L);
		skill.setTasks(List.of(taskAdded, taskRemoved));

		assertThat(skillStateService.getTasksOnPath(skill, null, Set.of(taskAdded), Set.of(taskRemoved)))
				.containsExactlyInAnyOrder(taskAdded);
	}

	@Test
	@DisplayName("No active path and task removed is not on path")
	public void noActivePathAndTaskRemovedIsNotOnPath() {
		Task task = regularTask(10L);
		assertThat(skillStateService.isTaskOnPath(task, null, Set.of(), Set.of(task))).isFalse();
	}

	@Test
	@DisplayName("No active path and task not removed is on path")
	public void noActivePathAndTaskIsNotRemovedIsOnPath() {
		Task task = regularTask(10L);
		assertThat(skillStateService.isTaskOnPath(task, null, Set.of(), Set.of())).isTrue();
	}

	@Test
	@DisplayName("Task on active path but removed is not on path")
	public void onActivePathAndTaskIsRemovedIsNotOnPath() {
		Task task = regularTask(10L);
		Path activePath = Path.builder().tasks(Set.of(task)).build();
		task.getPaths().add(activePath);
		assertThat(skillStateService.isTaskOnPath(task, activePath, Set.of(), Set.of(task))).isFalse();
	}

	@Test
	@DisplayName("Task on active path and not removed nor added is on path")
	public void onActivePathAndTaskIsNotRemovedNotAddedIsOnPath() {
		Task task = regularTask(10L);
		Path activePath = Path.builder().tasks(Set.of(task)).build();
		task.getPaths().add(activePath);
		assertThat(skillStateService.isTaskOnPath(task, activePath, Set.of(), Set.of())).isTrue();
	}

	@Test
	@DisplayName("Task is not on active path and added is on path")
	public void notOnActivePathAndTaskIsAddedIsOnPath() {
		Task task = regularTask(10L);
		Path activePath = Path.builder().build();
		assertThat(skillStateService.isTaskOnPath(task, activePath, Set.of(task), Set.of())).isTrue();
	}

	private static Task regularTask(long taskInfoId) {
		return RegularTask.builder()
				.taskInfo(TaskInfo.builder()
						.id(taskInfoId)
						.build())
				.build();
	}

}
