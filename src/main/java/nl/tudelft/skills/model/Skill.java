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

import java.util.*;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import nl.tudelft.skills.model.bookmark.BookmarkList;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Skill extends AbstractSkill {

	@NotBlank
	private String name;

	@NotNull
	@ManyToOne
	private Submodule submodule;

	@NotNull
	@Builder.Default
	private boolean hidden = false;

	@Nullable
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne(cascade = CascadeType.ALL)
	private HiddenSkillBookmarkList requirements;

	@Setter
	@NotNull
	@OrderBy("idx")
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "skill", cascade = CascadeType.REMOVE)
	private List<Task> tasks = new ArrayList<>();

	@Nullable
	@ManyToOne
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Checkpoint checkpoint;

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "skill", cascade = CascadeType.REMOVE)
	private Set<ExternalSkill> externalSkills = new HashSet<>();

	@ManyToOne
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Skill previousEditionSkill;

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "previousEditionSkill")
	private Set<Skill> futureEditionSkills = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "skillsRevealed")
	private Set<SCPerson> personRevealedSkill = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "skills")
	private Set<BookmarkList> onLists = new HashSet<>();

}
