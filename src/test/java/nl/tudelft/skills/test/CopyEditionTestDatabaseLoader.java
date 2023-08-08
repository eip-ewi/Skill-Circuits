/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
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
package nl.tudelft.skills.test;

import java.time.LocalDateTime;
import java.util.Set;

import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CopyEditionTestDatabaseLoader {

	private final Long USED_EDITION_ID = 69L;

	@Autowired
	private EditionRepository editionRepository;
	@Autowired
	private ModuleRepository moduleRepository;
	@Autowired
	private SubmoduleRepository submoduleRepository;
	@Autowired
	private SkillRepository skillRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private PathRepository pathRepository;
	@Autowired
	private CheckpointRepository checkpointRepository;
	@Autowired
	private ExternalSkillRepository externalSkillRepository;

	private SCEdition from;
	private SCEdition to;

	private SCModule moduleFromA;
	private SCModule moduleFromB;
	private SCModule moduleToA;
	private SCModule moduleToB;

	private Checkpoint checkpointFromA;
	private Checkpoint checkpointFromB;
	private Checkpoint checkpointToA;
	private Checkpoint checkpointToB;

	private Path pathFromA;
	private Path pathFromB;
	private Path pathToA;
	private Path pathToB;

	private Submodule submoduleFromA;
	private Submodule submoduleFromB;
	private Submodule submoduleToA;
	private Submodule submoduleToB;

	private ExternalSkill extSkillFromA;
	private ExternalSkill extSkillFromB;
	private ExternalSkill extSkillToA;
	private ExternalSkill extSkillToB;

	private Skill skillFromA;
	private Skill skillFromB;
	private Skill skillToA;
	private Skill skillToB;

	private Task taskFromA;
	private Task taskFromB;

	public void initEditionFrom(Skill linkTo) {
		initEditions();
		initCheckpoints(true);
		initPaths(true);
		initModules(true);
		initSubmodules(true);
		initSkills(true);
		initExternalSkillSameEdition(true);
		initExternalSkillOtherEdition(true, linkTo);
		initParentChild(true);
		initTasks();
	}

	public void initEditions() {
		from = editionRepository.save(SCEdition.builder().id(USED_EDITION_ID + 1).build());
		to = editionRepository.save(SCEdition.builder().id(USED_EDITION_ID + 2).build());
	}

	public void initCheckpoints(boolean editionFrom) {
		LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);

		if (editionFrom) {
			checkpointFromA = checkpointRepository.save(Checkpoint.builder()
					.edition(from).name("Checkpoint A").deadline(localDateTime).build());
			from.getCheckpoints().add(checkpointFromA);
			checkpointFromB = checkpointRepository.save(Checkpoint.builder()
					.edition(from).name("Checkpoint B").deadline(localDateTime.plusDays(5)).build());
			from.getCheckpoints().add(checkpointFromB);
		} else {
			checkpointToA = checkpointRepository.save(Checkpoint.builder()
					.edition(to).name("Checkpoint A").deadline(localDateTime).build());
			to.getCheckpoints().add(checkpointToA);
			checkpointToB = checkpointRepository.save(Checkpoint.builder()
					.edition(to).name("Checkpoint B").deadline(localDateTime.plusDays(5)).build());
			to.getCheckpoints().add(checkpointToB);
		}
	}

	public void initPaths(boolean editionFrom) {
		if (editionFrom) {
			pathFromA = pathRepository.save(Path.builder().name("Path A").edition(from).build());
			from.getPaths().add(pathFromA);
			pathFromB = pathRepository.save(Path.builder().name("Path B").edition(from).build());
			from.getPaths().add(pathFromB);
		} else {
			pathToA = pathRepository.save(Path.builder().name("Path A").edition(to).build());
			to.getPaths().add(pathToA);
			pathToB = pathRepository.save(Path.builder().name("Path B").edition(to).build());
			to.getPaths().add(pathToB);
		}
	}

	public void initModules(boolean editionFrom) {
		if (editionFrom) {
			moduleFromA = moduleRepository.save(SCModule.builder().name("Module A").edition(from).build());
			from.getModules().add(moduleFromA);
			moduleFromB = moduleRepository.save(SCModule.builder().name("Module B").edition(from).build());
			from.getModules().add(moduleFromB);
		} else {
			moduleToA = moduleRepository.save(SCModule.builder().name("Module A").edition(to).build());
			to.getModules().add(moduleToA);
			moduleToB = moduleRepository.save(SCModule.builder().name("Module B").edition(to).build());
			to.getModules().add(moduleToB);
		}
	}

	public void initSubmodules(boolean editionFrom) {
		if (editionFrom) {
			submoduleFromA = Submodule.builder().module(moduleFromA).name("Submodule A").column(0).row(0)
					.build();
			submoduleFromA = submoduleRepository.save(submoduleFromA);
			moduleFromA.getSubmodules().add(submoduleFromA);
			submoduleFromB = Submodule.builder().module(moduleFromA).name("Submodule B").column(0).row(0)
					.build();
			submoduleFromB = submoduleRepository.save(submoduleFromB);
			moduleFromA.getSubmodules().add(submoduleFromB);
		} else {
			submoduleToA = Submodule.builder().module(moduleToA).name("Submodule A").column(0).row(0)
					.build();
			submoduleToA = submoduleRepository.save(submoduleToA);
			moduleToA.getSubmodules().add(submoduleToA);
			submoduleToB = Submodule.builder().module(moduleToA).name("Submodule B").column(0).row(0)
					.build();
			submoduleToB = submoduleRepository.save(submoduleToB);
			moduleToA.getSubmodules().add(submoduleToB);
		}
	}

	public void initSkills(boolean editionFrom) {
		if (editionFrom) {
			skillFromA = Skill.builder().submodule(submoduleFromA).checkpoint(checkpointFromA).name("Skill A")
					.column(0).row(0).build();
			skillFromA = skillRepository.save(skillFromA);
			submoduleFromA.getSkills().add(skillFromA);
			checkpointFromA.getSkills().add(skillFromA);
			skillFromB = Skill.builder().submodule(submoduleFromA).checkpoint(checkpointFromA).name("Skill B")
					.column(0).row(0).build();
			skillFromB = skillRepository.save(skillFromB);
			submoduleFromA.getSkills().add(skillFromB);
			checkpointFromA.getSkills().add(skillFromB);
		} else {
			skillToA = Skill.builder().submodule(submoduleToA).checkpoint(checkpointToA).name("Skill A")
					.column(0).row(0).build();
			skillToA = skillRepository.save(skillToA);
			submoduleToA.getSkills().add(skillToA);
			checkpointToA.getSkills().add(skillToA);
			skillToB = Skill.builder().submodule(submoduleToA).checkpoint(checkpointToA).name("Skill B")
					.column(0).row(0).build();
			skillToB = skillRepository.save(skillToB);
			submoduleToA.getSkills().add(skillToB);
			checkpointToA.getSkills().add(skillToB);
		}
	}

	public void initExternalSkillSameEdition(boolean editionFrom) {
		if (editionFrom) {
			extSkillFromA = ExternalSkill.builder().skill(skillFromA).module(moduleFromB).column(0)
					.row(0).build();
			extSkillFromA = externalSkillRepository.save(extSkillFromA);
			skillFromA.getExternalSkills().add(extSkillFromA);
			moduleFromB.getExternalSkills().add(extSkillFromA);
		} else {
			extSkillToA = ExternalSkill.builder().skill(skillToA).module(moduleToB).column(0)
					.row(0).build();
			extSkillToA = externalSkillRepository.save(extSkillToA);
			skillToA.getExternalSkills().add(extSkillToA);
			moduleToB.getExternalSkills().add(extSkillToA);
		}
	}

	public void initExternalSkillOtherEdition(boolean editionFrom, Skill linkTo) {
		if (editionFrom) {
			extSkillFromB = ExternalSkill.builder().skill(linkTo).module(moduleFromA)
					.column(0).row(0).build();
			extSkillFromB = externalSkillRepository.save(extSkillFromB);
			linkTo.getExternalSkills().add(extSkillFromB);
			moduleFromA.getExternalSkills().add(extSkillFromB);
		} else {
			extSkillToB = ExternalSkill.builder().skill(linkTo).module(moduleToA)
					.column(0).row(0).build();
			extSkillToB = externalSkillRepository.save(extSkillToB);
			linkTo.getExternalSkills().add(extSkillToB);
			moduleToA.getExternalSkills().add(extSkillToB);
		}
	}

	public void initParentChild(boolean editionFrom) {
		if (editionFrom) {
			extSkillFromB.getParents().add(skillFromB);
			skillFromB.getChildren().add(extSkillFromB);
		} else {
			extSkillToB.getParents().add(skillToB);
			skillToB.getChildren().add(extSkillToB);
		}
	}

	public void initTasks() {
		taskFromA = Task.builder().skill(skillFromA).name("Task A").paths(Set.of(pathFromA)).build();
		taskFromA = taskRepository.save(taskFromA);
		pathFromA.getTasks().add(taskFromA);
		skillFromA.getTasks().add(taskFromA);
		taskFromB = Task.builder().skill(skillFromB).name("Task B").build();
		taskFromB = taskRepository.save(taskFromB);
		skillFromB.getTasks().add(taskFromB);
		skillFromB.getRequiredTasks().add(taskFromA);
		taskFromA.getRequiredFor().add(skillFromB);
	}

	public SCEdition getFrom() {
		return from;
	}

	public SCEdition getTo() {
		return to;
	}

	public SCModule getModuleFromA() {
		return moduleFromA;
	}

	public SCModule getModuleFromB() {
		return moduleFromB;
	}

	public SCModule getModuleToA() {
		return moduleToA;
	}

	public SCModule getModuleToB() {
		return moduleToB;
	}

	public Checkpoint getCheckpointFromA() {
		return checkpointFromA;
	}

	public Checkpoint getCheckpointFromB() {
		return checkpointFromB;
	}

	public Checkpoint getCheckpointToA() {
		return checkpointToA;
	}

	public Checkpoint getCheckpointToB() {
		return checkpointToB;
	}

	public Path getPathFromA() {
		return pathFromA;
	}

	public Path getPathFromB() {
		return pathFromB;
	}

	public Path getPathToA() {
		return pathToA;
	}

	public Path getPathToB() {
		return pathToB;
	}

	public Submodule getSubmoduleFromA() {
		return submoduleFromA;
	}

	public Submodule getSubmoduleFromB() {
		return submoduleFromB;
	}

	public Submodule getSubmoduleToA() {
		return submoduleToA;
	}

	public Submodule getSubmoduleToB() {
		return submoduleToB;
	}

	public ExternalSkill getExtSkillFromA() {
		return extSkillFromA;
	}

	public ExternalSkill getExtSkillFromB() {
		return extSkillFromB;
	}

	public ExternalSkill getExtSkillToA() {
		return extSkillToA;
	}

	public ExternalSkill getExtSkillToB() {
		return extSkillToB;
	}

	public Skill getSkillFromA() {
		return skillFromA;
	}

	public Skill getSkillFromB() {
		return skillFromB;
	}

	public Skill getSkillToA() {
		return skillToA;
	}

	public Skill getSkillToB() {
		return skillToB;
	}

	public Task getTaskFromA() {
		return taskFromA;
	}

	public Task getTaskFromB() {
		return taskFromB;
	}

}
