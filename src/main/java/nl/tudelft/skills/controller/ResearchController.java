/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.ParamEntity;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.dto.view.research.ResearchConsentView;
import nl.tudelft.skills.dto.view.research.ResearchInfoView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.service.ResearchService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/research")
public class ResearchController {

	private final ResearchService researchService;

	@GetMapping
	@PreAuthorize("@authorisationService.canViewEdition(#edition.id)")
	public ResearchInfoView getEditionResearchInfo(@ParamEntity SCEdition edition) {
		return researchService.getResearchInfo(edition);
	}

	@PutMapping
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#edition.id)")
	public void updateResearchInfo(@ParamEntity SCEdition edition, @RequestBody String consentInfo) {
		researchService.updateResearchInfo(edition, consentInfo);
	}

	@DeleteMapping
	@PreAuthorize("@authorisationService.canEditEditionCircuit(#edition.id)")
	public void removeResearch(@ParamEntity SCEdition edition) {
		researchService.removeResearch(edition);
	}

	@GetMapping("consent")
	@PreAuthorize("@authorisationService.canViewEdition(#edition.id)")
	public ResearchConsentView getConsent(@AuthenticatedSCPerson SCPerson person,
			@ParamEntity SCEdition edition) {
		return researchService.getResearchConsent(person, edition);
	}

	@PutMapping("consent")
	@PreAuthorize("@authorisationService.canViewEdition(#edition.id)")
	public void updateConsent(@AuthenticatedSCPerson SCPerson person, @ParamEntity SCEdition edition,
			@RequestParam boolean consent) {
		researchService.updateResearchConsent(person, edition, consent);
	}

}
