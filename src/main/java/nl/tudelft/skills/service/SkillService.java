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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.AbstractSkillRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.TaskCompletionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

	private final AbstractSkillRepository abstractSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final EditionControllerApi editionApi;
	private final CourseControllerApi courseApi;
	private final SkillRepository skillRepository;

	@Autowired
	public SkillService(AbstractSkillRepository abstractSkillRepository,
			TaskCompletionRepository taskCompletionRepository, EditionControllerApi editionApi,
			CourseControllerApi courseApi, SkillRepository skillRepository) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.editionApi = editionApi;
		this.courseApi = courseApi;
		this.skillRepository = skillRepository;
	}

	/**
	 * Deletes a skill.
	 *
	 * @param  id The id of the skill
	 * @return    The deleted skill
	 */
	@Transactional
	public AbstractSkill deleteSkill(Long id) {
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(id);
		skill.getChildren().forEach(c -> c.getParents().remove(skill));
		if (skill instanceof Skill s) {
			s.getTasks().forEach(t -> taskCompletionRepository.deleteAll(t.getCompletedBy()));
		}
		abstractSkillRepository.delete(skill);
		return skill;
	}

	/**
	 * Gets the correct skill for an external skill. This is the skill in the most recent edition which the
	 * person has last worked on. If none such edition exists, the most recent edition is chosen. "Most
	 * recent" refers to the start date of the edition, and not the date of activity by the person.
	 *
	 * @param  personId      The id of the person.
	 * @param  externalSkill The external skill.
	 * @return               The correct skill which the external skill refers to. This is the skill in the
	 *                       most recent edition which the person has last worked on. If none such edition
	 *                       exists, the most recent edition is chosen.
	 */
	public Skill recentActiveEditionForSkillOrLatest(Long personId, ExternalSkill externalSkill) {
		// TODO check access rights, if, for example, an edition is not available anymore

		Skill skill = externalSkill.getSkill();
		Long editionId = skill.getSubmodule().getModule().getEdition().getId();
		CourseDetailsDTO course = courseApi.getCourseByEdition(editionId).block();

		// Get the edition ids for the course, sorted by the start date (decreasing, newest to oldest)
		List<Long> sortedEditionIdsOfCourse = course.getEditions().stream()
				.sorted(Comparator.comparing(EditionSummaryDTO::getStartDate))
				.map(EditionSummaryDTO::getId)
				.collect(Collectors.toList());
		Collections.reverse(sortedEditionIdsOfCourse);

		// Get ids of the editions the person as completed at least one task in
		List<Long> completedTasksInEditions = taskCompletionRepository.getByPersonId(personId).stream()
				.map(taskCompletion -> taskCompletion.getTask().getSkill().getSubmodule()
						.getModule().getEdition().getId())
				.collect(Collectors.toList());

		// For efficiency purposes, create a map with the editionId as key, and the index in the sorted list as value
		Map<Long, Integer> editionToOrderIdx = sortedEditionIdsOfCourse.stream().collect(
				Collectors.toMap(edition -> edition, sortedEditionIdsOfCourse::indexOf));

		// Find root of the "skill tree"
		Skill current = skill;
		while (current.getPreviousEditionSkill() != null) {
			current = current.getPreviousEditionSkill();
		}

		// Do DFS, keep track of:
		// 1. "active edition" skill which:
		//		- is in an edition the person has completed something in
		//		- is the most recent edition
		// 2. "most recent" skill which only fulfills the last condition (most recent)
		Skill recentActiveEditionSkill = null;
		Skill mostRecentEditionSkill = null;
		Stack<Skill> traversal = new Stack<>();
		traversal.push(current);
		while (!traversal.isEmpty()) {
			current = traversal.pop();
			Long currentEditionId = current.getSubmodule().getModule().getEdition().getId();

			// Check if this skills edition is more recent
			// Was not assigned yet || current order index < most recent order index
			if (mostRecentEditionSkill == null ||
					editionToOrderIdx.get(currentEditionId) < editionToOrderIdx
							.get(mostRecentEditionSkill.getSubmodule()
									.getModule().getEdition().getId())) {
				mostRecentEditionSkill = current;
			}

			// Check if the person has completed a skill in this edition, and if it is more recent
			// Was not assigned yet || current order index < recent active order index
			if (completedTasksInEditions.contains(currentEditionId) &&
					(recentActiveEditionSkill == null ||
							editionToOrderIdx.get(currentEditionId) < editionToOrderIdx
									.get(recentActiveEditionSkill.getSubmodule()
											.getModule().getEdition().getId()))) {
				recentActiveEditionSkill = current;
			}

			List<Skill> nextSkills = skillRepository.findByPreviousEditionSkill(current);
			for (Skill nextSkill : nextSkills) {
				traversal.push(nextSkill);
			}
		}

		// If the "active edition" skill is null at the end, this means that there is no edition the person
		// has completed something in, which contains the skill
		// Then return the "most recent" skill
		if (recentActiveEditionSkill == null) {
			return mostRecentEditionSkill;
		}

		// Return "active edition" skill
		return recentActiveEditionSkill;
	}

}
