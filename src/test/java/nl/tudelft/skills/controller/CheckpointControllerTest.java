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
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("name", "checkpoint"),
						new BasicNameValuePair("edition.id", Long.toString(db.getEditionRL().getId())),
						new BasicNameValuePair("moduleId",
								Long.toString(db.getModuleProofTechniques().getId())),
						new BasicNameValuePair("skillIds",
								db.skillImplication.getId() + "," + db.skillNegation.getId()),
						new BasicNameValuePair("deadline",
								LocalDateTime.of(2022, 1, 1, 1, 1)
										.format(DateTimeFormatter.ISO_DATE_TIME))))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrl("module/" + db.getModuleProofTechniques().getId()));

		Optional<Checkpoint> checkpoint = checkpointRepository.findAll().stream()
				.filter(cp -> cp.getName().equals("checkpoint")).findFirst();
		assertThat(checkpoint).isNotEmpty();

		assertThat(skillRepository.findAll().stream()
				.filter(skill -> skill.getCheckpoint().equals(checkpoint.get())))
						.containsExactlyInAnyOrder(db.skillImplication, db.skillNegation);
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
								db.skillImplication.getId() + "," + db.skillNegation.getId()),
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
						new BasicNameValuePair("id", Long.toString(db.checkpointLectureOne.getId())),
						new BasicNameValuePair("name", "edited")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk());

		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.checkpointLectureOne.getId());
		assertThat(checkpoint.getName()).isEqualTo("edited");
	}

	@Test
	public void patchCheckpointIsForbidden() throws Exception {
		mvc.perform(patch("/checkpoint").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("id", Long.toString(db.checkpointLectureOne.getId())),
						new BasicNameValuePair("name", "edited")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(redirectedUrlPattern("**/auth/login"));

		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.checkpointLectureOne.getId());
		assertThat(checkpoint.getName()).isEqualTo("Lecture 1");
	}

	@Test
	@WithUserDetails("admin")
	public void deleteCheckpoint() {
		checkpointController.deleteCheckpoint(db.checkpointLectureOne.getId());

		Optional<Checkpoint> checkpoint = checkpointRepository.findById(db.checkpointLectureOne.getId());
		assertThat(checkpoint).isEmpty();
	}

	@Test
	@WithUserDetails("admin")
	public void deleteLastCheckpointForbidden() {
		var res = checkpointController.deleteCheckpoint(db.checkpointLectureTwo.getId());

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.CONFLICT));

		Optional<Checkpoint> checkpoint = checkpointRepository.findById(db.checkpointLectureTwo.getId());
		assertThat(checkpoint).isNotEmpty();
	}

	@Test
	@WithUserDetails("admin")
	public void addSkillsToCheckpoint() {
		var res = checkpointController.addSkillsToCheckpoint(db.checkpointLectureTwo.getId(),
				List.of(db.skillImplication.getId()));

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		assertThat(skillRepository.findByIdOrThrow(db.skillImplication.getId()).getCheckpoint())
				.isEqualTo(db.checkpointLectureTwo);
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.checkpointLectureTwo.getId());
		assertThat(checkpoint.getSkills()).contains(db.skillImplication);
	}

	@Test
	@WithUserDetails("admin")
	public void deleteSomeSkillsFromCheckpoint() {
		var res = checkpointController.deleteSkillsFromCheckpoint(db.checkpointLectureOne.getId(),
				List.of(db.skillProofOutline.getId()));

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		//skill gets next checkpoint
		assertThat(skillRepository.findByIdOrThrow(db.skillProofOutline.getId()).getCheckpoint())
				.isEqualTo(db.checkpointLectureTwo);
		// Checkpoint still exists
		Checkpoint checkpoint = checkpointRepository.findByIdOrThrow(db.checkpointLectureTwo.getId());
		assertThat(checkpoint.getSkills()).doesNotContain(db.skillProofOutline);
	}

	@Test
	@WithUserDetails("admin")
	public void deleteAllSkillsFromCheckpoint() {
		var res = checkpointController.deleteSkillsFromCheckpoint(db.checkpointLectureOne.getId(),
				checkpointRepository.findByIdOrThrow(db.checkpointLectureOne.getId()).getSkills().stream()
						.map(Skill::getId).toList());

		assertThat(res).isEqualTo(new ResponseEntity<>(HttpStatus.OK));

		assertThat(skillRepository
				.findAllByIdIn(Set.of(db.skillImplication.getId(), db.skillNegation.getId(),
						db.skillVariables.getId(), db.skillProofOutline.getId(),
						db.skillAssumption.getId()))
				.stream().map(Skill::getCheckpoint))
						.allSatisfy(cp -> assertThat(cp).isEqualTo(db.checkpointLectureTwo));
		assertThat(skillRepository.findByIdOrThrow(db.skillProofOutline.getId()).getCheckpoint())
				.isEqualTo(db.checkpointLectureTwo);
		// Checkpoint is deleted
		assertThat(checkpointRepository.findById(db.checkpointLectureOne.getId())).isEmpty();
	}

	@Test
	public void deleteCheckpointIsForbidden() throws Exception {
		mvc.perform(delete("/checkpoint/" + db.checkpointLectureOne.getId()))
				.andExpect(status().isForbidden());
	}

}
