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

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import nl.tudelft.labracore.api.dto.RoleId;
import nl.tudelft.skills.enums.ViewMode;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.dto.CourseDetailsDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.labracore.api.dto.RoleEditionDetailsDTO;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.DefaultRole;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.cache.RoleCacheManager;
import nl.tudelft.skills.repository.*;

import java.util.Optional;
import java.util.function.Predicate;

@Service
@AllArgsConstructor
public class AuthorisationService {

    private final EditionRepository editionRepository;
    private final PersonRepository personRepository;

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
        return withRole(editionId, role -> role == TEACHER || role == HEAD_TA);
    }

    public boolean isEditor(Long editionId) {
        if (isAdmin()) {
            return true;
        }
        return hasEditorRole(editionId) && withAuthenticatedSCPerson(person -> person.getViewMode() == ViewMode.EDITOR);
    }

    /*
     * Permissions
     */

    public boolean canEditEditionCircuit(Long editionId) {
        return isAdmin() || isEditor(editionId);
    }

    public boolean canViewEditionCircuit(Long editionId) {
        return isAdmin() || canEditEditionCircuit(editionId) || editionRepository.findByIdOrThrow(editionId).isVisible();
    }

    public boolean canEditModuleCircuit(SCModule module) {
        return canEditEditionCircuit(module.getEdition().getId());
    }

    public boolean canViewModuleCircuit(SCModule module) {
        return canViewEditionCircuit(module.getEdition().getId());
    }

    /*
     * Helper
     */

    public boolean withAuthenticatedPerson(Predicate<Person> predicate) {
        Person authenticatedPerson = getAuthenticatedPerson();
        return authenticatedPerson != null && predicate.test(authenticatedPerson);
    }

    public boolean withAuthenticatedSCPerson(Predicate<SCPerson> predicate) {
        return withAuthenticatedPerson(person -> predicate.test(personRepository.findByIdOrThrow(person.getId())));
    }

    public boolean withRole(Long editionId, Predicate<RoleDetailsDTO.TypeEnum> predicate) {
        if (!isAuthenticated()) {
            return false;
        }
        RoleDetailsDTO.TypeEnum role = roleCache.get(new RoleId().editionId(editionId).personId(getAuthenticatedPerson().getId())).map(RoleDetailsDTO::getType).orElse(null);
        return role != null && predicate.test(role);
    }

    // Old

	private RoleCacheManager roleCache;

	private ModuleRepository moduleRepository;
	private SubmoduleRepository submoduleRepository;
	private SkillRepository skillRepository;

	private TaskRepository taskRepository;
	private CheckpointRepository checkpointRepository;
	private PathRepository pathRepository;
	private AbstractSkillRepository abstractSkillRepository;

	private CourseControllerApi courseApi;
	private PersonControllerApi personApi;

	/**
	 * Gets whether the authenticated user is staff.
	 *
	 * @return True iff the authenticated user is staff
	 */
	public boolean isStaff() {
		return isAdmin() || getAuthenticatedPerson().getDefaultRole() == DefaultRole.TEACHER;
	}

	/**
	 * Gets whether the authenticated user is a manager somewhere.
	 *
	 * @return True iff the authenticated user is a manager
	 */
	public boolean isManagerAnywhere() {
		return isStaff() || personApi.getRolesForPerson(getAuthenticatedPerson().getId())
				.any(r -> r.getType() == RoleEditionDetailsDTO.TypeEnum.HEAD_TA
						|| r.getType() == RoleEditionDetailsDTO.TypeEnum.TEACHER)
				.block();
	}

	/**
	 * Gets whether the authenticated user is a student by default.
	 *
	 * @return True iff the authenticated user is a student by default.
	 */
	public boolean isStudent() {
		if (!isAuthenticated())
			return false;
		return getAuthenticatedPerson().getDefaultRole() == DefaultRole.STUDENT;
	}

	/**
	 * Gets whether the authenticated user can view all the editions of a course.
	 *
	 * @param  courseId The id of the course.
	 * @return          True iff the user can view all the editions of a course by id.
	 */
	public boolean canViewCourse(Long courseId) {
		return isAuthenticated() && isAtLeastTeacherInCourse(courseId);
	}

	/**
	 * Gets whether the authenticated user can view an edition.
	 *
	 * @param  editionId The id of the course.
	 * @return           True iff the user can view the edition.
	 */
	public boolean canViewEdition(Long editionId) {
		return isAuthenticated() && (isAtLeastHeadTAInEdition(editionId)
				|| editionRepository.findByIdOrThrow(editionId).isVisible());
	}

	/**
	 * Gets whether the authenticated user can view a module.
	 *
	 * @param  moduleId The id of the module.
	 * @return          True iff the user can view the module.
	 */
	public boolean canViewModule(Long moduleId) {
		return isAuthenticated() && canViewEdition(moduleRepository.getById(moduleId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can view a skill.
	 *
	 * @param  skillId The id of the skill.
	 * @return         True iff the user can view the skill.
	 */
	public boolean canViewSkill(Long skillId) {
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(skillId);

		if (skill instanceof ExternalSkill) {
			return isAuthenticated()
					&& canViewEdition(((ExternalSkill) skill).getModule().getEdition().getId());
		}
		return isAuthenticated() && canViewEdition(skill.getSubmodule().getModule().getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can edit the edition
	 *
	 * @param  editionId The id of the edition.
	 * @return           True iff the user can publish the edition.
	 */
	public boolean canEditEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can publish the edition for students.
	 *
	 * @param  editionId The id of the edition.
	 * @return           True iff the user can publish the edition.
	 */
	public boolean canPublishEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can create a module in the edition.
	 *
	 * @param  editionId The edition id.
	 * @return           True iff the user can create a module in the edition.
	 */
	public boolean canCreateModuleInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can delete a module in the edition.
	 *
	 * @param  editionId The edition id.
	 * @return           True iff the user can delete a module in the edition.
	 */
	public boolean canDeleteModuleInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can delete a module.
	 *
	 * @param  moduleId The module id.
	 * @return          True iff the user can delete a module.
	 */
	public boolean canDeleteModule(Long moduleId) {
		return canDeleteModuleInEdition(moduleRepository.findByIdOrThrow(moduleId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can edit a module in a edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can edit the module in edition
	 */
	@Transactional
	public boolean canEditModuleInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can edit a module.
	 *
	 * @param  moduleId The id of the module
	 * @return          True iff the user can edit the module
	 */
	@Transactional
	public boolean canEditModule(Long moduleId) {
		return canEditModuleInEdition(
				moduleRepository.findByIdOrThrow(moduleId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can create submodules in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can create submodules in the edition
	 */
	public boolean canCreateSubmoduleInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can create a submodule in a module.
	 *
	 * @param  moduleId The id of the module
	 * @return          True iff the user can create skills in the module
	 */
	public boolean canCreateSubmodule(Long moduleId) {
		return canCreateSubmoduleInEdition(
				moduleRepository.findByIdOrThrow(moduleId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can delete a submodule in the edition.
	 *
	 * @param  editionId The edition id.
	 * @return           True iff the user can delete a submodule in the edition.
	 */
	public boolean canDeleteSubmoduleInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can delete a submodule.
	 *
	 * @param  submoduleId The submodule id.
	 * @return             True iff the user can delete a submodule.
	 */
	public boolean canDeleteSubmodule(Long submoduleId) {
		return canDeleteSubmoduleInEdition(
				submoduleRepository.findByIdOrThrow(submoduleId).getModule().getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can edit a submodule in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can edit the submodule in edition
	 */
	@Transactional
	public boolean canEditSubmoduleInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can edit a submodule.
	 *
	 * @param  submoduleId The id of the module
	 * @return             True iff the user can edit the submodule
	 */
	@Transactional
	public boolean canEditSubmodule(Long submoduleId) {
		return canEditSubmoduleInEdition(
				submoduleRepository.findByIdOrThrow(submoduleId).getModule().getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can create skills in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can create skills in the edition
	 */
	public boolean canCreateSkillInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can create a skill in a module.
	 *
	 * @param  moduleId The id of the module
	 * @return          True iff the user can create skills in the module
	 */
	public boolean canCreateSkillInModule(Long moduleId) {
		return canCreateSkillInEdition(
				moduleRepository.findByIdOrThrow(moduleId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can create a skill in a submodule.
	 *
	 * @param  submoduleId The id of the submodule
	 * @return             True iff the user can create skills in the submodule
	 */
	public boolean canCreateSkill(Long submoduleId) {
		return canCreateSkillInEdition(
				submoduleRepository.findByIdOrThrow(submoduleId).getModule().getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can edit the skills in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can edit the skills in the edition
	 */
	public boolean canEditSkillInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can edit a skill.
	 *
	 * @param  skillId The id of the skill
	 * @return         True iff the user can edit the skill
	 */
	@Transactional
	public boolean canEditSkill(Long skillId) {
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(skillId);
		if (skill instanceof Skill) {
			return canEditSkillInEdition(((Skill) skill).getSubmodule().getModule().getEdition().getId());
		} else {
			return canEditSkillInEdition(((ExternalSkill) skill).getModule().getEdition().getId());
		}
	}

	/**
	 * Gets whether the authenticated user can delete skills in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can delete skills in the edition
	 */
	public boolean canDeleteSkillInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can delete a skill.
	 *
	 * @param  skillId The id of the skill
	 * @return         True iff the user can delete the skill
	 */
	public boolean canDeleteSkill(Long skillId) {
		AbstractSkill skill = abstractSkillRepository.findByIdOrThrow(skillId);
		SCModule module = skill instanceof ExternalSkill s ? s.getModule() : skill.getSubmodule().getModule();
		return canDeleteSkillInEdition(module.getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can edit a task.
	 *
	 * @param  taskId The id of the task
	 * @return        True iff the user can edit the task
	 */
	public boolean canEditTask(Long taskId) {
		return canEditSkill(taskRepository.findByIdOrThrow(taskId).getSkill().getId());
	}

	/**
	 * Gets whether the authenticated user can delete a task.
	 *
	 * @param  taskId The id of the task
	 * @return        True iff the user can delete the task
	 */
	public boolean canDeleteTask(Long taskId) {
		return canEditSkill(taskRepository.findByIdOrThrow(taskId).getSkill().getId());
	}

	/**
	 * Gets whether the authenticated user edit a checkpoint in an edition
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can edit checkpoints in the edition
	 */
	public boolean canEditCheckpointInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can edit a checkpoint
	 *
	 * @param  checkpointId The id of the checkpoint
	 * @return              True iff the user can edit the checkpoint
	 */
	public boolean canEditCheckpoint(Long checkpointId) {
		return canEditCheckpointInEdition(
				checkpointRepository.findByIdOrThrow(checkpointId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can delete a checkpoint in an edition
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can delete checkpoints in the edition
	 */
	public boolean canDeleteCheckpointInEdition(Long editionId) {
		return canEditCheckpointInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can delete a checkpoint
	 *
	 * @param  checkpointId The id of the checkpoint
	 * @return              True iff the user can delete the checkpoint
	 */
	public boolean canDeleteCheckpoint(Long checkpointId) {
		return canDeleteCheckpointInEdition(
				checkpointRepository.findByIdOrThrow(checkpointId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can create a checkpoint in an edition
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can create the checkpoint
	 */
	public boolean canCreateCheckpointInEdition(Long editionId) {
		return canEditCheckpointInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can create a path in edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can create a path in an edition.
	 */
	public boolean canCreatePathInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can modify elements relating to paths in edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user can modify path related elements in edition.
	 */
	public boolean canEditPathInEdition(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
	}

	/**
	 * Gets whether the authenticated user can delete a path
	 *
	 * @param  pathId The id of the path
	 * @return        True iff the user can delete the path
	 */
	public boolean canDeletePath(Long pathId) {
		return canEditPathInEdition(
				pathRepository.findByIdOrThrow(pathId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can edit a path
	 *
	 * @param  pathId The id of the path
	 * @return        True iff the user can edit the path
	 */
	public boolean canEditPath(Long pathId) {
		return canEditPathInEdition(
				pathRepository.findByIdOrThrow(pathId).getEdition().getId());
	}

	/**
	 * Gets whether the authenticated user can view both tasks in and out of a path.
	 *
	 * @param  editionId The edition id.
	 * @return           True iff the user can view tasks not in path.
	 */
	public boolean canViewThroughPath(Long editionId) {
		return isAtLeastHeadTAInEdition(editionId);
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
		return roleCache.get(new RoleId().editionId(editionId).personId(getAuthenticatedPerson().getId()))
				.map(RoleDetailsDTO::getType).orElse(null);
	}

	/**
	 * Checks whether the authenticated user is at least a teacher in a course.
	 *
	 * @param  courseId The id of the course
	 * @return          True iff the user is an admin or teacher in the course
	 */
	public boolean isAtLeastTeacherInCourse(Long courseId) {
		if (isAdmin()) {
			return true;
		}
		CourseDetailsDTO course = courseApi.getCourseById(courseId).block();

		return course.getEditions().stream().anyMatch(e -> getRoleInEdition(e.getId()) == TEACHER);
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
		if (isAdmin()) {
			return true;
		}
		var role = getRoleInEdition(editionId);
		return role == TEACHER || role == HEAD_TA;
	}

	/**
	 * Checks whether the authenticated user is a head TA in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is a head TA in the edition
	 */
	public boolean isHeadTAInEdition(Long editionId) {
		return getRoleInEdition(editionId) == HEAD_TA;
	}

	/**
	 * Checks whether the authenticated user is at least a TA in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is an admin, teacher, head TA, or TA in the edition
	 */
	public boolean isAtLeastTAInEdition(Long editionId) {
		if (isAdmin()) {
			return true;
		}
		var role = getRoleInEdition(editionId);
		return role == TEACHER || role == HEAD_TA || role == TA;
	}

	/**
	 * Checks whether the authenticated user is at least a student in an edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff the user is an admin, teacher, head TA, TA or student in the edition
	 */
	public boolean isAtLeastStudentInEdition(Long editionId) {
		if (isAdmin()) {
			return true;
		}
		var role = getRoleInEdition(editionId);
		return role != null && role != BLOCKED && role != TEACHER_RO;
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

	/**
	 * Checks whether the authenticated user can get the editions of a course.
	 *
	 * @param  courseId The id of the course
	 * @return          True iff true user can get the editions of the course
	 */
	public boolean canGetEditionsOfCourse(Long courseId) {
		return isManagerAnywhere();
	}

	/**
	 * Checks whether the authenticated user can get the modules of a edition.
	 *
	 * @param  editionId The id of the edition
	 * @return           True iff true user can get the modules of the edition
	 */
	public boolean canGetModulesOfEdition(Long editionId) {
		return isManagerAnywhere();
	}

	/**
	 * Checks whether the authenticated user can get the skills of a module.
	 *
	 * @param  moduleId The id of the module
	 * @return          True iff true user can get the skills of the module
	 */
	public boolean canGetSkillsOfModule(Long moduleId) {
		return isManagerAnywhere();
	}

}
