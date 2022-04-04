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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.cache.RoleCacheManager;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.test.TestDatabaseLoader;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

	@Autowired
	public AuthorisationServiceTest(RoleCacheManager roleCacheManager,
			AuthorisationService authorisationService,
			RoleControllerApi roleApi,
			EditionRepository editionRepository,
			ModuleRepository moduleRepository,
			SubmoduleRepository submoduleRepository,
			SkillRepository skillRepository,
			TaskRepository taskRepository, CheckpointRepository checkpointRepository) {
		this.authorisationService = authorisationService;
		this.roleCacheManager = roleCacheManager;
		this.roleApi = roleApi;

		this.editionRepository = editionRepository;
		this.moduleRepository = moduleRepository;
		this.submoduleRepository = submoduleRepository;
		this.skillRepository = skillRepository;
		this.taskRepository = taskRepository;
		this.checkpointRepository = checkpointRepository;

		this.courseApi = mock(CourseControllerApi.class);
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
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canViewCourse(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository, taskRepository,
				checkpointRepository,
				courseApi);
		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().id(db.getCourseRL().getId())
						.editions(List.of(new EditionSummaryDTO().id(db.edition.getId())))));

		assertThat(authorisationService.canViewCourse(db.getCourseRL().getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canViewEdition(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository, taskRepository,
				checkpointRepository,
				courseApi);

		assertThat(authorisationService.canViewEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",true" })
	void canViewEditionWithVisibility(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository, taskRepository,
				checkpointRepository,
				courseApi);

		SCEdition edition = db.getEditionRL();
		edition.setVisible(true);
		editionRepository.save(edition);

		assertThat(authorisationService.canViewEdition(edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canPublishEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canPublishEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteCreateInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateModuleInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteModuleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteModuleInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteModule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteModule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditModuleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditModuleInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditModule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditModule(db.getModuleProofTechniques().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canCreateSubmoduleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSubmoduleInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canCreateSubmodule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSubmodule(db.moduleProofTechniques.getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditSubmoduleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSubmoduleInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditSubmodule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSubmodule(db.submoduleCases.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSubmoduleInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSubmoduleInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSubmodule(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSubmodule(db.submoduleCases.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canCreateSkillInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSkillInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canCreateSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSkill(db.submoduleCases.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditSkillInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSkillInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSkill(db.skillAssumption.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSkillInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSkillInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSkill(db.skillAssumption.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canEditTask(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditTask(db.taskDo10a.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteTask(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteTask(db.taskDo10a.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER", "HEAD_TA", "TA", "STUDENT" })
	void getRoleInEdition(String role) {
		mockRole(role);
		assertThat(authorisationService.getRoleInEdition(db.edition.getId()))
				.isEqualTo(RoleDetailsDTO.TypeEnum.valueOf(role));
	}

	@Test
	@WithUserDetails("username")
	void getNoRoleInEdition() {
		mockRole(null);
		assertThat(authorisationService.getRoleInEdition(db.edition.getId())).isEqualTo(null);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isAtLeastTeacherInCourse(String role, boolean expected) {
		mockRole(role);

		AuthorisationService authorisationService = new AuthorisationService(roleCacheManager,
				editionRepository, moduleRepository, submoduleRepository, skillRepository, taskRepository,
				checkpointRepository,
				courseApi);

		when(courseApi.getCourseById(anyLong()))
				.thenReturn(Mono.just(new CourseDetailsDTO().id(db.getCourseRL().getId())
						.editions(List.of(new EditionSummaryDTO().id(db.edition.getId())))));
		assertThat(authorisationService.isAtLeastTeacherInCourse(db.getCourseRL().getId()))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isAtLeastTeacherInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastTeacherInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER_RO,true", "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void isTeacherROInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isTeacherROInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,false", "STUDENT,false", ",false" })
	void isAtLeastHeadTAInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastHeadTAInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,false", ",false" })
	void isAtLeastTAInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastTAInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,true", "TA,true", "STUDENT,true", ",false" })
	void isAtLeastStudentInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isAtLeastStudentInEdition(db.edition.getId())).isEqualTo(expected);
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,false", "HEAD_TA,false", "TA,false", "STUDENT,true", ",false" })
	void isStudentInEdition(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.isStudentInEdition(db.edition.getId())).isEqualTo(expected);
	}

	private void mockRole(String role) {
		if (role == null || role.isBlank()) {
			when(roleApi.getRolesById(anyList(), anyList())).thenReturn(Flux.empty());
		} else {
			when(roleApi.getRolesById(anyList(), anyList()))
					.thenReturn(Flux.just(new RoleDetailsDTO()
							.id(new Id().editionId(db.edition.getId()).personId(TestUserDetailsService.id))
							.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
							.type(RoleDetailsDTO.TypeEnum.valueOf(role))));
		}
	}

}
