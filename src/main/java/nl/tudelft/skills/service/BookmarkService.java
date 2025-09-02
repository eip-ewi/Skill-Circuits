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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.create.PersonalBookmarkListCreate;
import nl.tudelft.skills.dto.patch.PersonalBookmarkListPatch;
import nl.tudelft.skills.dto.view.BookmarkListView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.bookmark.BookmarkList;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;
import nl.tudelft.skills.model.bookmark.PersonalBookmarkList;
import nl.tudelft.skills.repository.bookmark.BookmarkListRepository;
import nl.tudelft.skills.repository.bookmark.HiddenSkillBookmarkListRepository;
import nl.tudelft.skills.repository.bookmark.PersonalBookmarkListRepository;

@Service
@AllArgsConstructor
public class BookmarkService {

	private final BookmarkViewService bookmarkViewService;
	private final EditionService editionService;
	private final HiddenSkillBookmarkListRepository hiddenSkillBookmarkListRepository;

	private final BookmarkListRepository bookmarkListRepository;
	private final PersonalBookmarkListRepository personalBookmarkListRepository;

	private final DTOConverter dtoConverter;

	public List<BookmarkListView> getBookmarksForPerson(SCPerson person) {
		Set<Long> managedEditions = editionService.getManagedEditionIds(person, false);
		Set<HiddenSkillBookmarkList> skillLists = hiddenSkillBookmarkListRepository
				.findAllByEditions(managedEditions);

		List<BookmarkList> allLists = new ArrayList<>();
		allLists.addAll(person.getBookmarkLists());
		allLists.addAll(skillLists);

		return bookmarkViewService.convertToListViews(allLists, person);
	}

	@Transactional
	public PersonalBookmarkList createPersonalBookmarkList(SCPerson person,
			PersonalBookmarkListCreate create) {
		PersonalBookmarkList list = create.apply(dtoConverter);
		list.setPerson(person);
		return personalBookmarkListRepository.save(list);
	}

	@Transactional
	public void patchPersonalBookmarkList(PersonalBookmarkList list, PersonalBookmarkListPatch patch) {
		patch.apply(list, dtoConverter);
		personalBookmarkListRepository.save(list);
	}

	@Transactional
	public void deletePersonalBookmarkList(PersonalBookmarkList list) {
		personalBookmarkListRepository.delete(list);
	}

	@Transactional
	public void addSkillToBookmarkList(BookmarkList list, Skill skill) {
		list.getSkills().add(skill);
		list.updateLastModified();
		bookmarkListRepository.save(list);
	}

	@Transactional
	public void removeSkillFromBookmarkList(BookmarkList list, Skill skill) {
		list.getSkills().remove(skill);
		list.updateLastModified();
		bookmarkListRepository.save(list);
	}

	@Transactional
	public void addTaskToBookmarkList(BookmarkList list, TaskInfo task) {
		list.getTasks().add(task);
		list.updateLastModified();
		bookmarkListRepository.save(list);
	}

	@Transactional
	public void removeTaskFromBookmarkList(BookmarkList list, TaskInfo task) {
		list.getTasks().remove(task);
		list.updateLastModified();
		bookmarkListRepository.save(list);
	}

	@Transactional
	public void addChoiceTaskToBookmarkList(BookmarkList list, ChoiceTask task) {
		list.getChoiceTasks().add(task);
		list.updateLastModified();
		bookmarkListRepository.save(list);
	}

	@Transactional
	public void removeChoiceTaskFromBookmarkList(BookmarkList list, ChoiceTask task) {
		list.getChoiceTasks().remove(task);
		list.updateLastModified();
		bookmarkListRepository.save(list);
	}
}
