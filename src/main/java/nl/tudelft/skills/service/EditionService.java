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

import java.util.Set;

import javax.servlet.http.HttpSession;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelModuleViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelSubmoduleViewDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.EditionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
public class EditionService {

	private EditionControllerApi editionApi;
	private EditionRepository editionRepository;
	private CircuitService circuitService;

	@Autowired
	public EditionService(EditionControllerApi editionApi, EditionRepository editionRepository,
			CircuitService circuitService) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		this.circuitService = circuitService;
	}

	/**
	 * Configures the model for the module circuit view.
	 *
	 * @param id      The id of the module
	 * @param model   The module to configure
	 * @param session The http session
	 */
	public void configureEditionModel(Long id, Model model, HttpSession session) {
		EditionLevelEditionViewDTO edition = getEditionView(id);

		Set<Pair<Integer, Integer>> positions = edition.getFilledPositions();
		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(0) + 1;
		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(0) + 1;
		Boolean studentMode = (Boolean) session.getAttribute("student-mode-" + id);

		model.addAttribute("level", "edition");
		model.addAttribute("edition", edition);
		circuitService.setCircuitAttributes(model, positions, columns, rows);

		model.addAttribute("emptyBlock", EditionLevelSubmoduleViewDTO.empty());
		model.addAttribute("emptyGroup", EditionLevelModuleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);
	}

	/**
	 * Return EditionLevelEditionViewDto for edition with id, including edition name and course from Labrador
	 * db and SCModules.
	 *
	 * @param  id Edition id.
	 * @return    EditionViewDTO for edition with id.
	 */
	public EditionLevelEditionViewDTO getEditionView(Long id) {
		EditionDetailsDTO edition = editionApi.getEditionById(id).block();

		EditionLevelEditionViewDTO view = View.convert(getOrCreateSCEdition(id),
				EditionLevelEditionViewDTO.class);

		view.setName(edition.getName());
		view.setCourse(
				new EditionLevelCourseViewDTO(edition.getCourse().getId(), edition.getCourse().getName()));
		return view;
	}

	/**
	 * Returns a SCEdition by edition id. If it doesn't exist, creates one.
	 *
	 * @param  id The id of the edition
	 * @return    The SCEdition with id.
	 */
	@Transactional
	public SCEdition getOrCreateSCEdition(Long id) {
		return editionRepository.findById(id)
				.orElseGet(() -> editionRepository.save(SCEdition.builder().id(id).build()));
	}

	/**
	 * Returns the number of skills in the edition.
	 *
	 * @param  id Edition id
	 * @return    The number of skills that the edition contains.
	 */
	public Integer getNumberOfSkillsInEdition(Long id) {
		SCEdition edition = getOrCreateSCEdition(id);

		int skillCount = 0;
		for (var module : edition.getModules()) {
			for (var submodule : module.getSubmodules()) {
				skillCount += submodule.getSkills().size();
			}
		}
		return skillCount;
	}

}
