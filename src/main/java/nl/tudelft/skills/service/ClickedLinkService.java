/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
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

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.repository.ClickedLinkRepository;

@Service
public class ClickedLinkService {
	private final ClickedLinkRepository clickedLinkRepository;

	@Autowired
	public ClickedLinkService(ClickedLinkRepository clickedLinkRepository) {
		this.clickedLinkRepository = clickedLinkRepository;
	}

	/**
	 * Deletes the clicked links belonging to specific tasks
	 *
	 * @param tasks The list of tasks the clicked links should be deleted for
	 */
	@Transactional
	public void deleteClickedLinksForTasks(List<RegularTask> tasks) {
		var taskIds = tasks.stream().map(RegularTask::getId).toList();
		clickedLinkRepository.deleteAllByTaskIdIn(taskIds);
	}
}
