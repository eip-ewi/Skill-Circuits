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

/*
 * Stolen from Queue.
 */
public abstract class TimedCacheManager<ID, DTO> extends CoreCacheManager<ID, DTO> {
	/**
	 * The default timeout of the timed cache.
	 */
	public static final long DEFAULT_TIMEOUT = 60000L;

	/**
	 * The set timeout of the timed cache in milliseconds. This many milliseconds after the last cache
	 * invalidation, this cache is invalidated again.
	 */
	private final long timeout;

	/**
	 * The last time in milliseconds since epoch (system-time) that this cache was (in)validated.
	 * {@link #timeout} milliseconds after this timestamp, the cache is invalidated (again).
	 */
	private long lastValidation = System.currentTimeMillis();

	/**
	 * Constructs a new timed cache with the timeout set to {@link #DEFAULT_TIMEOUT}.
	 */
	public TimedCacheManager() {
		this.timeout = DEFAULT_TIMEOUT;
	}

	/**
	 * Constructs a new timed cache with the timeout set to the given timeout.
	 *
	 * @param timeout The timeout to set internally.
	 */
	public TimedCacheManager(long timeout) {
		this.timeout = timeout;
	}

	@Override
	protected synchronized void validateCache() {
		long now = System.currentTimeMillis();
		if (lastValidation + timeout < now) {
			cache.clear();
			empties.clear();
			lastValidation = now;
		}
	}
}
