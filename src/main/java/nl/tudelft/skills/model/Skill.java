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

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotNull
	@ManyToOne
	private Submodule submodule;

	@NotBlank
	private String name;

	@NotNull
	@Builder.Default
	private boolean essential = true;

	@NotNull
	@Builder.Default
	private boolean hidden = false;

	@Min(0)
	@NotNull
	@Column(name = "yPos")
	private Integer row;

	@Min(0)
	@NotNull
	@Column(name = "xPos")
	private Integer column;

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany
	private Set<Skill> parents = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "parents")
	private Set<Skill> children = new HashSet<>();

	@NotNull
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OrderBy("idx")
	@Cascade(CascadeType.DELETE)
	@OneToMany(mappedBy = "skill")
	private List<Task> tasks = new ArrayList<>();

	@NotNull
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	private Checkpoint checkpoint;

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).setIdx(i);
		}
	}
}
