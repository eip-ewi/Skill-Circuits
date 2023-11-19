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
package nl.tudelft.skills.model.labracore;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.PathPreference;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskCompletion;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SCPerson {

	@Id
	private Long id;

	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "person")
	private Set<TaskCompletion> taskCompletions = new HashSet<>();

	@NotNull
	@OneToOne(cascade = CascadeType.ALL)
	private Inventory inventory;

	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "person")
	private Set<PathPreference> pathPreferences = new HashSet<>();

	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany
	// For configuring a skill with any task in any path
	private Set<Task> tasksAdded = new HashSet<>();

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany
	@Builder.Default
	// For configuring a skill with any task in any path
	private Set<Skill> skillsModified = new HashSet<>();

}
