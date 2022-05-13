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
package nl.tudelft.skills.dto.view;

import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.model.Inventory;
import nl.tudelft.skills.model.InventoryItem;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryViewDTO extends View<Inventory> {

	private Long id;
	private List<InventoryItem> inventoryItems;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InventoryViewDTO that = (InventoryViewDTO) o;
		return Objects.equals(inventoryItems, that.inventoryItems);
	}

	@Override
	public int hashCode() {
		return Objects.hash(inventoryItems);
	}
}
