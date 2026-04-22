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
package nl.tudelft.skills.security;

import static java.util.Objects.requireNonNull;
import static nl.tudelft.labracore.api.dto.RoleDetailsDTO.TypeEnum.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.labracore.api.dto.RoleEditionDetailsDTO;
import nl.tudelft.labracore.api.dto.RoleId;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.cache.RoleCacheManager;
import nl.tudelft.skills.dto.id.ChoiceTaskId;
import nl.tudelft.skills.dto.id.SCModuleId;
import nl.tudelft.skills.dto.id.SkillId;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.dto.patch.SkillPositionUpdates;
import nl.tudelft.skills.dto.patch.SubmodulePositionUpdates;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.bookmark.BookmarkList;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;
import nl.tudelft.skills.model.bookmark.PersonalBookmarkList;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.PersonRepository;

@Service
@AllArgsConstructor
public class AuthorisationService {

	private final PersonControllerApi personApi;

	private final RoleCacheManager roleCache;

	private final AbstractSkillRepository abstractSkillRepository;
	private final EditionRepository editionRepository;
	private final PersonRepository personRepository;
	private final SubmoduleRepository submoduleRepository;

	private final DTOConverter dtoConverter;

	/**
	 * Gets the currently authenticated user.
	 *
	 * @return The currently authenticated person, and null if no such person exists.
	 */
	public Person getAuthenticatedPerson() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		if (authentication.getPrincipal() instanceof LabradorUserDetails userDetails) {
			return userDetails.getUser();
		}
		return null;
	}

	/**
	 * Gets whether the user is authenticated.
	 *
	 * @return True iff the user is authenticated
	 */
	public boolean isAuthenticated() {
		return getAuthenticatedPerson() != null;
	}

	/*
	 * Role
	 */

	public boolean hasAdminRole() {
		return withAuthenticatedPerson(person -> person.getDefaultRole() == DefaultRole.ADMIN);
	}

	public boolean isAdmin() {
		return hasAdminRole() && withAuthenticatedSCPerson(person -> person.getViewMode() == ViewMode.ADMIN);
	}

	public boolean hasEditorRole(Long editionId) {
		return withSCEdition(editionId,
				edition -> withAuthenticatedSCPerson(person -> edition.getEditors().stream()
						.anyMatch(editor -> Objects.equals(editor.getId(), person.getId()))))
				|| withRole(editionId, role -> role == TEACHER);
	}

	public boolean isTeacher() {
		if (isAdmin()) {
			return true;
		}
		return withAuthenticatedPerson(person -> requireNonNull(personApi.getRolesForPerson(person.getId())
				.any(role -> role.getType() == RoleEditionDetailsDTO.TypeEnum.TEACHER).block()));
	}

	public boolean isTeacher(Long editionId) {
		if (isAdmin()) {
			return true;
		}
		return withRole(editionId, role -> role == TEACHER)
				&& withAuthenticatedSCPerson(person -> person.getViewMode() == ViewMode.EDITOR);
	}

	public boolean isEditor(Long editionId) {
		if (isAdmin()) {
			return true;
		}
		return hasEditorRole(editionId)
				&& withAuthenticatedSCPerson(person -> person.getViewMode() == ViewMode.EDITOR);
	}

	/*
	 * Permissions
	 */

	public boolean canSearchForPeople() {
		return isTeacher();
	}

	public boolean canManageEditionEditors(Long editionId) {
		return isTeacher(editionId) || isEditor(editionId);
	}

	public boolean canEditEditionCircuit(Long editionId) {
		return isAdmin() || isEditor(editionId);
	}

	public boolean canViewEdition(Long editionId) {
		return isAdmin() || hasEditorRole(editionId) || editionRepository.getOrCreate(editionId).isVisible();
	}

	public boolean canExportEditionStatistics(Long editionId) {
		return isAdmin() || isTeacher(editionId);
	}

	public boolean canEditModuleCircuit(SCModuleId moduleId) {
		return canEditModuleCircuit(dtoConverter.apply(moduleId));
	}

	public boolean canEditModuleCircuit(SCModule module) {
		return canEditEditionCircuit(module.getEdition().getId());
	}

	public boolean canViewModuleCircuit(SCModule module) {
		return canViewEdition(module.getEdition().getId());
	}

	public boolean canEditSubmodule(SubmoduleId submoduleId) {
		return canEditSubmodule(dtoConverter.apply(submoduleId));
	}

	public boolean canEditSubmodule(Submodule submodule) {
		return canEditModuleCircuit(submodule.getModule());
	}

	public boolean canViewSkill(AbstractSkill abstractSkill) {
		return switch (abstractSkill) {
			case Skill skill -> canViewEdition(skill.getSubmodule().getModule().getEdition().getId());
			case ExternalSkill skill -> canViewEdition(skill.getModule().getEdition().getId());
			default -> false; // Unreachable
		};
	}

	public boolean canEditSkill(SkillId skillId) {
		return canEditSkill(dtoConverter.apply(skillId));
	}

	public boolean canEditSkill(AbstractSkill abstractSkill) {
		return switch (abstractSkill) {
			case Skill skill -> canEditEditionCircuit(skill.getSubmodule().getModule().getEdition().getId());
			case ExternalSkill skill -> canEditEditionCircuit(skill.getModule().getEdition().getId());
			default -> false; // Unreachable
		};
	}

	public boolean canEditSkillPositions(SkillPositionUpdates positions) {
		Set<Long> skillIds = positions.updates().stream()
				.map(update -> update.skill().getId())
				.collect(Collectors.toSet());
		Set<AbstractSkill> skills = abstractSkillRepository.findAllByIdIn(skillIds);
		if (!foundAllIds(skills.stream().map(AbstractSkill::getId).collect(Collectors.toSet()),
				skillIds)) {
			return false;
		}

		List<Long> editionIds = skills.stream().map(this::getEditionId).toList();
		if (editionIds.stream().anyMatch(Objects::isNull)) {
			return false;
		}

		return editionIds.stream()
				.distinct()
				.allMatch(this::canEditEditionCircuit);
	}

	public boolean canEditBookmarkList(BookmarkList list) {
		return withAuthenticatedPerson(person -> switch (list) {
			case PersonalBookmarkList personalBookmarkList ->
				Objects.equals(personalBookmarkList.getPerson().getId(), person.getId());
			case HiddenSkillBookmarkList hiddenSkillBookmarkList ->
				isAdmin() || hasEditorRole(
						hiddenSkillBookmarkList.getSkill().getSubmodule().getModule().getEdition().getId());
			default -> false; // Unreachable
		});
	}

	public boolean canEditTask(ChoiceTaskId taskId) {
		return canEditSkill(dtoConverter.apply(taskId).getSkill());
	}

	public boolean canEditTask(ChoiceTask choiceTask) {
		return canEditSkill(choiceTask.getSkill());
	}

	public boolean canViewTaskInfo(TaskInfo taskInfo) {
		if (taskInfo.getTask() != null) {
			return canViewSkill(taskInfo.getTask().getSkill());
		}
		if (taskInfo.getChoiceTask() != null) {
			return canViewSkill(taskInfo.getChoiceTask().getSkill());
		}
		return false;
	}

	public boolean canEditTaskInfo(TaskInfo taskInfo) {
		if (taskInfo.getTask() != null) {
			return canEditSkill(taskInfo.getTask().getSkill());
		}
		if (taskInfo.getChoiceTask() != null) {
			return canEditSkill(taskInfo.getChoiceTask().getSkill());
		}
		return false;
	}

	public boolean canEditSubmodulePositions(SubmodulePositionUpdates positions) {
		Set<Long> submoduleIds = positions.updates().stream()
				.map(update -> update.submodule().getId())
				.collect(Collectors.toSet());
		List<Submodule> submodules = submoduleRepository.findAllById(submoduleIds);
		if (!foundAllIds(submodules.stream().map(Submodule::getId).collect(Collectors.toSet()),
				submoduleIds)) {
			return false;
		}

		return submodules.stream()
				.map(submodule -> submodule.getModule().getEdition().getId())
				.distinct()
				.allMatch(this::canEditEditionCircuit);
	}

	/*
	 * Helper
	 */

	private boolean foundAllIds(Set<Long> foundIds, Set<Long> requestedIds) {
		return foundIds.equals(requestedIds);
	}

	private Long getEditionId(AbstractSkill abstractSkill) {
		return switch (abstractSkill) {
			case Skill skill -> skill.getSubmodule().getModule().getEdition().getId();
			case ExternalSkill skill -> skill.getModule().getEdition().getId();
			default -> null; // Unreachable
		};
	}

	public boolean withAuthenticatedPerson(Predicate<Person> predicate) {
		Person authenticatedPerson = getAuthenticatedPerson();
		return authenticatedPerson != null && predicate.test(authenticatedPerson);
	}

	public boolean withAuthenticatedSCPerson(Predicate<SCPerson> predicate) {
		return withAuthenticatedPerson(
				person -> predicate.test(personRepository.findByIdOrThrow(person.getId())));
	}

	public boolean withSCEdition(Long editionId, Predicate<SCEdition> predicate) {
		return predicate.test(editionRepository.getOrCreate(editionId));
	}

	public boolean withRole(Long editionId, Predicate<RoleDetailsDTO.TypeEnum> predicate) {
		if (!isAuthenticated()) {
			return false;
		}
		RoleDetailsDTO.TypeEnum role = roleCache
				.get(new RoleId().editionId(editionId).personId(getAuthenticatedPerson().getId()))
				.map(RoleDetailsDTO::getType).orElse(null);
		return role != null && predicate.test(role);
	}

}
