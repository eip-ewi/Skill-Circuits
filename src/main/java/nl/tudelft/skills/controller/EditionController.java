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

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.service.EditionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("edition")
public class EditionController {
	private EditionRepository editionRepository;
	private EditionService editionService;
	private HttpSession session;

	@Autowired
	public EditionController(EditionRepository editionRepository, EditionService editionService,
			HttpSession session) {
		this.editionRepository = editionRepository;
		this.editionService = editionService;
		this.session = session;
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
		EditionLevelEditionViewDTO edition = editionService.getEditionView(id);

		Set<Pair<Integer, Integer>> positions = edition.getFilledPositions();
		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(0) + 1;
		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(0) + 1;
		Boolean studentMode = (Boolean) session.getAttribute("student-mode-" + id);

		model.addAttribute("level", "edition");
		model.addAttribute("edition", edition);
		model.addAttribute("columns", columns);
		model.addAttribute("rows", rows);
		if (positions.isEmpty()) {
			model.addAttribute("emptySpaces", List.of(Pair.of(0, 0)));
		} else {
			model.addAttribute("emptySpaces", IntStream.range(0, rows).boxed()
					.flatMap(row -> IntStream.range(0, columns).mapToObj(col -> Pair.of(col, row)))
					.filter(pos -> !positions.contains(pos))
					.toList());
		}

		model.addAttribute("emptyBlock", EditionLevelSubmoduleViewDTO.empty());
		model.addAttribute("emptyGroup", EditionLevelModuleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);

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

	/**
	 * Toggles student mode for a specific edition.
	 *
	 * @param  id The id of the edition
	 * @return    The edition page
	 */
	@PostMapping("{id}/studentmode")
	public String toggleStudentMode(@PathVariable Long id) {
		Boolean currentStudentMode = (Boolean) session.getAttribute("student-mode-" + id);
		session.setAttribute("student-mode-" + id,
				currentStudentMode == null || !currentStudentMode);
		return "redirect:/edition/{id}";
	}

}
