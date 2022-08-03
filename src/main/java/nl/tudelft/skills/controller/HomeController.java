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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCModule;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.ModuleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final EditionControllerApi editionApi;
	private final RoleControllerApi roleApi;

	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;

	@Autowired
	public HomeController(EditionControllerApi editionApi, RoleControllerApi roleApi,
			EditionRepository editionRepository, ModuleRepository moduleRepository) {
		this.editionApi = editionApi;
		this.roleApi = roleApi;
		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
	}

	/**
	 * Gets the home page. Loads all courses that the authenticated person teaches or that have active and
	 * visible editions.
	 *
	 * @param  person The authenticated person, or null if the user is unauthenticated
	 * @param  model  The model to add details to
	 * @return        The home page
	 */
	@Transactional
	@GetMapping("/")
	public String getHomePage(@AuthenticatedPerson(required = false) Person person, Model model) {
		List<EditionDetailsDTO> editions = person == null
				? editionApi.getEditionsById(editionApi.getAllEditionsActiveAtDate(LocalDateTime.now())
						.map(EditionSummaryDTO::getId).collectList().block()).collectList().block()
				: editionApi.getAllEditionsActiveOrTaughtBy(person.getId()).collectList().block();
		Set<Long> visible = editionRepository
				.findAllById(editions.stream().map(EditionDetailsDTO::getId).toList()).stream()
				.filter(SCEdition::isVisible).map(SCEdition::getId).collect(Collectors.toSet());
		Set<Long> teacherIds = person == null ? Set.of()
				: roleApi
						.getRolesById(editions.stream().map(EditionDetailsDTO::getId).toList(),
								List.of(person.getId()))
						.collectList().block().stream()
						.filter(r -> r.getType() == RoleDetailsDTO.TypeEnum.TEACHER)
						.map(r -> r.getEdition().getId()).collect(Collectors.toSet());

		List<CourseSummaryDTO> courses = editions.stream()
				.filter(e -> visible.contains(e.getId()) || teacherIds.contains(e.getId()))
				.map(EditionDetailsDTO::getCourse).distinct().toList();

		model.addAttribute("courses", courses);

		// Temporarily put all modules on the home page
		List<SCModule> allModules = moduleRepository.findAll();
		model.addAttribute("moduleIds", allModules.stream().map(SCModule::getId).toList());
		model.addAttribute("moduleNames",
				allModules.stream().collect(Collectors.toMap(SCModule::getId, SCModule::getName)));

		return "index";
	}

	/**
	 * Gets the page for logging in (development/LDAP only).
	 *
	 * @return The page to load
	 */
	@GetMapping("auth/login")
	public String getLoginPage() {
		return "login";
	}

}
