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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.repository.ModuleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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
		ModuleLevelModuleViewDTO module = View.convert(moduleRepository.findByIdOrThrow(id),
				ModuleLevelModuleViewDTO.class);
		Set<Pair<Integer, Integer>> positions = module.getSubmodules().stream()
				.flatMap(s -> s.getSkills().stream())
				.map(s -> Pair.of(s.getColumn(), s.getRow())).collect(Collectors.toSet());
		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(-1) + 1;
		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(-1) + 1;

		model.addAttribute("module", module);
		model.addAttribute("columns", columns);
		model.addAttribute("rows", rows);
		model.addAttribute("spaces", IntStream.range(0, rows).boxed()
				.flatMap(row -> IntStream.range(0, columns).mapToObj(col -> Pair.of(col, row)))
				.filter(pos -> !positions.contains(pos))
				.toList());

		return "module/view";
	}

}