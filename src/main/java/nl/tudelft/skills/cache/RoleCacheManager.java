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
package nl.tudelft.skills.cache;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.Id;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;

@Component
@RequestScope
public class RoleCacheManager extends CoreCacheManager<Id, RoleDetailsDTO> {

	@Autowired
	private RoleControllerApi api;

	@Autowired
	private PersonCacheManager pCache;

	@Override
	protected List<RoleDetailsDTO> fetch(List<Id> ids) {
		return api.getRolesById(
				ids.stream().map(Id::getEditionId).collect(Collectors.toSet()),
				ids.stream().map(Id::getPersonId).collect(Collectors.toSet()))
				.collectList().block();
	}

	@Override
	protected Id getId(RoleDetailsDTO dto) {
		return dto.getId();
	}

	@Override
	protected void registerAdditionally(RoleDetailsDTO dto) {
		pCache.register(dto.getPerson());
	}

}
