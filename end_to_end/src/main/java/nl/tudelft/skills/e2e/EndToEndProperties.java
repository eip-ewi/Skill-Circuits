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
package nl.tudelft.skills.e2e;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EndToEndProperties {

	private static final Properties properties = new Properties();
	private static final AtomicBoolean propertiesLoaded = new AtomicBoolean(false);

	public static String getOrNull(String key) {
		if (propertiesLoaded.compareAndSet(false, true)) {
			loadProperties();
		}
		String envKey = key.replaceAll("[-.]", "_").toUpperCase();
		if (System.getenv().containsKey(envKey)) {
			return System.getenv(envKey);
		}
		return properties.getProperty(key);
	}

	public static Optional<String> getOptional(String key) {
		return Optional.ofNullable(getOrNull(key));
	}

	public static String get(String key) {
		String value = getOrNull(key);
		if (value == null) {
			throw new IllegalArgumentException("Property with name '" + key + "' not found");
		}
		return value;
	}

	public static boolean getBoolean(String key) {
		return "true".equalsIgnoreCase(get(key));
	}

	public static double getDouble(String key) {
		return Double.parseDouble(get(key));
	}

	private static void loadProperties() {
		try {
			properties.load(EndToEndProperties.class.getClassLoader().getResourceAsStream("e2e.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
