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

import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.dto.view.edition.*;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
public class EditionService {

	private final EditionControllerApi editionApi;
	private final EditionRepository editionRepository;
	private final CircuitService circuitService;

	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final ModuleRepository moduleRepository;
	private final SubmoduleRepository submoduleRepository;
	private final AbstractSkillRepository abstractSkillRepository;
	private final AchievementRepository achievementRepository;
	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;

	@Autowired
	public EditionService(EditionControllerApi editionApi, EditionRepository editionRepository,
			CircuitService circuitService, CheckpointRepository checkpointRepository,
			PathRepository pathRepository, ModuleRepository moduleRepository,
			SubmoduleRepository submoduleRepository, AbstractSkillRepository abstractSkillRepository,
			AchievementRepository achievementRepository, SkillRepository skillRepository,
			TaskRepository taskRepository) {
		this.editionApi = editionApi;
		this.editionRepository = editionRepository;
		this.circuitService = circuitService;

		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.moduleRepository = moduleRepository;
		this.submoduleRepository = submoduleRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.achievementRepository = achievementRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
	}

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

		// TODO asserting on the new attribute in additional tests
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
	 * Copies one edition (i.e., modules, submodules etc.) to another.
	 *
	 * @param copyTo   The id of the edition to copy to.
	 * @param copyFrom The id of the edition to copy from.
	 */
	@Transactional
	public void copyEdition(Long copyTo, Long copyFrom) {
		// Check if the edition to copy to, is empty
		// This should already be ensured by the frontend in practice
		if (!isEditionEmpty(copyTo)) {
			return;
		}

		SCEdition editionTo = editionRepository.findByIdOrThrow(copyTo);
		SCEdition editionFrom = editionRepository.findByIdOrThrow(copyFrom);

		// ---- Copy checkpoints ----
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

		// ---- Copy paths ----
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

		editionTo.setDefaultPath(pathMap.get(editionFrom.getDefaultPath()));

		// ---- Copy modules/submodules ----
		Map<SCModule, SCModule> moduleMap = new HashMap<>();
		Map<Submodule, Submodule> submoduleMap = new HashMap<>();

		editionFrom.getModules().forEach(m -> {
			SCModule module = moduleRepository.save(
					SCModule.builder()
							.name(m.getName())
							.edition(editionTo)
							.build());

			editionTo.getModules().add(module);
			moduleMap.put(m, module);

			m.getSubmodules().forEach(sm -> {
				Submodule submodule = submoduleRepository.save(
						Submodule.builder()
								.name(sm.getName())
								.module(module)
								.row(sm.getRow())
								.column(sm.getColumn())
								.build());

				module.getSubmodules().add(submodule);
				submoduleMap.put(sm, submodule);
			});
		});

		// ---- Copy external skills from modules ----
		Map<ExternalSkill, ExternalSkill> externalSkillMap = new HashMap<>();
		Map<AbstractSkill, AbstractSkill> abstractSkillMap = new HashMap<>();

		moduleMap.forEach((prev, copy) -> prev.getExternalSkills().forEach(s -> {
			ExternalSkill externalSkill = abstractSkillRepository.save(
					ExternalSkill.builder()
							.module(copy)
							.build());

			copy.getExternalSkills().add(externalSkill);
			externalSkillMap.put(s, externalSkill);
			abstractSkillMap.put(s, externalSkill);
		}));

		// ---- Copy skills from submodules ----
		// ---- Copy/link external skills from/to skills ----
		// ---- Link checkpoints and skills ----
		Map<Skill, Skill> skillMap = new HashMap<>();

		submoduleMap.forEach((prev, copy) -> prev.getSkills().forEach(s -> {
			Checkpoint linkedCheckpoint = checkpointsMap.get(s.getCheckpoint());
			if (linkedCheckpoint != null) {
				// This should hold for any correctly formed edition
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
				abstractSkillMap.put(s, skill);

				s.getExternalSkills().forEach(extSkill -> {
					ExternalSkill linkedSkill = externalSkillMap.get(extSkill);
					if (linkedSkill != null) {
						// It is linked to a module of this edition

						skill.getExternalSkills().add(linkedSkill);
					} else {
						// It is not linked to a module of this edition

						ExternalSkill externalSkill = abstractSkillRepository.save(
								ExternalSkill.builder()
										.module(extSkill.getModule())
										.build());

						skill.getExternalSkills().add(externalSkill);
						externalSkillMap.put(extSkill, externalSkill);
					}
				});
			}
		}));

		// ---- Link parents/children of skills ----
		abstractSkillMap.forEach((prev, copy) -> prev.getParents().forEach((AbstractSkill p) -> {
			AbstractSkill linkedSkill = abstractSkillMap.get(p);
			if (linkedSkill != null) {
				// This should hold for any correctly formed edition

				copy.getParents().add(linkedSkill);
				linkedSkill.getChildren().add(copy);
			}
		}));

		// ---- Copy tasks from skills and link to path and required skills ----
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

		// ---- Copy achievements ----
		Map<Achievement, Achievement> achievementMap = new HashMap<>();

		taskMap.forEach((prev, copy) -> prev.getAchievements().forEach(a -> {
			Achievement copiedAchievement = achievementMap.get(a);
			if (copiedAchievement == null) {
				Achievement achievement = achievementRepository.save(
						Achievement.builder()
								.name(a.getName())
								.build());

				a.getTasks().forEach(innerTask -> {
					Task copiedTask = taskMap.get(innerTask);
					if (copiedTask != null) {
						achievement.getTasks().add(copiedTask);
					} else {
						achievement.getTasks().add(innerTask);
					}
				});

				copy.getAchievements().add(achievement);
				achievementMap.put(a, achievement);
			} else {
				copy.getAchievements().add(copiedAchievement);
			}
		}));

		editionRepository.flush();
		checkpointRepository.flush();
		pathRepository.flush();
		moduleRepository.flush();
		submoduleRepository.flush();
		abstractSkillRepository.flush();
		achievementRepository.flush();
		skillRepository.flush();
		taskRepository.flush();
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

}
