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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.bookmark.HiddenSkillBookmarkListRepository;

@Service
@AllArgsConstructor
public class CopyService {

	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;
	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final SubmoduleRepository submoduleRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;
	private final RegularTaskRepository regularTaskRepository;
	private final ChoiceTaskRepository choiceTaskRepository;
	private final TaskInfoRepository taskInfoRepository;
	private final HiddenSkillBookmarkListRepository hiddenSkillBookmarkListRepository;

	@Transactional
	public void copyEdition(SCEdition original, SCEdition copy) {
		moduleRepository.deleteAll(copy.getModules());
		checkpointRepository.deleteAll(copy.getCheckpoints());
		pathRepository.deleteAll(copy.getPaths());

		CopyInfo copyInfo = new CopyInfo();

		copy.setVisible(original.isVisible());
		copy.setCheckpoints(original.getCheckpoints().stream()
				.map(checkpoint -> copyCheckpoint(checkpoint, copy, copyInfo)).collect(Collectors.toSet()));
		copy.setPaths(original.getPaths().stream().map(path -> copyPath(path, copy, copyInfo))
				.collect(Collectors.toList()));
		copy.setModules(original.getModules().stream().map(module -> copyModule(module, copy, copyInfo))
				.collect(Collectors.toSet()));
        copyExternalSkills(copyInfo);
		copyConnections(copyInfo);
		copyHiddenSkillRequirements(copyInfo);

		editionRepository.save(copy);
	}

	private Checkpoint copyCheckpoint(Checkpoint original, SCEdition toEdition, CopyInfo copyInfo) {
		Checkpoint copy = Checkpoint.builder()
				.name(original.getName())
				.deadline(original.getDeadline())
				.edition(toEdition)
				.build();
		copy = checkpointRepository.save(copy);
		copyInfo.setCheckpointCopy(original, copy);
		return copy;
	}

	private Path copyPath(Path original, SCEdition toEdition, CopyInfo copyInfo) {
		Path copy = Path.builder()
				.name(original.getName())
				.idx(original.getIdx())
				.description(original.getDescription())
				.edition(toEdition)
				.build();
		copy = pathRepository.save(copy);
		copyInfo.setPathCopy(original, copy);
		return copy;
	}

	private SCModule copyModule(SCModule original, SCEdition toEdition, CopyInfo copyInfo) {
		SCModule copy = SCModule.builder()
				.name(original.getName())
				.edition(toEdition)
				.build();
		final SCModule savedCopy = moduleRepository.save(copy);
        copyInfo.setModuleCopy(original, savedCopy);
		toEdition.getModules().add(savedCopy);
		savedCopy.setSubmodules(original.getSubmodules().stream()
				.map(submodule -> copySubmodule(submodule, savedCopy, copyInfo)).collect(Collectors.toSet()));
		return savedCopy;
	}

    private void copyExternalSkills(CopyInfo copyInfo) {
        copyInfo.getCopiedModules().forEach((original, copy) -> {
            copy.setExternalSkills(original.getExternalSkills().stream()
                    .map(externalSkill -> copyExternalSkill(externalSkill, copy, copyInfo))
                    .collect(Collectors.toSet()));
        });
    }

	private Submodule copySubmodule(Submodule original, SCModule toModule, CopyInfo copyInfo) {
		Submodule copy = Submodule.builder()
				.name(original.getName())
				.column(original.getColumn())
				.module(toModule)
				.build();
		final Submodule savedCopy = submoduleRepository.save(copy);
		toModule.getSubmodules().add(savedCopy);
		savedCopy.setSkills(original.getSkills().stream().map(skill -> copySkill(skill, savedCopy, copyInfo))
				.collect(Collectors.toSet()));
		return savedCopy;
	}

	private Skill copySkill(Skill original, Submodule toSubmodule, CopyInfo copyInfo) {
		Skill copy = Skill.builder()
				.name(original.getName())
				.column(original.getColumn())
				.essential(original.isEssential())
				.hidden(original.isHidden())
				.submodule(toSubmodule)
				.checkpoint(copyInfo.getCopy(original.getCheckpoint()))
				.previousEditionSkill(original)
				.build();
		final Skill savedCopy = skillRepository.save(copy);
		copyInfo.setSkillCopy(original, savedCopy);
		savedCopy.setTasks(original.getTasks().stream().map(task -> copyTask(task, savedCopy, copyInfo))
				.collect(Collectors.toList()));
		return savedCopy;
	}

	private Task copyTask(Task original, Skill toSkill, CopyInfo copyInfo) {
		Task copy = switch (original) {
			case RegularTask regularTask -> copyTask(regularTask, toSkill, copyInfo);
			case ChoiceTask choiceTask -> copyTask(choiceTask, toSkill, copyInfo);
			default -> throw new IllegalArgumentException(); // Unreachable
		};
		copyInfo.setTaskCopy(original, copy);
		copy.setPaths(original.getPaths().stream().map(copyInfo::getCopy).collect(Collectors.toSet()));
		return copy;
	}

	private RegularTask copyTask(RegularTask original, Skill toSkill, CopyInfo copyInfo) {
		RegularTask copy = RegularTask.builder()
				.idx(original.getIdx())
				.skill(toSkill)
				.build();
		copyTaskInfo(original.getTaskInfo(), copy, copyInfo);
		copy = regularTaskRepository.save(copy);
		return copy;
	}

	private ChoiceTask copyTask(ChoiceTask original, Skill toSkill, CopyInfo copyInfo) {
		ChoiceTask copy = ChoiceTask.builder()
				.name(original.getName())
				.minTasks(original.getMinTasks())
				.idx(original.getIdx())
				.skill(toSkill)
				.build();
		final ChoiceTask savedCopy = choiceTaskRepository.save(copy);
		savedCopy.setTasks(original.getTasks().stream().map(task -> copyTaskInfo(task, savedCopy, copyInfo))
				.collect(Collectors.toList()));
		return savedCopy;
	}

	private TaskInfo copyTaskInfo(TaskInfo original, Task toTask, CopyInfo copyInfo) {
		TaskInfo copy = TaskInfo.builder()
				.name(original.getName())
				.type(original.getType())
				.time(original.getTime())
				.link(original.getLink())
				.build();
		switch (toTask) {
			case RegularTask regularTask -> {
				copy.setTask(regularTask);
				regularTask.setTaskInfo(copy);
			}
			case ChoiceTask choiceTask -> copy.setChoiceTask(choiceTask);
			default -> {
			} // Unreachable
		}
		copy = taskInfoRepository.save(copy);
		copyInfo.setTaskInfoCopy(original, copy);
		return copy;
	}

	private ExternalSkill copyExternalSkill(ExternalSkill original, SCModule toModule, CopyInfo copyInfo) {
		ExternalSkill copy = ExternalSkill.builder()
				.column(original.getColumn())
				.essential(original.isEssential())
				.module(toModule)
				.build();
		if (copyInfo.isSkillCopied(original.getSkill())) {
			copy.setSkill((Skill) copyInfo.getCopy(original.getSkill()));
		} else {
			copy.setSkill(original.getSkill());
		}
		copy = externalSkillRepository.save(copy);
		copyInfo.setSkillCopy(original, copy);
		copy.getSkill().getExternalSkills().add(copy);
		toModule.getExternalSkills().add(copy);
		return copy;
	}

	private void copyConnections(CopyInfo copyInfo) {
		copyInfo.getCopiedSkills().forEach((original, copy) -> {
			copy.setParents(
					original.getParents().stream().map(copyInfo::getCopy).collect(Collectors.toSet()));
			copy.setChildren(
					original.getChildren().stream().map(copyInfo::getCopy).collect(Collectors.toSet()));
		});
	}

	private void copyHiddenSkillRequirements(CopyInfo copyInfo) {
		copyInfo.getCopiedSkills().forEach((original, copy) -> {
			if (original instanceof Skill originalSkill && originalSkill.getRequirements() != null) {
				((Skill) copy).setRequirements(
						copyHiddenSkillBookmarkList(originalSkill.getRequirements(), (Skill) copy, copyInfo));
			}
		});
	}

	private HiddenSkillBookmarkList copyHiddenSkillBookmarkList(HiddenSkillBookmarkList original,
			Skill toSkill, CopyInfo copyInfo) {
		HiddenSkillBookmarkList copy = HiddenSkillBookmarkList.builder()
				.skill(toSkill)
				.build();
		copy = hiddenSkillBookmarkListRepository.save(copy);
		copy.setSkills(original.getSkills().stream().map(skill -> (Skill) copyInfo.getCopy(skill))
				.collect(Collectors.toSet()));
		copy.setTasks(original.getTasks().stream().map(copyInfo::getCopy).collect(Collectors.toSet()));
		copy.setChoiceTasks(original.getChoiceTasks().stream()
				.map(task -> (ChoiceTask) copyInfo.getCopy(task)).collect(Collectors.toSet()));
		return copy;
	}

	private static class CopyInfo {

        private final Map<SCModule, SCModule> moduleMap = new HashMap<>();
		private final Map<Checkpoint, Checkpoint> checkpointMap = new HashMap<>();
		private final Map<Path, Path> pathMap = new HashMap<>();
		private final Map<AbstractSkill, AbstractSkill> skillMap = new HashMap<>();
		private final Map<Task, Task> taskMap = new HashMap<>();
		private final Map<TaskInfo, TaskInfo> taskInfoMap = new HashMap<>();

        public void setModuleCopy(SCModule original, SCModule copy) {
            moduleMap.put(original, copy);
        }

        public Map<SCModule, SCModule> getCopiedModules() {
            return moduleMap;
        }

		public void setCheckpointCopy(Checkpoint original, Checkpoint copy) {
			checkpointMap.put(original, copy);
		}

		public Checkpoint getCopy(Checkpoint original) {
			return checkpointMap.get(original);
		}

		public void setPathCopy(Path original, Path copy) {
			pathMap.put(original, copy);
		}

		public Path getCopy(Path original) {
			return pathMap.get(original);
		}

		public void setSkillCopy(AbstractSkill original, AbstractSkill copy) {
			skillMap.put(original, copy);
		}

		public boolean isSkillCopied(AbstractSkill original) {
			return skillMap.containsKey(original);
		}

		public AbstractSkill getCopy(AbstractSkill original) {
			return skillMap.get(original);
		}

		public Map<AbstractSkill, AbstractSkill> getCopiedSkills() {
			return skillMap;
		}

		public void setTaskCopy(Task original, Task copy) {
			taskMap.put(original, copy);
		}

		public Task getCopy(Task original) {
			return taskMap.get(original);
		}

		public void setTaskInfoCopy(TaskInfo original, TaskInfo copy) {
			taskInfoMap.put(original, copy);
		}

		public TaskInfo getCopy(TaskInfo original) {
			return taskInfoMap.get(original);
		}

	}

}
