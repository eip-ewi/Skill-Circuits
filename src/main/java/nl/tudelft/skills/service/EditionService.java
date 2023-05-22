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
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.edition.*;
import nl.tudelft.skills.model.Path;
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

		Set<PathViewDTO> pathsInEdition = getPaths(edition.getId());

		model.addAttribute("level", "edition");
		model.addAttribute("edition", edition);
		circuitService.setCircuitAttributes(model, positions, columns, rows);

		model.addAttribute("emptyBlock", EditionLevelSubmoduleViewDTO.empty());
		model.addAttribute("emptyGroup", EditionLevelModuleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);

		model.addAttribute("paths", pathsInEdition);
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

		// TODO asserting on the new attribute in additional tests
		List<EditionLevelEditionSummaryDTO> olderEditions = editionApi
				.getAllEditionsByCourse(edition.getCourse().getId())
				.collectList().block().stream()
				.filter(dto -> dto.getStartDate().isBefore(edition.getStartDate()))
				.map(dto -> new EditionLevelEditionSummaryDTO(dto.getId(), dto.getName()))
				.collect(Collectors.toList());

		view.setName(edition.getName());
		view.setCourse(
				new EditionLevelCourseViewDTO(edition.getCourse().getId(), edition.getCourse().getName(),
						olderEditions));
		return view;
	}

	/**
	 * Returns a list of PathViewDTOs with all the paths in this edition.
	 *
	 * @param  editionId Edition id.
	 * @return           List of PathViewDTOs for edition with id.
	 */
	public Set<PathViewDTO> getPaths(Long editionId) {
		return editionRepository.findById(editionId).get()
				.getPaths().stream().map(p -> View.convert(p, PathViewDTO.class))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns the default path for an edition if it exists.
	 *
	 * @param  editionId The edition id
	 * @return           The default path of an edition if it exists, null otherwise.
	 */
	public Path getDefaultPath(Long editionId) {
		return editionRepository.findById(editionId).get().getDefaultPath();
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

}
