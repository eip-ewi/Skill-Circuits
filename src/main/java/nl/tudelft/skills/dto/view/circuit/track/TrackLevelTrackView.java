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
package nl.tudelft.skills.dto.view.circuit.track;

import java.util.List;

import nl.tudelft.skills.dto.view.circuit.BlockView;
import nl.tudelft.skills.dto.view.circuit.CircuitView;
import nl.tudelft.skills.dto.view.circuit.GroupView;
import nl.tudelft.skills.dto.view.circuit.ItemView;

public record TrackLevelTrackView(
		long id,
		String name,
		List<Edition> groups) implements CircuitView {
	@Override
	public String getCircuitType() {
		return "track";
	}

	public record Edition(long id, String name, List<Module> blocks) implements GroupView {
		@Override
		public String getGroupType() {
			return "edition";
		}
	}

	public record Module(
			long id,
			String name,
			Integer column,
			List<Long> parents,
			List<Long> children,
			List<Submodule> items) implements BlockView {
		@Override
		public String getBlockType() {
			return "module";
		}
	}

	public record Submodule(long id, String name, boolean completed, boolean locked)
			implements
				ItemView {
		@Override
		public String getItemType() {
			return "submodule";
		}
	}
}
