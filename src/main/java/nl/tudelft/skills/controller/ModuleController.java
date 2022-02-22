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
package nl.tudelft.skills.controller;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.repository.ModuleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("module")
public class ModuleController {

	private ModuleRepository moduleRepository;

	@Autowired
	public ModuleController(ModuleRepository moduleRepository) {
		this.moduleRepository = moduleRepository;
	}

	/**
	 * Gets the page for a single module. This page contains a circuit with all submodules, skills, and tasks
	 * in the module.
	 *
	 * @param  id    The id of the module
	 * @param  model The model to add data to
	 * @return       The page to load
	 */
	@GetMapping("{id}")
	public String getModulePage(@PathVariable Long id, Model model) {
		model.addAttribute("module",
				View.convert(moduleRepository.findByIdOrThrow(id), ModuleLevelModuleViewDTO.class));
		return "module/view";
	}

}
