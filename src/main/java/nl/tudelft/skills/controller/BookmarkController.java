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
package nl.tudelft.skills.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.create.PersonalBookmarkListCreate;
import nl.tudelft.skills.dto.patch.PersonalBookmarkListPatch;
import nl.tudelft.skills.dto.view.BookmarkListView;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.bookmark.BookmarkList;
import nl.tudelft.skills.model.bookmark.PersonalBookmarkList;
import nl.tudelft.skills.service.BookmarkService;
import nl.tudelft.skills.service.BookmarkViewService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

	private final BookmarkService bookmarkService;
	private final BookmarkViewService bookmarkViewService;

	@GetMapping
	public List<BookmarkListView> getBookmarksForPerson(@AuthenticatedSCPerson SCPerson person) {
		return bookmarkService.getBookmarksForPerson(person);
	}

	@PostMapping
	public BookmarkListView createPersonalBookmarkList(@AuthenticatedSCPerson SCPerson person,
			@RequestBody PersonalBookmarkListCreate create) {
		PersonalBookmarkList list = bookmarkService.createPersonalBookmarkList(person, create);
		return new BookmarkListView(list.getId(), list.getName(), list.getLastModified(),
				Collections.emptyList(), Collections.emptyList(), null);
	}

	@PatchMapping("{list}")
	public void patchPersonalBookmarkList(@PathEntity PersonalBookmarkList list,
			@RequestBody PersonalBookmarkListPatch patch) {
		bookmarkService.patchPersonalBookmarkList(list, patch);
	}

	@DeleteMapping("{list}")
	public void deletePersonalBookmarkList(@PathEntity PersonalBookmarkList list) {
		bookmarkService.deletePersonalBookmarkList(list);
	}

	@PostMapping("{list}/skills/{skill}")
	@PreAuthorize("@authorisationService.canEditBookmarkList(#list)")
	public BookmarkListView.BookmarkListSkillView addSkillToBookmarkList(
			@AuthenticatedSCPerson SCPerson person, @PathEntity BookmarkList list, @PathEntity Skill skill) {
		bookmarkService.addSkillToBookmarkList(list, skill);
		return bookmarkViewService.convertToListSkillView(skill, person);
	}

	@DeleteMapping("{list}/skills/{skill}")
	@PreAuthorize("@authorisationService.canEditBookmarkList(#list)")
	public void removeSkillFromBookmarkList(@PathEntity BookmarkList list, @PathEntity Skill skill) {
		bookmarkService.removeSkillFromBookmarkList(list, skill);
	}

	@PostMapping("{list}/tasks/{task}")
	@PreAuthorize("@authorisationService.canEditBookmarkList(#list)")
	public BookmarkListView.BookmarkListTaskView addTaskToBookmarkList(@AuthenticatedSCPerson SCPerson person,
			@PathEntity BookmarkList list, @PathEntity TaskInfo task) {
		bookmarkService.addTaskToBookmarkList(list, task);
		return bookmarkViewService.convertToListTaskView(task, person);
	}

	@DeleteMapping("{list}/tasks/{task}")
	@PreAuthorize("@authorisationService.canEditBookmarkList(#list)")
	public void removeTaskFromBookmarkList(@PathEntity BookmarkList list, @PathEntity TaskInfo task) {
		bookmarkService.removeTaskFromBookmarkList(list, task);
	}

	@PostMapping("{list}/choice-tasks/{task}")
	@PreAuthorize("@authorisationService.canEditBookmarkList(#list)")
	public BookmarkListView.BookmarkListTaskView addChoiceTaskToBookmarkList(
			@AuthenticatedSCPerson SCPerson person, @PathEntity BookmarkList list,
			@PathEntity ChoiceTask task) {
		bookmarkService.addChoiceTaskToBookmarkList(list, task);
		return bookmarkViewService.convertToListTaskView(task, person);
	}

	@DeleteMapping("{list}/choice-tasks/{task}")
	@PreAuthorize("@authorisationService.canEditBookmarkList(#list)")
	public void removeChoiceTaskFromBookmarkList(@PathEntity BookmarkList list, @PathEntity ChoiceTask task) {
		bookmarkService.removeChoiceTaskFromBookmarkList(list, task);
	}

}
