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
package nl.tudelft.skills.security;

import static nl.tudelft.labracore.api.dto.RoleDetailsDTO.TypeEnum.*;

import javax.annotation.Nullable;

import nl.tudelft.labracore.api.dto.Id;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.cache.RoleCacheManager;
import nl.tudelft.skills.repository.SkillRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorisationService {

	private RoleCacheManager roleCache;

	private SkillRepository skillRepository;

	@Autowired
	public AuthorisationService(RoleCacheManager roleCache, SkillRepository skillRepository) {
		this.roleCache = roleCache;
		this.skillRepository = skillRepository;
	}

	/**
	 * Gets the currently authenticated user.
	 *
	 * @return The currently authenticated user
	 */
	public Person getAuthPerson() {
		return ((LabradorUserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal()).getUser();
	}

	/**
	 * Gets whether the user is authenticated.
	 *
	 * @return True iff the user is authenticated
	 */
	public boolean isAuthenticated() {
		return SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal() instanceof LabradorUserDetails;
	}

	/**
	 * Gets whether the authenticated user is an admin.
	 *
	 * @return The iff the authenticated user is an admin
	 */
	public boolean isAdmin() {
		if (!isAuthenticated())
			return false;
		return getAuthPerson().getDefaultRole() == DefaultRole.ADMIN;
	}

	/**
	 * Gets whether the authenticated user can edit the skills in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can edit the skills in the edition
	 */
	public boolean canEditSkills(Long editionId) {
		return isAtLeastTeacherInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can edit a skill.
	 *
	 * @param  skillId The id of the skill
	 * @return         True iff the user can edit the skill
	 */
	@Transactional
	public boolean canEditSkill(Long skillId) {
		return canEditSkills(
				skillRepository.findByIdOrThrow(skillId).getSubmodule().getModule().getEdition());
	}

	/**
	 * Gets the authenticated user's role in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           The user's role or null if the person has no role in the edition
	 */
	public @Nullable RoleDetailsDTO.TypeEnum getRoleInEdition(Long editionId) {
		if (!isAuthenticated())
			return null;
		return roleCache.get(new Id().editionId(editionId).personId(getAuthPerson().getId()))
				.map(RoleDetailsDTO::getType).orElse(null);
	}

	/**
	 * Checks whether the authenticated user is at least a teacher in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is an admin or teacher in the edition
	 */
	public boolean isAtLeastTeacherInEdition(Long editionId) {
		return isAdmin() || getRoleInEdition(editionId) == TEACHER;
	}

	/**
	 * Checks whether the authenticated user has the teacher read-only role in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user has the teacher read-only role.
	 */
	public boolean isTeacherROInEdition(Long editionId) {
		return getRoleInEdition(editionId) == TEACHER_RO;
	}

	/**
	 * Checks whether the authenticated user is at least a head TA in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is an admin, teacher, or head TA in the edition
	 */
	public boolean isAtLeastHeadTAInEdition(Long editionId) {
		var role = getRoleInEdition(editionId);
		return isAdmin() || role == TEACHER || role == HEAD_TA;
	}

	/**
	 * Checks whether the authenticated user is at least a TA in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is an admin, teacher, head TA, or TA in the edition
	 */
	public boolean isAtLeastTAInEdition(Long editionId) {
		var role = getRoleInEdition(editionId);
		return isAdmin() || role == TEACHER || role == HEAD_TA || role == TA;
	}

	/**
	 * Checks whether the authenticated user is at least a student in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is an admin, teacher, head TA, TA or student in the edition
	 */
	public boolean isAtLeastStudentInEdition(Long editionId) {
		var role = getRoleInEdition(editionId);
		return isAdmin() || (role != null && role != BLOCKED && role != TEACHER_RO);
	}

	/**
	 * Checks whether the authenticated user is a student in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is a student in the edition
	 */
	public boolean isStudentInEdition(Long editionId) {
		return getRoleInEdition(editionId) == STUDENT;
	}

}