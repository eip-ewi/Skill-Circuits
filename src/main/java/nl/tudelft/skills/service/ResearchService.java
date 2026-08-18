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
package nl.tudelft.skills.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.view.research.ResearchConsentView;
import nl.tudelft.skills.dto.view.research.ResearchInfoView;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.research.ResearchConsent;
import nl.tudelft.skills.model.research.ResearchInfo;
import nl.tudelft.skills.repository.research.ResearchConsentRepository;
import nl.tudelft.skills.repository.research.ResearchInfoRepository;

@Service
@AllArgsConstructor
public class ResearchService {

	private final ResearchInfoRepository researchInfoRepository;
	private final ResearchConsentRepository researchConsentRepository;

	@Transactional
	public ResearchInfoView getResearchInfo(SCEdition edition) {
		ResearchInfo researchInfo = edition.getResearchInfo();
		if (researchInfo == null) {
			return new ResearchInfoView(false, null);
		} else {
			return new ResearchInfoView(true, researchInfo.getConsentInfo());
		}
	}

	@Transactional
	public void updateResearchInfo(SCEdition edition, String consentInfo) {
		ResearchInfo researchInfo = researchInfoRepository.findByEditionId(edition.getId())
				.orElseGet(() -> ResearchInfo.builder().edition(edition).consentInfo(consentInfo).build());

		researchInfo.setConsentInfo(consentInfo);
		researchInfo = researchInfoRepository.save(researchInfo);
		edition.setResearchInfo(researchInfo);

		researchConsentRepository.deleteAllByResearchInfoId(researchInfo.getId());
	}

	@Transactional
	public void removeResearch(SCEdition edition) {
		edition.setResearchInfo(null);
		researchInfoRepository.deleteByEditionId(edition.getId());
	}

	@Transactional
	public ResearchConsentView getResearchConsent(SCPerson person, SCEdition edition) {
		return researchInfoRepository.findByEditionId(edition.getId())
				.flatMap(researchInfo -> researchConsentRepository
						.findById(new ResearchConsent.ResearchConsentId(researchInfo.getId(), person.getId()))
						.map(consent -> new ResearchConsentView(consent.getConsentGiven(),
								consent.getParticipantId())))
				.orElseGet(() -> new ResearchConsentView(null, null));
	}

	@Transactional
	public void updateResearchConsent(SCPerson person, SCEdition edition, boolean consent) {
		researchInfoRepository.findByEditionId(edition.getId()).ifPresent(researchInfo -> {
			researchConsentRepository.save(ResearchConsent.builder()
					.id(new ResearchConsent.ResearchConsentId(researchInfo.getId(), person.getId()))
					.researchInfo(researchInfo)
					.person(person)
					.consentGiven(consent)
					.build());
		});
	}

}
