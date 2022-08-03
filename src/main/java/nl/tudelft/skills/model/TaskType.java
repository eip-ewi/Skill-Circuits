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

public enum TaskType {

	READING(
		"book"
	),
	VIDEO(
		"play"
	),
	QUIZ(
		"clipboard-question"
	),
	IMPLEMENTATION(
		"desktop"
	),
	EXERCISE(
		"pencil-ruler"
	),
	COLLABORATION(
		"people-carry"
	),
	EXPERIMENT(
		"flask"
	);

	private final String icon;

	TaskType(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}
}
