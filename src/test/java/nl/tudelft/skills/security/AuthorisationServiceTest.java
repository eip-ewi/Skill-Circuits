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
import static org.mockito.Mockito.when;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.Id;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.repository.SkillRepository;
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

@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class AuthorisationServiceTest {

	@Autowired
	private TestDatabaseLoader db;

	private final AuthorisationService authorisationService;

	private final RoleControllerApi roleApi;

	private final SkillRepository skillRepository;

	@Autowired
	public AuthorisationServiceTest(AuthorisationService authorisationService, RoleControllerApi roleApi,
			SkillRepository skillRepository) {
		this.authorisationService = authorisationService;
		this.roleApi = roleApi;
		this.skillRepository = skillRepository;
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
	void canCreateSkills(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canCreateSkills(db.edition.getId())).isEqualTo(expected);
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
	void canEditSkills(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canEditSkills(db.edition.getId())).isEqualTo(expected);
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
	void canDeleteSkills(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSkills(db.edition.getId())).isEqualTo(expected);
	}

	@Transactional
	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,true", "HEAD_TA,false", "TA,false", "STUDENT,false", ",false" })
	void canDeleteSkill(String role, boolean expected) {
		mockRole(role);
		assertThat(authorisationService.canDeleteSkill(db.skillAssumption.getId())).isEqualTo(expected);
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
