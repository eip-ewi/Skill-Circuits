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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import nl.tudelft.skills.model.AbstractSkill;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;
import nl.tudelft.skills.repository.PersonRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;
import nl.tudelft.skills.repository.bookmark.HiddenSkillBookmarkListRepository;

@Service
@AllArgsConstructor
public class HiddenSkillRevealingService {

	private final HiddenSkillBookmarkListRepository hiddenSkillBookmarkListRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final SkillStateService skillStateService;

	private final PersonRepository personRepository;

	@Transactional
	public Set<Skill> revealSkillsAfterTaskCompletion(TaskInfo task, SCPerson person) {
		Set<HiddenSkillBookmarkList> candidates = new HashSet<>();
		candidates.addAll(hiddenSkillBookmarkListRepository.findAllByTasksContains(task));
		candidates.addAll(hiddenSkillBookmarkListRepository.findAllBySkillsContains(
				requireNonNull(task.getTask() != null ? task.getTask() : task.getChoiceTask()).getSkill()));
		if (task.getChoiceTask() != null) {
			candidates.addAll(
					hiddenSkillBookmarkListRepository.findAllByChoiceTasksContains(task.getChoiceTask()));
		}

		Set<Long> completedTaskIds = taskCompletionRepository.findAllCompletedTaskIdsForPerson(person);
        Set<Long> revealedSkillIds = person.getSkillsRevealed().stream().map(AbstractSkill::getId).collect(Collectors.toSet());
		Set<Skill> newRevealedSkills = candidates.stream()
				.filter(candidate -> isListCompleted(candidate, completedTaskIds, revealedSkillIds))
				.map(HiddenSkillBookmarkList::getSkill).collect(Collectors.toSet());

		person.getSkillsRevealed().addAll(newRevealedSkills);
		personRepository.save(person);

		return newRevealedSkills;
	}

	public boolean isListCompleted(HiddenSkillBookmarkList list, Set<Long> completedTaskIds, Set<Long> revealedSkillIds) {
		if (list.getTasks().stream().anyMatch(task -> !completedTaskIds.contains(task.getId()))) {
			return false;
		}
		if (list.getChoiceTasks().stream()
				.anyMatch(task -> !skillStateService.isTaskCompleted(task, completedTaskIds))) {
			return false;
		}
		if (list.getSkills().stream()
				.anyMatch(skill -> !skillStateService.isSkillCompleted(skill, completedTaskIds, revealedSkillIds))) {
			return false;
		}
		return true;
	}

}
