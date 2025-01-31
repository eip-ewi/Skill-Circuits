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
package nl.tudelft.skills.model;

import javax.persistence.*;

import lombok.*;
import nl.tudelft.skills.model.labracore.SCPerson;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	private Path path;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	private SCEdition edition;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	private SCPerson person;
}
