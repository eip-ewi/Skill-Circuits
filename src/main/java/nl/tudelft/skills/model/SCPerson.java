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

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.bookmark.PersonalBookmarkList;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SCPerson {

	@Id
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ViewMode viewMode;

	@NotNull
	@OneToOne(cascade = CascadeType.ALL)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PersonalPreferences preferences;

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "person")
	private Set<TaskCompletion> taskCompletions = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "person")
	private Set<PathPreference> pathPreferences = new HashSet<>();

	@NotNull
	@ManyToMany
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Task> tasksAdded = new HashSet<>();

	@NotNull
	@ManyToMany
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Task> tasksRemoved = new HashSet<>();

	@NotNull
	@ManyToMany
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	// To remember which skills have already been revealed
	private Set<Skill> skillsRevealed = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "editors")
	private Set<SCEdition> editorInEditions = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
	private Set<PersonalBookmarkList> bookmarkLists = new HashSet<>();

}
