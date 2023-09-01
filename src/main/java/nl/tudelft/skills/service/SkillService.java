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

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.EditionSummaryDTO;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.security.AuthorisationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

	private final AbstractSkillRepository abstractSkillRepository;
	private final TaskCompletionRepository taskCompletionRepository;
	private final EditionRepository editionRepository;
	private final EditionControllerApi editionApi;
	private final CourseControllerApi courseApi;
	private final SkillRepository skillRepository;
	private final AuthorisationService authorisationService;
	private final ClickedLinkService clickedLinkService;

	@Autowired
	public SkillService(AbstractSkillRepository abstractSkillRepository,
			TaskCompletionRepository taskCompletionRepository, EditionControllerApi editionApi,
			CourseControllerApi courseApi, SkillRepository skillRepository,
			EditionRepository editionRepository, AuthorisationService authorisationService,
			ClickedLinkService clickedLinkService) {
		this.abstractSkillRepository = abstractSkillRepository;
		this.taskCompletionRepository = taskCompletionRepository;
		this.editionApi = editionApi;
		this.courseApi = courseApi;
		this.skillRepository = skillRepository;
		this.editionRepository = editionRepository;
		this.authorisationService = authorisationService;
		this.clickedLinkService = clickedLinkService;
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
			s.getFutureEditionSkills().forEach(innerSkill -> innerSkill.setPreviousEditionSkill(null));

			clickedLinkService.deleteClickedLinksForTasks(s.getTasks());

			if (s.getPreviousEditionSkill() != null) {
				s.getPreviousEditionSkill().getFutureEditionSkills().remove(s);
			}
		}
		abstractSkillRepository.delete(skill);
		return skill;
	}

	/**
	 * Gets the correct skill for an external skill. This is the skill in the most recent edition which the
	 * person has last worked on. If none such edition exists, the most recent edition is chosen. "Most
	 * recent" refers to the start date of the edition, and not the date of activity by the person. If there
	 * is no skill in an edition visible to the user, this returns null.
	 *
	 * @param  personId      The id of the person.
	 * @param  externalSkill The external skill.
	 * @return               The correct skill which the external skill refers to. This is the skill in the
	 *                       most recent edition which the person has last worked on. If none such edition
	 *                       exists, the most recent edition is chosen.
	 */
	public Skill recentActiveEditionForSkillOrLatest(Long personId, ExternalSkill externalSkill) {
		Skill skill = externalSkill.getSkill();
		Long editionId = skill.getSubmodule().getModule().getEdition().getId();
		CourseDetailsDTO course = courseApi.getCourseByEdition(editionId).block();

		Map<Long, EditionSummaryDTO> editionsById = course.getEditions().stream()
				.collect(toMap(EditionSummaryDTO::getId, Function.identity()));

		// Get ids of the editions the person as completed at least one task in
		Set<Long> completedTasksInEditions = taskCompletionRepository.getByPersonId(personId).stream()
				.map(taskCompletion -> taskCompletion.getTask().getSkill().getSubmodule()
						.getModule().getEdition().getId())
				.collect(Collectors.toSet());

		// Do DFS
		List<Skill> traversal = traverseSkillTree(skill);

		// Filter by skills in editions visible to the user
		traversal = traversal.stream()
				.filter(innerSkill -> {
					Long innerEditionId = innerSkill.getSubmodule().getModule().getEdition().getId();
					return authorisationService.canViewEdition(innerEditionId);
				})
				.sorted(Comparator.comparing((Skill innerSkill) -> editionsById.get(innerSkill.getSubmodule()
						.getModule().getEdition().getId()).getStartDate()).reversed())
				.collect(Collectors.toList());

		// If it exists, return the skill from the last edition the person has completed a task in
		Optional<Skill> completedTasksInEdition = traversal.stream()
				.filter(innerSkill -> completedTasksInEditions.contains(innerSkill.getSubmodule()
						.getModule().getEdition().getId()))
				.findFirst();

		// If it does not exist, return the skill in the latest edition
		// If there is no skill visible to the user, returns null
		return completedTasksInEdition.orElse(traversal.size() > 0 ? traversal.get(0) : null);
	}

	/**
	 * Return the DFS traversal of the "skill tree" for the given tree. The tree contains all skill versions
	 * in different editions, linked by the previous/future skill(s) attributes
	 *
	 * @param  skill The skill for which to traverse the tree of skill versions.
	 * @return       The DFS traversal of the skill tree for the given skill.
	 */
	public List<Skill> traverseSkillTree(Skill skill) {
		// Find root of the "skill tree"
		Skill current = skill;
		while (current.getPreviousEditionSkill() != null) {
			current = current.getPreviousEditionSkill();
		}

		// Do DFS
		Stack<Skill> traversal = new Stack<>();
		traversal.push(current);

		List<Skill> skills = new ArrayList<>();

		while (!traversal.isEmpty()) {
			current = traversal.pop();
			skills.add(current);

			// Continue traversal
			Set<Skill> nextSkills = current.getFutureEditionSkills();
			for (Skill nextSkill : nextSkills) {
				traversal.push(nextSkill);
			}
		}

		return skills;
	}

}
