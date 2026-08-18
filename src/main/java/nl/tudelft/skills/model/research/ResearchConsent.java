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
package nl.tudelft.skills.model.research;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.skills.model.SCPerson;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchConsent {

	@Data
	@Embeddable
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ResearchConsentId {
		private Long researchInfoId;
		private Long personId;
	}

	@EmbeddedId
	private ResearchConsentId id;

	@NotNull
	@ManyToOne
	@MapsId("researchInfoId")
	private ResearchInfo researchInfo;

	@NotNull
	@ManyToOne
	@MapsId("personId")
	private SCPerson person;

	@NotNull
	private Boolean consentGiven;

	public ResearchConsent(Long researchInfoId, Long personId, boolean consentGiven) {
		this.id = new ResearchConsentId(researchInfoId, personId);
		this.consentGiven = consentGiven;
	}

}
