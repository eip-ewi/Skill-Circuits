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
package nl.tudelft.skills.e2e.docker;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public final class SkillCircuitsContainer {

	private static final Logger logger = LoggerFactory.getLogger(SkillCircuitsContainer.class);

	private static final DockerComposeContainer<?> compose = new DockerComposeContainer<>(
			new File("compose.yml"))
			.withExposedService("database", 3306,
					Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(2)))
			.withExposedService("skills", 8084,
					Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(3)))
			.withLogConsumer("skills", frame -> {
				logger.info(frame.getUtf8StringWithoutLineEnding());
			});
	private static final AtomicBoolean started = new AtomicBoolean(false);

	public static void create() {
		if (started.compareAndSet(false, true)) {
			compose.start();
		}
	}

}
