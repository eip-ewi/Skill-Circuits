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

public record User(String username, String password, String displayName) {

	public static final User cseteacher1 = new User("cseteacher1", "cseteacher1", "CSE Teacher 1");

	public static final User csestudent1 = new User("csestudent1", "csestudent1", "CSE Student 1");
	public static final User csestudent2 = new User("csestudent2", "csestudent2", "CSE Student 2");

}
