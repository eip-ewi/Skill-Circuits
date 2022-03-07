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

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.edition.EditionLevelCourseViewDTO;
import nl.tudelft.skills.dto.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.EditionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditionService {

	private EditionControllerApi editionApi;
	private EditionRepository editionRepository;

	@Autowired
	public EditionService(EditionControllerApi editionApi, EditionRepository editionRepository) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
	}

	/**
	 * Return EditionViewDto for edition with id, including edition name and course from Labrador db and
	 * SCModules.
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

}
