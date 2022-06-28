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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.SkillRepository;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class CheckpointControllerTest extends ControllerTest {

	private final CheckpointRepository checkpointRepository;
	private final CheckpointController checkpointController;
	private final SkillRepository skillRepository;

	@Autowired
	public CheckpointControllerTest(CheckpointRepository checkpointRepository,
			CheckpointController checkpointController, SkillRepository skillRepository) {
		this.checkpointRepository = checkpointRepository;
		this.checkpointController = checkpointController;
		this.skillRepository = skillRepository;
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
