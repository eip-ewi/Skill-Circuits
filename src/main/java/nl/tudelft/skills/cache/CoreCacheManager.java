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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.google.common.collect.Sets;

/*
 * Stolen from Submit. Stolen from Queue by transitive property.
 */
public abstract class CoreCacheManager<ID, DTO> {
	protected final Map<ID, DTO> cache = new ConcurrentHashMap<>();
	protected final Set<ID> empties = new HashSet<>();

	/**
	 * Fetches the objects currently missing from the cache. These objects are fetched through a list of their
	 * IDs.
	 *
	 * @param  ids The list of IDs of objects to fetch.
	 * @return     The list of fetched objects.
	 */
	protected abstract List<DTO> fetch(List<ID> ids);

	/**
	 * Gets the ID of a particular instance of the data object.
	 *
	 * @param  dto The DTO data object that will be cached.
	 * @return     The ID of the DTO object.
	 */
	protected abstract ID getId(DTO dto);

	/**
	 * @return The maximum number of items to be fetched from Labracore in one go.
	 */
	protected int batchSize() {
		return 128;
	}

	/**
	 * Used to manually check the status of the cache and invalidate the cache if necessary.
	 */
	protected synchronized void validateCache() {
	}

	/**
	 * Gets a list of DTOs from a list of ids. The list of ids gets broken up into already cached items and to
	 * be fetched items. The to be fetched are then fetched and the already cached are just fetched from
	 * cache.
	 *
	 * @param  ids The ids to find matching DTOs for.
	 * @return     The list of DTOs found to be matching.
	 */
	public List<DTO> get(List<ID> ids) {
		validateCache();

		List<ID> misses = ids.stream()
				.filter(id -> !cache.containsKey(id) && !empties.contains(id))
				.collect(Collectors.toList());
		if (!misses.isEmpty()) {
			forEachBatch(misses, batch -> registerImpl(new HashSet<>(batch), fetch(batch)));
		}

		return ids.stream()
				.map(cache::get)
				.collect(Collectors.toList());
	}

	/**
	 * Gets a list of DTOs from a stream of IDs. This method is mostly used as a convenience method wrapping
	 * {@link #get(List)}.
	 *
	 * @param  ids The stream of ids to be collected and requested from the cache.
	 * @return     The list of DTOs with the given IDs.
	 */
	public List<DTO> get(Stream<ID> ids) {
		return get(ids.collect(Collectors.toList()));
	}

	/**
	 * Gets a single DTO from its id. If the DTO is already cached, no external lookup is done.
	 *
	 * @param  id The id of the DTO to lookup.
	 * @return    The requested DTO if it exists, otherwise nothing.
	 */
	public Optional<DTO> get(ID id) {
		if (id == null) {
			return Optional.empty();
		}
		return Optional.of(get(List.of(id)))
				.filter(l -> !l.isEmpty())
				.map(l -> l.get(0));
	}

	/**
	 * Gets the DTO with the given id or throws an exception with status code 404 if none such DTO could be
	 * found.
	 *
	 * @param  id The id of the DTO to find.
	 * @return    The found DTO with the given id.
	 */
	public DTO getOrThrow(ID id) {
		var l = get(List.of(id));
		if (l == null || l.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find DTO by id " + id);
		}
		return l.get(0);
	}

	/**
	 * Registers an entry into the cache after validating the cache first.
	 *
	 * @param dto The DTO that is to be registered.
	 */
	public void register(DTO dto) {
		validateCache();
		registerImpl(dto);
	}

	/**
	 * Registers a list of entries into the cache after validating the cache first.
	 *
	 * @param dtos The DTOs that are to be registered.
	 */
	public void register(List<DTO> dtos) {
		validateCache();
		registerImpl(dtos);
	}

	/**
	 * Implementation of the register method. Registers an entry into the cache.
	 *
	 * @param dto The DTO that is to be registered.
	 */
	private void registerImpl(DTO dto) {
		cache.put(getId(dto), dto);
		registerAdditionally(dto);
	}

	/**
	 * Implementation of the register method. Registers a list of entries into the cache.
	 *
	 * @param dtos The DTOs that are to be registered.
	 */
	private void registerImpl(List<DTO> dtos) {
		dtos.forEach(this::registerImpl);
	}

	/**
	 * Implementation of the register method. Registers a list of entries into the cache and also registers
	 * the IDs that were requested but not returned as 'empty' IDs.
	 *
	 * @param requests The set of requested IDs.
	 * @param dtos     The list of DTOs returned from the request.
	 */
	private void registerImpl(Set<ID> requests, List<DTO> dtos) {
		dtos.forEach(this::registerImpl);
		empties.addAll(Sets.difference(requests,
				dtos.stream().map(this::getId).collect(Collectors.toSet())));
	}

	/**
	 * Performs additional registers in other caches if necessary.
	 *
	 * @param dto The DTO that is being registered.
	 */
	protected void registerAdditionally(DTO dto) {
	}

	/**
	 * Helper function for iterating over batches of items. The batch size is determined through the
	 * {@link #batchSize()} method in implementations of this class. Each created batch will be passed on to
	 * the consumer function f.
	 *
	 * @param ids The list of ids that are to be batched.
	 * @param f   The consumer of each batch.
	 */
	private void forEachBatch(List<ID> ids, Consumer<List<ID>> f) {
		List<ID> batch = new ArrayList<>();
		for (ID id : ids) {
			batch.add(id);

			if (batch.size() >= batchSize()) {
				f.accept(batch);
				batch.clear();
			}
		}

		if (!batch.isEmpty()) {
			f.accept(batch);
		}
	}

}
