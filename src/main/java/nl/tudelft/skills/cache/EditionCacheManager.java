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
package nl.tudelft.skills.cache;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;

@Component
@RequestScope
@AllArgsConstructor
public class EditionCacheManager extends CoreCacheManager<Long, EditionDetailsDTO> {

	private final EditionControllerApi api;

	@Override
	protected List<EditionDetailsDTO> fetch(List<Long> ids) {
		return api.getEditionsById(ids).collectList().block();
	}

	@Override
	protected Long getId(EditionDetailsDTO dto) {
		return dto.getId();
	}

}
