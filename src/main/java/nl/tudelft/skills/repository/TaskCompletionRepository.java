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
package nl.tudelft.skills.repository;

import java.util.Optional;
import java.util.Set;

import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.model.Task;
import nl.tudelft.skills.model.TaskInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import nl.tudelft.skills.model.TaskCompletion;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

	default TaskCompletion findByIdOrThrow(Long id) {
		return findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("TaskCompletion was not found: " + id));
	}

    @Query("""
           select completion from TaskCompletion completion
           where completion.person.id = :#{#person.id}
           order by completion.timestamp desc limit 1
           """)
    Optional<TaskCompletion> findLastTaskCompletedFor(@Param("person") SCPerson person);

    @Query("""
           select case when count(completion) > 0 then true else false end
           from TaskCompletion completion
           where completion.task.id = :#{#task.id}
           and completion.person.id = :#{#person.id}
           """)
    boolean hasCompleted(@Param("person") SCPerson person, @Param("task") TaskInfo task);

    @Query("""
           select completion.task.id from TaskCompletion completion
           where completion.person.id = :#{#person.id} 
           """)
    Set<Long> findAllCompletedTaskIdsForPerson(@Param("person") SCPerson person);

    void deleteByPersonAndTask(SCPerson person, TaskInfo task);

	Optional<TaskCompletion> getFirstByPersonIdAndTimestampNotNullOrderByTimestampDesc(Long id);

	Set<TaskCompletion> getByPersonId(Long id);
}
