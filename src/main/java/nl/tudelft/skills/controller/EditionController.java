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

import javax.transaction.Transactional;

import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.service.EditionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("edition")
public class EditionController {
	private EditionRepository editionRepository;
	private EditionService editionService;

	@Autowired
	public EditionController(EditionRepository editionRepository, EditionService editionService) {
		this.editionRepository = editionRepository;
		this.editionService = editionService;
	}

	/**
	 * Gets the page for a single edition. This page contains a list with all modules in the edition.
	 *
	 * @param  id    The id of the edition
	 * @param  model The model to add data to
	 * @return       The page to load
	 */
	@GetMapping("{id}")
	@PreAuthorize("@authorisationService.canViewEdition(#id)")
	public String getEditionPage(@PathVariable Long id, Model model) {
		model.addAttribute("edition", editionService.getEditionView(id));
		return "edition/view";
	}

	/**
	 * Sets the edition to visible for students.
	 *
	 * @param  id The id of the edition
	 * @return    The page to load
	 */
	@PostMapping("{id}/publish")
	@Transactional
	@PreAuthorize("@authorisationService.canPublishEdition(#id)")
	public String publishEdition(@PathVariable Long id) {
		SCEdition edition = editionRepository.findByIdOrThrow(id);
		edition.setVisible(true);
		editionRepository.save(edition);

		return "redirect:/edition/" + id.toString();
	}

	/**
	 * Sets the edition to invisible for students.
	 *
	 * @param  id The id of the edition
	 * @return    The page to load
	 */

	@PostMapping("{id}/unpublish")
	@Transactional
	@PreAuthorize("@authorisationService.canPublishEdition(#id)")
	public String unpublishEdition(@PathVariable Long id) {

		SCEdition edition = editionRepository.findByIdOrThrow(id);

		edition.setVisible(false);
		editionRepository.save(edition);

		return "redirect:/edition/" + id.toString();
	}
}
