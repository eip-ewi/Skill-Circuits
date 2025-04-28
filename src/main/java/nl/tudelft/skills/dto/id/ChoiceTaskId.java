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
package nl.tudelft.skills.dto.id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nl.tudelft.librador.dto.id.IdDTO;
import nl.tudelft.skills.model.ChoiceTask;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.repository.ChoiceTaskRepository;
import nl.tudelft.skills.repository.RegularTaskRepository;
import org.springframework.data.repository.CrudRepository;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChoiceTaskId extends IdDTO<ChoiceTask, Long> {

	public ChoiceTaskId(Long id) {
		super(id);
	}

	@Override
	public Class<? extends CrudRepository<ChoiceTask, Long>> repositoryClass() {
		return ChoiceTaskRepository.class;
	}

	@Override
	public Class<? extends ChoiceTask> targetClass() {
		return ChoiceTask.class;
	}

}
