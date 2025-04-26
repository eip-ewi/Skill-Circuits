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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.cache.RoleCacheManager;
import nl.tudelft.skills.model.ExternalSkill;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class AuthorisationServiceTest {

	@Autowired
	private TestDatabaseLoader db;

	private final AuthorisationService authorisationService;

	private final RoleCacheManager roleCacheManager;
	private final RoleControllerApi roleApi;
	private final CourseControllerApi courseApi;

	private final EditionRepository editionRepository;
	private final ModuleRepository moduleRepository;
	private final SubmoduleRepository submoduleRepository;
	private final SkillRepository skillRepository;
	private final TaskRepository taskRepository;
	private final CheckpointRepository checkpointRepository;
	private final PathRepository pathRepository;
	private final AbstractSkillRepository abstractSkillRepository;
	private final ExternalSkillRepository externalSkillRepository;
	private final PersonControllerApi personApi;

	@Autowired
	public AuthorisationServiceTest(RoleCacheManager roleCacheManager,
			AuthorisationService authorisationService,
			RoleControllerApi roleApi,
			EditionRepository editionRepository,
			ModuleRepository moduleRepository,
			SubmoduleRepository submoduleRepository,
			SkillRepository skillRepository,
			TaskRepository taskRepository, CheckpointRepository checkpointRepository,
			PathRepository pathRepository,
			AbstractSkillRepository abstractSkillRepository,
			ExternalSkillRepository externalSkillRepository,
			PersonControllerApi personApi) {
		this.authorisationService = authorisationService;
		this.roleCacheManager = roleCacheManager;
		this.roleApi = roleApi;

		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.submoduleRepository = submoduleRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;
		this.pathRepository = pathRepository;
		this.abstractSkillRepository = abstractSkillRepository;
		this.externalSkillRepository = externalSkillRepository;

		this.courseApi = mock(CourseControllerApi.class);
		this.personApi = personApi;
	}

	@Test
	@WithUserDetails("username")
	void getAuthPerson() {
		assertThat(authorisationService.getAuthPerson().getUsername()).isEqualTo("username");
	}

	@Test
	@WithUserDetails("username")
	void isAuthenticated() {
		assertThat(authorisationService.isAuthenticated()).isTrue();
	}

	@Test
	@WithUserDetails("admin")
	void isAdmin() {
		assertThat(authorisationService.isAdmin()).isTrue();
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isNotAdmin(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAdmin()).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "ADMIN,false", "TEACHER,false", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void isHeadTA(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isHeadTAInEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@Test
	@WithUserDetails("teacher")
	void isStaff() {
		assertThat(authorisationService.isStaff()).isTrue();
	}

	@Test
	@WithUserDetails("username")
	void isNotStaff() {
		assertThat(authorisationService.isStaff()).isFalse();
	}

	@Test
	@WithUserDetails("username")
	void isStudent() {
		assertThat(authorisationService.isStudent()).isTrue();
	}

	@Test
	@WithUserDetails("teacher")
	void isNotStudentButTeacher() {
		assertThat(authorisationService.isStudent()).isFalse();
	}

	@Test
	@WithUserDetails("admin")
	void isNotStudentButAdmin() {
		assertThat(authorisationService.isStudent()).isFalse();
	}

	@Test
	@WithAnonymousUser
	void cannotViewIfNotAuthenticated() {
		assertThat(authorisationService.canViewCourse(db.getCourseRL().getId())).isFalse();
		assertThat(authorisationService.canViewEdition(db.getEditionRL().getId())).isFalse();
		assertThat(authorisationService.canViewModule(db.getModuleProofTechniques().getId())).isFalse();
		assertThat(authorisationService.canViewSkill(db.getSkillAssumption().getId())).isFalse();

		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());
		assertThat(authorisationService.canViewSkill(externalSkill.getId())).isFalse();
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canViewCourse(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository,
				taskRepository,
				checkpointRepository, pathRepository,
				abstractSkillRepository,
				courseApi, personApi);
		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().id(db.getCourseRL().getId())
						.editions(List.of(new EditionSummaryDTO().id(db.getEditionRL().getId())))));

		assertThat(authorisationService.canViewCourse(db.getCourseRL().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canViewEdition(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository,
				taskRepository,
				checkpointRepository, pathRepository, abstractSkillRepository,
				courseApi, personApi);

		assertThat(authorisationService.canViewEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",true" })
	void canViewEditionWithVisibility(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository,
				taskRepository,
				checkpointRepository, pathRepository, abstractSkillRepository,
				courseApi, personApi);

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		assertThat(authorisationService.canViewEdition(edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canViewModule(String role, boolean expected) {
		mockRole(role);

		assertThat(authorisationService.canViewModule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",true" })
	void canViewModuleWithVisibility(String role, boolean expected) {
		mockRole(role);

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		assertThat(authorisationService.canViewModule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canViewTask(String role, boolean expected) {
		mockRole(role);

		assertThat(authorisationService.canViewTask(db.getTaskDo11ad().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",true" })
	void canViewTaskWithVisibility(String role, boolean expected) {
		mockRole(role);

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		assertThat(authorisationService.canViewTask(db.getTaskDo11ad().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canViewSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canViewSkill(db.getSkillAssumption().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",true" })
	void canViewSkillWithVisibility(String role, boolean expected) {
		mockRole(role);
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);
		assertThat(authorisationService.canViewSkill(db.getSkillAssumption().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canViewSkillExternalSameEdition(String role, boolean expected) {
		mockRole(role);
		ExternalSkill externalSkill = db.createExternalSkill(db.getSkillAssumption());
		assertThat(authorisationService.canViewSkill(externalSkill.getId())).isEqualTo(expected);
	}

	@Test
	@WithUserDetails("username")
	void canViewSkillExternalOtherEdition() {
		// Since this is a bigger test, it is only checked with one role (the student role).
		// This checks that the correct submodule is retrieved if it is an external skill (not using the overridden
		// method in the ExternalSkill class).

		mockRole("STUDENT");

		// Set visibility of this edition
		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		// Create an edition which is invisible, and a skill it contains to which the external skill
		// should link to
		Skill skill = db.createSkillInEditionHelper(db.getEditionRL().getId() + 1, false);
		ExternalSkill externalSkill = db.createExternalSkill(skill);

		assertThat(authorisationService.canViewSkill(externalSkill.getId())).isEqualTo(true);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canPublishEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canPublishEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteCreateInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateModuleInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteModuleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteModuleInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteModule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteModule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditModuleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditModuleInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditModule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditModule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreateSubmoduleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSubmoduleInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreateSubmodule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSubmodule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditSubmoduleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSubmoduleInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditSubmodule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSubmodule(db.getSubmoduleCases().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSubmoduleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSubmoduleInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSubmodule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSubmodule(db.getSubmoduleCases().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreateSkillInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSkillInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreateSkillInModule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSkillInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreateSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSkill(db.getSubmoduleCases().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditSkillInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSkillInEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSkill(db.getSkillAssumption().getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditExternalSkill(String role, boolean expected) {
		Long id = externalSkillRepository.save(ExternalSkill.builder()
				.module(db.getModuleProofTechniques())
				.skill(db.getSkillAssumption())
				.row(1)
				.column(1)
				.build()).getId();
		mockRole(role);
		assertThat(authorisationService.canEditSkill(id)).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSkillInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSkillInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSkill(db.getSkillAssumption().getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteExternalSkill(String role, boolean expected) {
		Long id = externalSkillRepository.save(ExternalSkill.builder()
				.module(db.getModuleProofTechniques())
				.skill(db.getSkillAssumption())
				.row(1)
				.column(1)
				.build()).getId();
		mockRole(role);
		assertThat(authorisationService.canDeleteSkill(id)).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditTask(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditTask(db.getTaskDo10a().getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteTask(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteTask(db.getTaskDo10a().getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreateCheckpointInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateCheckpointInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditCheckpoint(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditCheckpoint(db.getCheckpointLectureOne().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeleteCheckpoint(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteCheckpoint(db.getCheckpointLectureOne().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canCreatePathInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreatePathInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditPathInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditPathInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canEditPath(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditPath(db.getPathFinderPath().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canDeletePath(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeletePath(db.getPathFinderPath().getId()))
				.isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void canViewThroughPath(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canViewThroughPath(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER", "HEAD_TA", "TA", "STUDENT" })
	void getRoleInEdition(String role) {
		mockRole(role);
		assertThat(authorisationService.getRoleInEdition(db.getEditionRL().getId()))
				.isEqualTo(RoleDetailsDTO.TypeEnum.valueOf(role));
	}

	@Test
	@WithUserDetails("username")
	void getNoRoleInEdition() {
		mockRole(null);
		assertThat(authorisationService.getRoleInEdition(db.getEditionRL().getId())).isEqualTo(null);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isAtLeastTeacherInCourse(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository,
				taskRepository,
				checkpointRepository, pathRepository, abstractSkillRepository,
				courseApi, personApi);

		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().id(db.getCourseRL().getId())
						.editions(List.of(new EditionSummaryDTO().id(db.getEditionRL().getId())))));
		assertThat(authorisationService.isAtLeastTeacherInCourse(db.getCourseRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isAtLeastTeacherInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastTeacherInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER_RO,true", "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isTeacherROInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isTeacherROInEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void isAtLeastHeadTAInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastHeadTAInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,false", ",false" })
	void isAtLeastTAInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastTAInEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",false" })
	void isAtLeastStudentInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastStudentInEdition(db.getEditionRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,true", ",false" })
	void isStudentInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isStudentInEdition(db.getEditionRL().getId())).isEqualTo(expected);
	}

	@Test
	@Transactional
	@WithUserDetails("teacher")
	void canGetEditionsOfCourse() {
		assertThat(authorisationService.canGetEditionsOfCourse(db.getCourseRL().getId())).isTrue();
	}

	@Test
	@Transactional
	@WithUserDetails("teacher")
	void canGetModulesOfEdition() {
		assertThat(authorisationService.canGetModulesOfEdition(db.getEditionRL().getId())).isTrue();
	}

	@Test
	@Transactional
	@WithUserDetails("teacher")
	void canGetSkillsOfModule() {
		assertThat(authorisationService.canGetSkillsOfModule(db.getModuleProofTechniques().getId())).isTrue();
	}

	private void mockRole(String role) {
		if (role == null || role.isBlank()) {
			when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.empty());
		} else {
			when(roleApi.getRolesById(anySet(), anySet()))
					.thenReturn(Flux.just(new RoleDetailsDTO()
							.id(new Id().editionId(db.getEditionRL().getId())
									.personId(TestUserDetailsService.id))
							.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
							.type(RoleDetailsDTO.TypeEnum.valueOf(role))));
		}
	}

}
