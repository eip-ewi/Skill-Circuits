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

import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.repository.ModuleRepository;
import nl.tudelft.skills.repository.labracore.SCPersonRepository;

import org.springframework.stereotype.Service;

@Service
public class ModuleService {

	private final ModuleRepository moduleRepository;
	private final SCPersonRepository personRepository;

	public ModuleService(ModuleRepository moduleRepository, SCPersonRepository personRepository) {
		this.moduleRepository = moduleRepository;
		this.personRepository = personRepository;
	}

	/**
	 * In module level view, marks all tasks that are in that module and have been completed by a person as
	 * completed.
	 *
	 * @param moduleViewDTO the dto to set completed attributes for
	 * @param personId      the id of the person
	 */
	public void setCompletedTasksForPerson(ModuleLevelModuleViewDTO moduleViewDTO, Long personId) {
		List<Long> completedTasks = personRepository.getById(personId).getTasksCompleted().stream()
				.map(Task::getId).toList();
		moduleViewDTO.getSubmodules().forEach(sub -> sub.getSkills().forEach(skill -> skill.getTasks()
				.forEach(task -> task.setCompleted(completedTasks.contains(task.getId())))));
	}

}
