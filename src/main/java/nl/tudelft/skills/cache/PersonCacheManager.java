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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;

@Component
@ApplicationScope
public class PersonCacheManager extends TimedCacheManager<Long, PersonSummaryDTO> {

	private Map<String, Long> usernameCache = new ConcurrentHashMap<>();

	private ModelMapper mapper;

	private PersonControllerApi api;

	/**
	 * Creates a new person cache by setting the timeout on its timed cache implementation.
	 */
	@Autowired
	public PersonCacheManager(ModelMapper mapper, PersonControllerApi api, Environment env) {
		super(Long.parseLong(env.getProperty("skill-circuits.cache.person-timeout")) * 1000L);
		this.mapper = mapper;
		this.api = api;
	}

	public PersonSummaryDTO get(String username) {
		if (!usernameCache.containsKey(username)) {
			register(mapper.map(api.getPersonByUsername(username).block(), PersonSummaryDTO.class));
		}
		return getOrThrow(usernameCache.get(username));
	}

	@Override
	protected List<PersonSummaryDTO> fetch(List<Long> ids) {
		return api.getPeopleById(ids).collectList().block();
	}

	@Override
	protected Long getId(PersonSummaryDTO dto) {
		return dto.getId();
	}

	@Override
	protected void registerAdditionally(PersonSummaryDTO personSummaryDTO) {
		usernameCache.put(personSummaryDTO.getUsername(), personSummaryDTO.getId());
	}

	@Override
	protected synchronized void validateCache() {
		super.validateCache();
		if (cache.size() == 0) {
			usernameCache.clear();
		}
	}
}
