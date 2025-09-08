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
package nl.tudelft.skills.service.old;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.data.util.Pair;
import org.springframework.ui.Model;

public class CircuitService {

	/**
	 * Sets the model attributes necessary for creating the circuit. Sets the number of rows and columns, as
	 * well as the empty spaces. If there are no blocks in the circuit, sets a single empty space.
	 *
	 * @param model     the model to add attributes to.
	 * @param positions the set of positions that are filled in the circuit.
	 * @param columns   the amount of columns in the circuit.
	 * @param rows      the amount of rows in the circuit.
	 */
	public void setCircuitAttributes(Model model, Set<Pair<Integer, Integer>> positions, int columns,
			int rows) {
		model.addAttribute("columns", columns);
		model.addAttribute("rows", rows);

		if (positions.isEmpty()) {
			model.addAttribute("emptySpaces", List.of(Pair.of(0, 0)));
		} else {
			model.addAttribute("emptySpaces", IntStream.range(0, rows).boxed()
					.flatMap(row -> IntStream.range(0, columns).mapToObj(col -> Pair.of(col, row)))
					.filter(pos -> !positions.contains(pos))
					.toList());
		}
	}
}
