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
package nl.tudelft.skills.service;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.dto.view.circuit.module.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.TaskCompletionRepository;

@Service
@AllArgsConstructor
public class ModuleCircuitService {

	private final TaskCompletionRepository taskCompletionRepository;

	public ModuleLevelModuleView getModuleCircuit(SCModule module, SCPerson person) {
		Set<Long> completedTaskIds = taskCompletionRepository.findAllCompletedTaskIdsForPerson(person);
		return new ModuleLevelModuleView(
				module.getId(),
				module.getName(),
				module.getEdition().getId(),
				module.getExternalSkills().stream().map(skill -> convertToSkillView(skill, completedTaskIds))
						.toList(),
				module.getSubmodules().stream()
						.map(submodule -> convertToSubmoduleView(submodule, completedTaskIds)).toList());
	}

	private ModuleLevelSubmoduleView convertToSubmoduleView(Submodule submodule, Set<Long> completedTaskIds) {
		return new ModuleLevelSubmoduleView(
				submodule.getId(),
				submodule.getName(),
				submodule.getSkills().stream().map(skill -> convertToSkillView(skill, completedTaskIds))
						.toList());
	}

	public ModuleLevelSkillView convertToSkillView(AbstractSkill abstractSkill, SCPerson person) {
		return convertToSkillView(abstractSkill,
				taskCompletionRepository.findAllCompletedTaskIdsForPerson(person));
	}

	public ModuleLevelSkillView convertToSkillView(AbstractSkill abstractSkill, Set<Long> completedTaskIds) {
		Skill skill = switch (abstractSkill) {
			case ExternalSkill externalSkill -> externalSkill.getSkill();
			case Skill s -> s;
			default -> throw new IllegalStateException(); // Unreachable
		};
		return new ModuleLevelSkillView(
				abstractSkill.getId(),
				skill.getName(),
				abstractSkill.getColumn(),
				abstractSkill.isEssential(),
				abstractSkill instanceof Skill s && s.isHidden(),
				abstractSkill instanceof ExternalSkill,
				getCheckpointIdInEdition(abstractSkill),
				abstractSkill.getParents().stream().map(AbstractSkill::getId).toList(),
				abstractSkill.getChildren().stream().map(AbstractSkill::getId).toList(),
				skill.getTasks().stream()
						.map(task -> convertToTaskView(task, completedTaskIds))
						.toList());
	}

	public Long getCheckpointIdInEdition(AbstractSkill abstractSkill) {
		return switch (abstractSkill) {
			case Skill skill -> skill.getCheckpoint() == null ? null : skill.getCheckpoint().getId();
			case ExternalSkill externalSkill ->
				findClosestNextCheckpoint(externalSkill, externalSkill.getModule().getEdition().getId())
						.map(Checkpoint::getId).orElse(null);
			default -> null; // Unreachable
		};
	}

	public Optional<Checkpoint> findClosestNextCheckpoint(AbstractSkill abstractSkill, Long editionId) {
		if (abstractSkill instanceof ExternalSkill externalSkill) {
			return findClosestNextCheckpoint(externalSkill.getSkill(), editionId);
		}
		Skill skill = (Skill) abstractSkill;
		if (skill.getCheckpoint() != null
				&& Objects.equals(skill.getCheckpoint().getEdition().getId(), editionId)) {
			return Optional.of(skill.getCheckpoint());
		}
		return skill.getChildren().stream().map(child -> findClosestNextCheckpoint(child, editionId))
				.filter(Optional::isPresent).map(Optional::get)
				.min(Comparator.comparing(Checkpoint::getDeadline));
	}

	public ModuleLevelTaskView convertToTaskView(Task task, SCPerson person) {
		return convertToTaskView(task, taskCompletionRepository.findAllCompletedTaskIdsForPerson(person));
	}

	public ModuleLevelTaskView convertToTaskView(Task task, Set<Long> completedTaskIds) {
		return switch (task) {
			case RegularTask regularTask -> convertToTaskView(regularTask, completedTaskIds);
			case ChoiceTask choiceTask -> convertToTaskView(choiceTask, completedTaskIds);
			default -> null; // Unreachable
		};
	}

	public ModuleLevelTaskView convertToTaskView(RegularTask task, Set<Long> completedTaskIds) {
		return new ModuleLevelTaskView.Regular(
				task.getId(),
				task.getTaskInfo().getId(),
				task.getName(),
				task.getType(),
				task.getTime(),
				task.getLink(),
				completedTaskIds.contains(task.getTaskInfo().getId()),
				task.getPaths().stream().map(Path::getId).toList());
	}

	public ModuleLevelTaskView convertToTaskView(ChoiceTask task, Set<Long> completedTaskIds) {
		return new ModuleLevelTaskView.Choice(
				task.getId(),
				task.getName(),
				task.getMinTasks(),
				task.getTasks().stream().map(info -> convertToChoiceView(info, completedTaskIds)).toList(),
				task.getPaths().stream().map(Path::getId).toList());
	}

	public ModuleLevelTaskView.ChoiceTaskChoiceView convertToChoiceView(TaskInfo info, SCPerson person) {
		return convertToChoiceView(info, taskCompletionRepository.findAllCompletedTaskIdsForPerson(person));
	}

	public ModuleLevelTaskView.ChoiceTaskChoiceView convertToChoiceView(TaskInfo info,
			Set<Long> completedTaskIds) {
		return new ModuleLevelTaskView.ChoiceTaskChoiceView(
				info.getId(),
				info.getName(),
				info.getType(),
				info.getTime(),
				info.getLink(),
				completedTaskIds.contains(info.getId()));
	}

}
