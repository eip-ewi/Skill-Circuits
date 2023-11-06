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
package nl.tudelft.skills.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.Id;
import nl.tudelft.labracore.api.dto.PersonSummaryDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.test.TestUserDetailsService;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CheckpointControllerTest extends ControllerTest {

	private final CheckpointRepository checkpointRepository;
	private final CheckpointController checkpointController;
	private final SkillRepository skillRepository;
	private final EditionRepository editionRepository;
	private final RoleControllerApi roleApi;

	@Autowired
	public CheckpointControllerTest(CheckpointRepository checkpointRepository,
			CheckpointController checkpointController, SkillRepository skillRepository,
			EditionRepository editionRepository, RoleControllerApi roleApi) {
		this.checkpointRepository = checkpointRepository;
		this.checkpointController = checkpointController;
		this.skillRepository = skillRepository;
		this.editionRepository = editionRepository;
		this.roleApi = roleApi;
	}

	@Test
	@WithUserDetails("admin")
	public void createCheckpoint() throws Exception {
		mvc.perform(post("/checkpoint").with(csrf())
				.content(getCreateCheckpointFormData())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrl("module/" + db.getModuleProofTechniques().getId()));

		Optional<Checkpoint> checkpoint = checkpointRepository.findAll().stream()
				.filter(cp -> cp.getName().equals("checkpoint")).findFirst();
		assertThat(checkpoint).isNotEmpty();

		assertThat(skillRepository.findAll().stream()
				.filter(skill -> skill.getCheckpoint().equals(checkpoint.get())))
						.containsExactlyInAnyOrder(
								db.getSkillImplication(),
								db.getSkillNegation());
	}

	@Test
	@WithUserDetails("admin")
	public void createCheckpointSetup() throws Exception {
		mvc.perform(post("/checkpoint/setup").with(csrf())
				.content(getCreateCheckpointFormData())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED));

		Optional<Checkpoint> checkpoint = checkpointRepository.findAll().stream()
				.filter(cp -> cp.getName().equals("checkpoint")).findFirst();
		assertThat(checkpoint).isNotEmpty();
	}

	private String getCreateCheckpointFormData() throws Exception {
		return EntityUtils.toString(new UrlEncodedFormEntity(List.of(
				new BasicNameValuePair("name", "checkpoint"),
				new BasicNameValuePair("edition.id", Long.toString(db.getEditionRL().getId())),
				new BasicNameValuePair("moduleId",
						Long.toString(db.getModuleProofTechniques().getId())),
				new BasicNameValuePair("skillIds",
						db.getSkillImplication().getId() + "," + db.getSkillNegation().getId()),
				new BasicNameValuePair("deadline",
						LocalDateTime.of(2022, 1, 1, 1, 1)
								.format(DateTimeFormatter.ISO_DATE_TIME)))));
	}

	private String getChangeCheckpointFormData(Long moduleId, Long prevId, Long newId) throws Exception {
		return EntityUtils.toString(new UrlEncodedFormEntity(List.of(
				new BasicNameValuePair("moduleId", Long.toString(moduleId)),
				new BasicNameValuePair("prevId", Long.toString(prevId)),
				new BasicNameValuePair("newId", Long.toString(newId)))));
	}

	@Test
	public void createCheckpointForbidden() throws Exception {
		mvc.perform(post("/checkpoint").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("name", "checkpoint"),
						new BasicNameValuePair("edition.id", Long.toString(db.getEditionRL().getId())),
						new BasicNameValuePair("moduleId",
								Long.toString(db.getModuleProofTechniques().getId())),
						new BasicNameValuePair("skillIds",
								db.getSkillImplication().getId() + "," + db.getSkillNegation().getId()),
						new BasicNameValuePair("deadline",
								LocalDateTime.of(2022, 1, 1, 1, 1)
										.format(DateTimeFormatter.ISO_DATE_TIME))))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrlPattern("**/auth/login"));

		assertThat(checkpointRepository.findAll().stream().filter(cp -> cp.getName().equals("checkpoint")))
				.isEmpty();
	}

	@Test
	@WithUserDetails("admin")
	public void patchCheckpoint() throws Exception {
		mvc.perform(patch("/checkpoint").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("id", Long.toString(db.getCheckpointLectureOne().getId())),
						new BasicNameValuePair("name", "edited"),
						new BasicNameValuePair("deadline", "2022-12-21T23:59")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk());

		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId());
		assertThat(checkpoint.getName()).isEqualTo("edited");
		assertThat(checkpoint.getDeadline()).isEqualTo(LocalDateTime.of(2022, 12, 21, 23, 59));
	}

	@ParameterizedTest
	@WithUserDetails("username")
	@CsvSource({ "TEACHER,200,editedName", "HEAD_TA,403,uneditedName", "TA,403,uneditedName",
			"STUDENT,403,uneditedName", ",403,uneditedName" })
	public void patchCheckpointName(String role, int status, String expectedName) throws Exception {
		Checkpoint checkpoint = checkpointRepository.save(Checkpoint.builder().name("uneditedName")
				.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT)))
				.edition(db.getEditionRL()).build());
		mockRole(roleApi, role);

		mvc.perform(patch("/checkpoint/name").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("id", Long.toString(checkpoint.getId())),
						new BasicNameValuePair("name", "editedName")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().is(status));

		Checkpoint checkpointNew = checkpointRepository.findByIdOrThrow(checkpoint.getId());
		assertThat(checkpointNew.getName()).isEqualTo(expectedName);
	}

	@Test
	public void patchCheckpointNameNotLoggedIn() throws Exception {
		mvc.perform(patch("/checkpoint/name").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("id", Long.toString(db.getCheckpointLectureOne().getId())),
						new BasicNameValuePair("name", "editedName")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrlPattern("**/auth/login"));

		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId());
		assertThat(checkpoint.getName()).isEqualTo("Lecture 1");
	}

	@Test
	public void patchCheckpointIsForbidden() throws Exception {
		mvc.perform(patch("/checkpoint").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("id", Long.toString(db.getCheckpointLectureOne().getId())),
						new BasicNameValuePair("name", "edited"),
						new BasicNameValuePair("deadline", "2022-12-21T23:59")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrlPattern("**/auth/login"));

		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId());
		assertThat(checkpoint.getName()).isEqualTo("Lecture 1");
	}

	@Test
	@WithUserDetails("username")
	public void changeCheckpointForbidden() throws Exception {
		Set<Skill> checkpointOneSkills = db.getCheckpointLectureOne().getSkills();
		Set<Skill> checkpointTwoSkills = db.getCheckpointLectureTwo().getSkills();

		// If fetch for the cache is called, the roleApi is called, which should return the student role
		when(roleApi.getRolesById(Set.of(db.getEditionRL().getId()), Set.of(TestUserDetailsService.id)))
				.thenReturn(Flux.just(new RoleDetailsDTO()
						.id(new Id().editionId(db.getEditionRL().getId())
								.personId(TestUserDetailsService.id))
						.person(new PersonSummaryDTO().id(TestUserDetailsService.id).username("username"))
						.type(RoleDetailsDTO.TypeEnum.valueOf("STUDENT"))));

		// Prepare and send request
		mvc.perform(patch("/checkpoint/change-checkpoint").with(csrf())
				.content(getChangeCheckpointFormData(db.getModuleProofTechniques().getId(),
						db.getCheckpointLectureOne().getId(), db.getCheckpointLectureTwo().getId()))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isForbidden());

		// Assert that the skills in checkpoints are still the same
		assertThat(checkpointRepository.findAll()).hasSize(2);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId()).getSkills())
				.containsAll(checkpointOneSkills);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureTwo().getId()).getSkills())
				.containsAll(checkpointTwoSkills);
	}

	@Test
	@WithUserDetails("admin")
	public void changeCheckpointInvalidSomeSkillsInModule() throws Exception {
		Set<Skill> checkpointOneSkills = db.getCheckpointLectureOne().getSkills();
		Set<Skill> checkpointTwoSkills = db.getCheckpointLectureTwo().getSkills();

		// Prepare and send request
		// Expect a redirection to the page, which will also be returned if the request is invalid
		mvc.perform(patch("/checkpoint/change-checkpoint").with(csrf())
				.content(getChangeCheckpointFormData(db.getModuleProofTechniques().getId(),
						db.getCheckpointLectureOne().getId(), db.getCheckpointLectureTwo().getId()))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrl("/module/" + db.getModuleProofTechniques().getId()));

		// Assert that the skills in checkpoints are still the same (nothing should have changed since
		// the checkpoint for lecture 2 contains some skills in the module already)
		assertThat(checkpointRepository.findAll()).hasSize(2);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId()).getSkills())
				.containsAll(checkpointOneSkills);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureTwo().getId()).getSkills())
				.containsAll(checkpointTwoSkills);
	}

	@Test
	@WithUserDetails("admin")
	public void changeCheckpointInvalidDifferentEditions() throws Exception {
		Set<Skill> checkpointOneSkills = db.getCheckpointLectureOne().getSkills();
		Set<Skill> checkpointTwoSkills = db.getCheckpointLectureTwo().getSkills();

		// Create a new checkpoint in different edition
		SCEdition edition = editionRepository.save(SCEdition.builder().id(1L).build());
		Checkpoint checkpoint = checkpointRepository.save(Checkpoint.builder()
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 100), LocalTime.MIDNIGHT))
				.name("Checkpoint").edition(edition).build());
		edition.setCheckpoints(Set.of(checkpoint));

		// Prepare and send request
		// Expect a redirection to the page, which will also be returned if the request is invalid
		mvc.perform(patch("/checkpoint/change-checkpoint").with(csrf())
				.content(getChangeCheckpointFormData(db.getModuleProofTechniques().getId(),
						db.getCheckpointLectureOne().getId(), checkpoint.getId()))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrl("/module/" + db.getModuleProofTechniques().getId()));

		// Assert that the skills in checkpoints are still the same (nothing should have changed since the
		// checkpoint is in a different edition)
		assertThat(checkpointRepository.findAll()).hasSize(3);
		assertThat(checkpointRepository.findByIdOrThrow(checkpoint.getId()).getSkills())
				.isEmpty();
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId()).getSkills())
				.containsAll(checkpointOneSkills);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureTwo().getId()).getSkills())
				.containsAll(checkpointTwoSkills);
	}

	@Test
	@WithUserDetails("admin")
	public void changeCheckpointValid() throws Exception {
		Set<Skill> checkpointOneSkills = db.getCheckpointLectureOne().getSkills();
		Set<Skill> checkpointTwoSkills = db.getCheckpointLectureTwo().getSkills();

		// Create a new checkpoint (contains no skills in this module)
		Checkpoint checkpoint = checkpointRepository.save(Checkpoint.builder()
				.deadline(LocalDateTime.of(LocalDate.ofYearDay(2022, 100), LocalTime.MIDNIGHT))
				.name("Checkpoint").edition(db.getEditionRL()).build());

		// Prepare and send request, expect a redirection to the page
		mvc.perform(patch("/checkpoint/change-checkpoint").with(csrf())
				.content(getChangeCheckpointFormData(db.getModuleProofTechniques().getId(),
						db.getCheckpointLectureOne().getId(), checkpoint.getId()))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrl("/module/" + db.getModuleProofTechniques().getId()));

		// Assert that the skills are now changed to be in the new checkpoint
		assertThat(checkpointRepository.findAll()).hasSize(3);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureOne().getId()).getSkills())
				.isEmpty();
		assertThat(checkpointRepository.findByIdOrThrow(checkpoint.getId()).getSkills())
				.containsAll(checkpointOneSkills);
		assertThat(checkpointRepository.findByIdOrThrow(db.getCheckpointLectureTwo().getId()).getSkills())
				.containsAll(checkpointTwoSkills);
	}

	@Test
	@WithUserDetails("admin")
	public void deleteCheckpoint() {
		Long checkpointId = db.getCheckpointLectureOne().getId();
		checkpointController.deleteCheckpoint(checkpointId);

		Optional<Checkpoint> checkpoint = checkpointRepository.findById(checkpointId);
		assertThat(checkpoint).isEmpty();
	}

	@Test
	@WithUserDetails("admin")
	public void deleteLastCheckpointForbidden() {
		var res = checkpointController.deleteCheckpoint(db.getCheckpointLectureTwo().getId());

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.CONFLICT));

		Optional<Checkpoint> checkpoint = checkpointRepository.findById(db.getCheckpointLectureTwo().getId());
		assertThat(checkpoint).isNotEmpty();
	}

	@Test
	@WithUserDetails("admin")
	public void setSkillsForCheckpoint() {
		var res = checkpointController.setSkillsForCheckpoint(db.getCheckpointLectureTwo().getId(),
				List.of(db.getSkillImplication().getId()));

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		assertThat(db.getSkillImplication().getCheckpoint())
				.isEqualTo(db.getCheckpointLectureTwo());
		Checkpoint checkpoint = db.getCheckpointLectureTwo();
		assertThat(checkpoint.getSkills()).containsExactly(db.getSkillImplication());
	}

	@Test
	@WithUserDetails("admin")
	public void addSkillsToCheckpoint() {
		var res = checkpointController.addSkillsToCheckpoint(db.getCheckpointLectureTwo().getId(),
				List.of(db.getSkillImplication().getId()));

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		assertThat(db.getSkillImplication().getCheckpoint())
				.isEqualTo(db.getCheckpointLectureTwo());
		Checkpoint checkpoint = db.getCheckpointLectureTwo();
		assertThat(checkpoint.getSkills()).contains(db.getSkillImplication());
	}

	@Test
	@WithUserDetails("admin")
	public void deleteSomeSkillsFromCheckpoint() {
		var res = checkpointController.deleteSkillsFromCheckpoint(db.getCheckpointLectureOne().getId(),
				List.of(db.getSkillProofOutline().getId()));

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		//skill gets next checkpoint
		assertThat(db.getSkillProofOutline().getCheckpoint())
				.isEqualTo(db.getCheckpointLectureTwo());
		// Checkpoint still exists
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.getCheckpointLectureTwo().getId());
		assertThat(checkpoint.getSkills()).doesNotContain(db.getSkillProofOutline());
	}

	@Test
	@WithUserDetails("admin")
	public void deleteAllSkillsFromCheckpoint() {
		Long checkpointLectureOneId = db.getCheckpointLectureOne().getId();
		var res = checkpointController.deleteSkillsFromCheckpoint(checkpointLectureOneId,
				db.getCheckpointLectureOne().getSkills().stream()
						.map(Skill::getId).toList());

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		assertThat(skillRepository
				.findAllByIdIn(Set.of(db.getSkillImplication().getId(), db.getSkillNegation().getId(),
						db.getSkillVariables().getId(), db.getSkillProofOutline().getId(),
						db.getSkillAssumption().getId()))
				.stream().map(Skill::getCheckpoint))
						.allSatisfy(cp -> assertThat(cp).isEqualTo(db.getCheckpointLectureTwo()));
		assertThat(db.getSkillProofOutline().getCheckpoint())
				.isEqualTo(db.getCheckpointLectureTwo());
		// Checkpoint is deleted
		assertThat(checkpointRepository.findById(checkpointLectureOneId)).isEmpty();
	}

	@Test
	public void deleteCheckpointIsForbidden() throws Exception {
		mvc.perform(delete("/checkpoint/" + db.getCheckpointLectureOne().getId()))
				.andExpect(status().isForbidden());
	}

}
