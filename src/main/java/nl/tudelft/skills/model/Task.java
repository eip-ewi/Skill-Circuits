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
package nl.tudelft.skills.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotNull
	@ManyToOne
	private Skill skill;

	@NotBlank
	private String name;

	private TaskType type;

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "tasks")
	private Set<Achievement> achievements = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "tasks")
	private Set<Path> paths = new HashSet<>();

}
