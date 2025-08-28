// /*
//  * Skill Circuits
//  * Copyright (C) 2025 - Delft University of Technology
//  *
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License as
//  * published by the Free Software Foundation, either version 3 of the
//  * License, or (at your option) any later version.
//  *
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
//  */
// package nl.tudelft.skills.controller.old;
//
// import static org.mockito.ArgumentMatchers.anySet;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;
//
// import java.util.Random;
// import java.util.Set;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.TestInstance;
// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.ui.ExtendedModelMap;
// import org.springframework.ui.Model;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
//
// import nl.tudelft.labracore.api.RoleControllerApi;
// import nl.tudelft.labracore.api.dto.Id;
// import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
// import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
// import nl.tudelft.skills.test.TestDatabaseLoader;
// import reactor.core.publisher.Flux;
//
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// public abstract class ControllerTest {
//
// 	private static final Random random = new Random(42L);
//
// 	@Autowired
// 	protected ModelMapper mapper;
//
// 	@Autowired
// 	protected TestDatabaseLoader db;
//
// 	@Autowired
// 	protected MockMvc mvc;
//
// 	protected Model model;
// 	protected ObjectMapper objectMapper;
//
// 	@BeforeEach
// 	void initFields() {
// 		model = new ExtendedModelMap();
// 		objectMapper = new ObjectMapper();
// 	}
//
// 	protected Long randomId() {
// 		return random.nextLong(1000000000L);
// 	}
//
// 	protected <T> T map(Object source, Class<T> dest) {
// 		return mapper.map(source, dest);
// 	}
//
// 	/**
// 	 * Mocks the response of the role api for a given role for any set of one edition.
// 	 *
// 	 * @param roleApi The roleApi.
// 	 * @param role    The role to return for the user.
// 	 */
// 	protected void mockRole(RoleControllerApi roleApi, String role) {
// 		if (role == null || role.isBlank()) {
// 			when(roleApi.getRolesById(anySet(), anySet())).thenReturn(Flux.empty());
// 		} else {
// 			when(roleApi.getRolesById(anySet(), anySet()))
// 					.thenReturn(Flux.just(new RoleDetailsDTO()
// 							.id(new Id().editionId(db.getEditionRL().getId())
// 									.personId(db.getPerson().getId()))
// 							.person(new PersonSummaryDTO().id(db.getPerson().getId()).username("username"))
// 							.type(RoleDetailsDTO.TypeEnum.valueOf(role))));
// 		}
// 	}
//
// 	/**
// 	 * Mocks the response of the role api for a given role for a specific edition.
// 	 *
// 	 * @param roleApi The roleApi.
// 	 * @param role    The role to return for the user.
// 	 * @param edition The edition for which to return this role.
// 	 */
// 	protected void mockRoleForEdition(RoleControllerApi roleApi, String role, Long edition) {
// 		if (role == null || role.isBlank()) {
// 			when(roleApi.getRolesById(eq(Set.of(edition)), anySet())).thenReturn(Flux.empty());
// 		} else {
// 			when(roleApi.getRolesById(eq(Set.of(edition)), anySet()))
// 					.thenReturn(Flux.just(new RoleDetailsDTO()
// 							.id(new Id().editionId(edition)
// 									.personId(db.getPerson().getId()))
// 							.person(new PersonSummaryDTO().id(db.getPerson().getId()).username("username"))
// 							.type(RoleDetailsDTO.TypeEnum.valueOf(role))));
// 		}
// 	}
//
// }
