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
package nl.tudelft.skills.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.edition.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;

@Service
@AllArgsConstructor
public class EditionService {

	private final EditionControllerApi editionApi;
	private final EditionRepository editionRepository;
	private final CircuitService circuitService;

	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final ModuleRepository moduleRepository;
	private final SubmoduleRepository submoduleRepository;
	private final AbstractSkillRepository abstractSkillRepository;
	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;

	/**
	 * Configures the model for the module circuit view.
	 *
	 * @param id      The id of the module
	 * @param model   The module to configure
	 * @param session The http session
	 */
	public void configureEditionModel(Long id, Model model, HttpSession session) {
		EditionLevelEditionViewDTO edition = getEditionView(id);

		Set<Pair<Integer, Integer>> positions = edition.getFilledPositions();
		int columns = positions.stream().mapToInt(Pair::getFirst).max().orElse(0) + 1;
		int rows = positions.stream().mapToInt(Pair::getSecond).max().orElse(0) + 1;
		Boolean studentMode = (Boolean) session.getAttribute("student-mode-" + id);

		Set<PathViewDTO> pathsInEdition = getPaths(edition.getId());

		model.addAttribute("level", "edition");
		model.addAttribute("edition", edition);
		circuitService.setCircuitAttributes(model, positions, columns, rows);

		model.addAttribute("emptyBlock", EditionLevelSubmoduleViewDTO.empty());
		model.addAttribute("emptyGroup", EditionLevelModuleViewDTO.empty());
		model.addAttribute("studentMode", studentMode != null && studentMode);

		model.addAttribute("paths", pathsInEdition);
	}

	/**
	 * Return EditionLevelEditionViewDto for edition with id, including edition name and course from Labrador
	 * db and SCModules.
	 *
	 * @param  id Edition id.
	 * @return    EditionViewDTO for edition with id.
	 */
	public EditionLevelEditionViewDTO getEditionView(Long id) {
		EditionDetailsDTO edition = editionApi.getEditionById(id).block();

		EditionLevelEditionViewDTO view = View.convert(getOrCreateSCEdition(id),
				EditionLevelEditionViewDTO.class);

		List<EditionLevelEditionSummaryDTO> olderEditions = editionApi
				.getAllEditionsByCourse(edition.getCourse().getId())
				.collectList().block().stream()
				.filter(dto -> dto.getStartDate().isBefore(edition.getStartDate()))
				.map(dto -> new EditionLevelEditionSummaryDTO(dto.getId(), dto.getName()))
				.collect(Collectors.toList());

		view.setName(edition.getName());
		view.setCourse(
				new EditionLevelCourseViewDTO(edition.getCourse().getId(), edition.getCourse().getName(),
						olderEditions));
		return view;
	}

	/**
	 * Returns a list of PathViewDTOs with all the paths in this edition.
	 *
	 * @param  editionId Edition id.
	 * @return           List of PathViewDTOs for edition with id.
	 */
	public Set<PathViewDTO> getPaths(Long editionId) {
		return editionRepository.findById(editionId).get()
				.getPaths().stream().map(p -> View.convert(p, PathViewDTO.class))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns the default path for an edition if it exists.
	 *
	 * @param  editionId The edition id
	 * @return           The default path of an edition if it exists, null otherwise.
	 */
	public Path getDefaultPath(Long editionId) {
		return editionRepository.findById(editionId).get().getDefaultPath();
	}

	/**
	 * Returns a SCEdition by edition id. If it doesn't exist, creates one.
	 *
	 * @param  id The id of the edition
	 * @return    The SCEdition with id.
	 */
	@Transactional
	public SCEdition getOrCreateSCEdition(Long id) {
		return editionRepository.findById(id)
				.orElseGet(() -> editionRepository.save(SCEdition.builder().id(id).build()));
	}

	/**
	 * Checks if an edition is empty (i.e., no modules, no checkpoints and no paths).
	 *
	 * @param  id The id of the edition to check.
	 * @return    If the given edition is empty.
	 */
	public boolean isEditionEmpty(Long id) {
		SCEdition edition = editionRepository.findByIdOrThrow(id);

		boolean noModules = edition.getModules().isEmpty();
		boolean noCheckpoints = edition.getCheckpoints().isEmpty();
		boolean noPaths = edition.getPaths().isEmpty();

		// These checks are sufficient, since submodules, skills etc. can
		// only exist if there is at least one module.
		return noModules && noCheckpoints && noPaths;
	}

	/**
	 * Copies one edition (i.e., modules, submodules etc.) to another.
	 *
	 * @param copyTo   The id of the edition to copy to.
	 * @param copyFrom The id of the edition to copy from.
	 */
	@Transactional
	public SCEdition copyEdition(Long copyTo, Long copyFrom) {
		// Check if the edition to copy to, is empty
		// This should already be ensured by the frontend in practice
		if (!isEditionEmpty(copyTo)) {
			return null;
		}

		SCEdition editionTo = editionRepository.findByIdOrThrow(copyTo);
		SCEdition editionFrom = editionRepository.findByIdOrThrow(copyFrom);

		// Copy checkpoints
		Map<Checkpoint, Checkpoint> checkpointMap = copyEditionCheckpoints(editionFrom, editionTo);

		// Copy paths
		Map<Path, Path> pathMap = copyEditionPaths(editionFrom, editionTo);
		editionTo.setDefaultPath(pathMap.get(editionFrom.getDefaultPath()));

		// Copy modules and submodules
		Map<SCModule, SCModule> moduleMap = copyEditionModules(editionFrom, editionTo);
		Map<Submodule, Submodule> submoduleMap = copyEditionSubmodules(moduleMap);

		// Copy (external) skills
		Map<Skill, Skill> skillMap = copyAndLinkEditionSkills(submoduleMap, checkpointMap);
		Map<ExternalSkill, ExternalSkill> externalSkillMap = copyAndLinkEditionExternalSkills(moduleMap,
				skillMap);
		Map<AbstractSkill, AbstractSkill> abstractSkillMap = new HashMap<>(externalSkillMap);
		abstractSkillMap.putAll(skillMap);
		linkParentsChildrenSkills(abstractSkillMap);

		// Copy tasks
		copyAndLinkEditionTasks(skillMap, pathMap);

		// TODO achievements are currently not copied.
		// Since there may be a lot of updates to the database, flush all databases for safety
		editionRepository.flush();
		pathRepository.flush();
		moduleRepository.flush();
		submoduleRepository.flush();
		abstractSkillRepository.flush();
		skillRepository.flush();
		taskRepository.flush();

		return editionTo;
	}

	/**
	 * Adds copies of checkpoints from the given edition (editionFrom) to the database, and adds them to the
	 * checkpoints of the edition to copy to (editionTo). Returns the map of checkpoints, from previous
	 * checkpoints in editionFrom to the new checkpoints in editionTo.
	 *
	 * @param  editionFrom The edition to copy from.
	 * @param  editionTo   The edition to copy to.
	 * @return             A map of checkpoints, from previous checkpoints in editionFrom to the new
	 *                     checkpoints in editionTo.
	 */
	Map<Checkpoint, Checkpoint> copyEditionCheckpoints(SCEdition editionFrom, SCEdition editionTo) {
		Map<Checkpoint, Checkpoint> checkpointsMap = new HashMap<>();

		editionFrom.getCheckpoints().forEach(c -> {
			Checkpoint checkpoint = checkpointRepository.save(
					Checkpoint.builder()
							.name(c.getName())
							.deadline(c.getDeadline())
							.edition(editionTo)
							.build());

			editionTo.getCheckpoints().add(checkpoint);
			checkpointsMap.put(c, checkpoint);
		});

		return checkpointsMap;
	}

	/**
	 * Adds copies of paths from the given edition (editionFrom) to the database, and adds them to the paths
	 * of the edition to copy to (editionTo). Returns the map of paths, from previous paths in editionFrom to
	 * the new paths in editionTo.
	 *
	 * @param  editionFrom The edition to copy from.
	 * @param  editionTo   The edition to copy to.
	 * @return             A map of paths, from previous paths in editionFrom to the new paths in editionTo.
	 */
	Map<Path, Path> copyEditionPaths(SCEdition editionFrom, SCEdition editionTo) {
		Map<Path, Path> pathMap = new HashMap<>();

		editionFrom.getPaths().forEach(p -> {
			Path path = pathRepository.save(
					Path.builder()
							.name(p.getName())
							.edition(editionTo)
							.build());

			editionTo.getPaths().add(path);
			pathMap.put(p, path);
		});

		return pathMap;
	}

	/**
	 * Adds copies of modules from the given edition (editionFrom) to the database, and adds them to the
	 * modules of the edition to copy to (editionTo). Returns the map of modules, from previous modules in
	 * editionFrom to the new modules in editionTo.
	 *
	 * @param  editionFrom The edition to copy from.
	 * @param  editionTo   The edition to copy to.
	 * @return             A map of modules, from previous modules in editionFrom to the new modules in
	 *                     editionTo.
	 */
	Map<SCModule, SCModule> copyEditionModules(SCEdition editionFrom, SCEdition editionTo) {
		Map<SCModule, SCModule> moduleMap = new HashMap<>();

		editionFrom.getModules().forEach(m -> {
			SCModule module = moduleRepository.save(
					SCModule.builder()
							.name(m.getName())
							.edition(editionTo)
							.build());

			editionTo.getModules().add(module);
			moduleMap.put(m, module);
		});

		return moduleMap;
	}

	/**
	 * Creates copies of submodules of the keys of the given module map. This map contains modules mapped to
	 * their copy. The submodules are added as submodules to the copy of the corresponding module. Returns the
	 * map of submodules, from previous submodules to the new submodules.
	 *
	 * @param  moduleMap The map of modules, from previous modules to the new modules.
	 * @return           The map of submodules, from previous submodules to the new submodules.
	 */
	Map<Submodule, Submodule> copyEditionSubmodules(Map<SCModule, SCModule> moduleMap) {
		Map<Submodule, Submodule> submoduleMap = new HashMap<>();

		moduleMap.forEach((prev, copy) -> {
			prev.getSubmodules().forEach(sm -> {
				Submodule submodule = submoduleRepository.save(
						Submodule.builder()
								.name(sm.getName())
								.module(copy)
								.row(sm.getRow())
								.column(sm.getColumn())
								.build());

				copy.getSubmodules().add(submodule);
				submoduleMap.put(sm, submodule);
			});
		});

		return submoduleMap;
	}

	/**
	 * Creates copies of skills of the keys of the given submodule map. This map contains submodules mapped to
	 * their copy. The skills are added as skills to the copy of the corresponding submodule. Additionally,
	 * links copied checkpoints to their copied skills. Returns the map of skills, from previous skills to the
	 * new skills.
	 *
	 * @param  submoduleMap  The map of submodules, from previous submodules to the new submodules.
	 * @param  checkpointMap The map of checkpoints, from previous checkpoints to the new checkpoints.
	 * @return               The map of skills, from previous skills to the new skills.
	 */
	Map<Skill, Skill> copyAndLinkEditionSkills(Map<Submodule, Submodule> submoduleMap,
			Map<Checkpoint, Checkpoint> checkpointMap) {
		Map<Skill, Skill> skillMap = new HashMap<>();

		submoduleMap.forEach((prev, copy) -> prev.getSkills().forEach(s -> {
			Checkpoint linkedCheckpoint = checkpointMap.get(s.getCheckpoint());

			if (linkedCheckpoint != null) {
				// This should hold for any correctly formed edition since a skill requires a checkpoint to be
				// linked to
				Skill skill = abstractSkillRepository.save(
						Skill.builder()
								.name(s.getName())
								.submodule(copy)
								.row(s.getRow())
								.column(s.getColumn())
								.essential(s.isEssential())
								.hidden(s.isHidden())
								.previousEditionSkill(s)
								.checkpoint(linkedCheckpoint)
								.build());

				linkedCheckpoint.getSkills().add(skill);
				s.getFutureEditionSkills().add(skill);
				copy.getSkills().add(skill);
				skillMap.put(s, skill);
			}
		}));

		return skillMap;
	}

	/**
	 * Creates copies of external skills of the keys of the given module map. The module map contains modules
	 * mapped to their copy, and the skill maps skills to their copy. The external skills are added as
	 * external skills to the copy of the corresponding module, and linked to their skill. Returns the map of
	 * external skills, from previous external skills to the new external skills.
	 *
	 * @param  moduleMap The map of modules, from previous modules to the new modules.
	 * @param  skillMap  The map of skills, from previous skills to the new skills.
	 * @return           The map of external skills, from previous external skills to the new external skills.
	 */
	Map<ExternalSkill, ExternalSkill> copyAndLinkEditionExternalSkills(
			Map<SCModule, SCModule> moduleMap, Map<Skill, Skill> skillMap) {
		Map<ExternalSkill, ExternalSkill> externalSkillMap = new HashMap<>();

		// A module has N external skills which link to other skills, these should be copied.
		// A skill has N external skills which link to it, these do not need to be copied.
		// Copy external skills for all modules in this edition, and link them to their corresponding skills.
		moduleMap.forEach((prev, copy) -> prev.getExternalSkills().forEach(s -> {
			Skill linkedSkill = skillMap.get(s.getSkill());

			// If there is no corresponding copy, this means it is part of another edition
			// Therefore the skill can be linked to that other skill without copying
			if (linkedSkill == null) {
				linkedSkill = s.getSkill();
			}

			ExternalSkill externalSkill = abstractSkillRepository.save(
					ExternalSkill.builder()
							.module(copy)
							.skill(linkedSkill)
							.row(s.getRow())
							.column(s.getColumn())
							.build());

			linkedSkill.getExternalSkills().add(externalSkill);
			copy.getExternalSkills().add(externalSkill);
			externalSkillMap.put(s, externalSkill);
		}));

		return externalSkillMap;
	}

	/**
	 * Links skills to their parents and children, given the map of copied (abstract) skills.
	 *
	 * @param abstractSkillMap The map of abstract skills, from previous abstract skills to the new abstract
	 *                         skills.
	 */
	void linkParentsChildrenSkills(Map<AbstractSkill, AbstractSkill> abstractSkillMap) {
		abstractSkillMap.forEach((prev, copy) -> prev.getParents().forEach((AbstractSkill p) -> {
			AbstractSkill linkedSkill = abstractSkillMap.get(p);
			if (linkedSkill != null) {
				// This should hold for any correctly formed edition

				copy.getParents().add(linkedSkill);
				linkedSkill.getChildren().add(copy);
			}
		}));
	}

	/**
	 * Creates copies of tasks of the keys of the given skill map. This map contains skills mapped to their
	 * copy. The tasks are added as tasks to the copy of the corresponding skill. Additionally, they are also
	 * linked to the copied path, as well as the skills they are required for.
	 *
	 * @param  skillMap The map of skills, from previous skills to the new skills.
	 * @param  pathMap  The map of paths, from previous paths to the new paths.
	 * @return          The map of tasks, from previous tasks to the new tasks.
	 */
	Map<Task, Task> copyAndLinkEditionTasks(Map<Skill, Skill> skillMap, Map<Path, Path> pathMap) {
		Map<Task, Task> taskMap = new HashMap<>();

		skillMap.forEach((prev, copy) -> prev.getTasks().forEach(t -> {
			Task task = taskRepository.save(
					Task.builder()
							.skill(copy)
							.name(t.getName())
							.type(t.getType())
							.time(t.getTime())
							.link(t.getLink())
							.idx(t.getIdx())
							.build());

			copy.getTasks().add(task);
			taskMap.put(t, task);

			t.getPaths().forEach(p -> {
				Path copiedPath = pathMap.get(p);
				if (copiedPath != null) {
					// This should hold for any correctly formed edition

					task.getPaths().add(copiedPath);
					copiedPath.getTasks().add(task);
				}
			});

			t.getRequiredFor().forEach(req -> {
				Skill copyRequiredFor = skillMap.get(req);
				if (copyRequiredFor != null) {
					// This should hold for any correctly formed edition

					task.getRequiredFor().add(copyRequiredFor);
					copyRequiredFor.getRequiredTasks().add(task);
				}
			});
		}));

		return taskMap;
	}

}
