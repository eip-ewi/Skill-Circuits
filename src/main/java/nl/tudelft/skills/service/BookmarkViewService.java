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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.skills.cache.EditionCacheManager;
import nl.tudelft.skills.dto.view.BookmarkListView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.bookmark.BookmarkList;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;
import nl.tudelft.skills.model.bookmark.PersonalBookmarkList;
import nl.tudelft.skills.repository.TaskCompletionRepository;

@Service
@AllArgsConstructor
public class BookmarkViewService {

	private final EditionCacheManager editionCache;

	private final TaskCompletionRepository taskCompletionRepository;
	private final ModuleCircuitService moduleCircuitService;

	public List<BookmarkListView> convertToListViews(List<BookmarkList> lists, SCPerson person) {
		cacheEditions(lists);
		Set<Long> completedTaskIds = taskCompletionRepository.findAllCompletedTaskIdsForPerson(person);
		return lists.stream().map(list -> convertToListView(list, completedTaskIds))
				.sorted(Comparator.<BookmarkListView>comparingInt(list -> list.skill() == null ? 0 : 1)
						.thenComparing(BookmarkListView::lastModified, Comparator.reverseOrder()))
				.toList();
	}

	private BookmarkListView convertToListView(BookmarkList list, Set<Long> completedTaskIds) {
		return new BookmarkListView(
				list.getId(),
				getListName(list),
				list.getLastModified(),
				list.getSkills().stream().map(skill -> convertToListSkillView(skill, completedTaskIds))
						.toList(),
				Stream.concat(
						list.getTasks().stream().map(info -> convertToListTaskView(info, completedTaskIds)),
						list.getChoiceTasks().stream()
								.map(task -> convertToListTaskView(task, completedTaskIds)))
						.toList(),
				list instanceof HiddenSkillBookmarkList hiddenSkillBookmarkList
						? hiddenSkillBookmarkList.getSkill().getId()
						: null);
	}

	private String getListName(BookmarkList list) {
		return switch (list) {
			case PersonalBookmarkList personalBookmarkList -> personalBookmarkList.getName();
			case HiddenSkillBookmarkList hiddenSkillBookmarkList ->
				hiddenSkillBookmarkList.getSkill().getName();
			default -> ""; // Unreachable
		};
	}

	public BookmarkListView.BookmarkListSkillView convertToListSkillView(Skill skill, SCPerson person) {
		return convertToListSkillView(skill,
				taskCompletionRepository.findAllCompletedTaskIdsForPerson(person));
	}

	public BookmarkListView.BookmarkListSkillView convertToListSkillView(Skill skill,
			Set<Long> completedTaskIds) {
		return new BookmarkListView.BookmarkListSkillView(
				skill.getId(),
				skill.getName(),
				getQualifiedName(skill),
				skill.isEssential(),
				skill.getTasks().stream()
						.map(task -> moduleCircuitService.convertToTaskView(task, completedTaskIds))
						.toList());
	}

	public BookmarkListView.BookmarkListTaskView convertToListTaskView(TaskInfo taskInfo, SCPerson person) {
		return convertToListTaskView(taskInfo,
				taskCompletionRepository.findAllCompletedTaskIdsForPerson(person));
	}

	private BookmarkListView.BookmarkListTaskView convertToListTaskView(TaskInfo taskInfo,
			Set<Long> completedTaskIds) {
		return new BookmarkListView.BookmarkListTaskInfoView(
				taskInfo.getId(),
				taskInfo.getName(),
				taskInfo.getType(),
				taskInfo.getTime(),
				taskInfo.getLink(),
				completedTaskIds.contains(taskInfo.getId()),
				getQualifiedName(getSkill(taskInfo)) + " > " + taskInfo.getName());
	}

	public BookmarkListView.BookmarkListTaskView convertToListTaskView(ChoiceTask choiceTask,
			SCPerson person) {
		return convertToListTaskView(choiceTask,
				taskCompletionRepository.findAllCompletedTaskIdsForPerson(person));
	}

	private BookmarkListView.BookmarkListTaskView convertToListTaskView(ChoiceTask task,
			Set<Long> completedTaskIds) {
		return new BookmarkListView.BookmarkListChoiceTaskView(
				task.getId(),
				task.getName(),
				task.getMinTasks(),
				task.getTasks().stream().map(subtask -> convertToListSubtaskView(subtask, completedTaskIds))
						.toList(),
				getQualifiedName(task.getSkill()) + " > " + task.getName());
	}

	private BookmarkListView.BookmarkListSubtaskView convertToListSubtaskView(TaskInfo taskInfo,
			Set<Long> completedTaskIds) {
		return new BookmarkListView.BookmarkListSubtaskView(
				taskInfo.getId(),
				taskInfo.getName(),
				taskInfo.getType(),
				taskInfo.getTime(),
				taskInfo.getLink(),
				completedTaskIds.contains(taskInfo.getId()));
	}

	private String getQualifiedName(Skill skill) {
		EditionDetailsDTO edition = editionCache
				.getOrThrow(skill.getSubmodule().getModule().getEdition().getId());
		return edition.getCourse().getName() + " - " + edition.getName() + " > "
				+ skill.getSubmodule().getModule().getName() + " > " + skill.getSubmodule().getName() + " > "
				+ skill.getName();
	}

	private Skill getSkill(TaskInfo taskInfo) {
		return requireNonNull(taskInfo.getTask() == null ? taskInfo.getChoiceTask() : taskInfo.getTask())
				.getSkill();
	}

	private void cacheEditions(Collection<? extends BookmarkList> lists) {
		Stream<Long> editionIds = lists.stream()
				.flatMap(list -> Stream.concat(
						list.getSkills().stream(),
						Stream.concat(
								list.getTasks().stream().map(this::getSkill),
								list.getChoiceTasks().stream().map(Task::getSkill))))
				.map(skill -> skill.getSubmodule().getModule().getEdition().getId())
				.distinct();
		editionCache.get(editionIds);
	}
}
