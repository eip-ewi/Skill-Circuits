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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.id.SubmoduleIdDTO;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.dto.patch.SkillPositionPatchDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.repository.CheckpointRepository;
import nl.tudelft.skills.repository.SkillRepository;
import nl.tudelft.skills.repository.SubmoduleRepository;
import nl.tudelft.skills.repository.TaskRepository;
import nl.tudelft.skills.service.SkillService;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class SkillControllerTest extends ControllerTest {

	private final SkillController skillController;
	private final SkillRepository skillRepository;
	private final SubmoduleRepository submoduleRepository;
	private final CheckpointRepository checkpointRepository;

	@Autowired
	public SkillControllerTest(SkillRepository skillRepository, TaskRepository taskRepository,
			SkillService skillService, SubmoduleRepository submoduleRepository,
			CheckpointRepository checkpointRepository) {
		this.submoduleRepository = submoduleRepository;
		this.checkpointRepository = checkpointRepository;
		this.skillController = new SkillController(skillRepository, taskRepository, submoduleRepository,
				skillService);
		this.skillRepository = skillRepository;
	}

	@Test
	@WithUserDetails("admin")
	void createSkill() throws Exception {
		String element = mvc.perform(post("/skill").with(csrf())
				.content(EntityUtils.toString(new UrlEncodedFormEntity(List.of(
						new BasicNameValuePair("name", "Skill"),
						new BasicNameValuePair("submodule.id", Long.toString(db.getSubmoduleCases().getId())),
						new BasicNameValuePair("checkpoint.id",
								Long.toString(db.getCheckpointLectureOne().getId())),
						new BasicNameValuePair("row", "10"),
						new BasicNameValuePair("column", "11")))))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher idMatcher = Pattern.compile("id=\"block-(\\d+)\"").matcher(element);
		assertThat(idMatcher.find()).isTrue();

		Long id = Long.parseLong(idMatcher.group(1));
		assertThat(skillRepository.existsById(id)).isTrue();

		assertThat(element)
				.contains("<span id=\"block-" + id + "-name\">Skill</span>")
				.contains("style=\"grid-row: 11; grid-column: 12");
	}

	@Test
	void patchSkill() {
		skillController.patchSkill(SkillPatchDTO.builder()
				.id(db.getSkillVariables().getId())
				.name("Updated")
				.submodule(new SubmoduleIdDTO(db.getSubmoduleCases().getId()))
				.build(), model);

		Skill skill = db.getSkillVariables();
		assertThat(skill.getName()).isEqualTo("Updated");
		assertThat(submoduleRepository.findByIdOrThrow(skill.getSubmodule().getId())).isEqualTo(
				submoduleRepository.findByIdOrThrow(db.getSubmoduleCases().getId()));
	}

	@Test
	void updateSkillPosition() {
		skillController.updateSkillPosition(db.getSkillVariables().getId(), SkillPositionPatchDTO.builder()
				.column(10)
				.row(11)
				.build());

		Skill skill = db.getSkillVariables();
		assertThat(skill.getColumn()).isEqualTo(10);
		assertThat(skill.getRow()).isEqualTo(11);
	}

	@Test
	void deleteSkill() {
		Long id = db.getSkillVariables().getId();
		skillController.deleteSkill(id, "block");
		assertThat(skillRepository.existsById(id)).isFalse();
	}

	@Test
	void connectSkill() {
		assertThat(db.getSkillImplication().getChildren())
				.doesNotContain(db.getSkillProofOutline());
		skillController.connectSkill(db.getSkillImplication().getId(), db.getSkillProofOutline().getId());
		assertThat(db.getSkillImplication().getChildren())
				.contains(db.getSkillProofOutline());
	}

	@Test
	void disconnectSkill() {
		assertThat(db.getSkillImplication().getChildren())
				.contains(db.getSkillAssumption());
		skillController.disconnectSkill(db.getSkillImplication().getId(), db.getSkillAssumption().getId());
		assertThat(db.getSkillImplication().getChildren())
				.doesNotContain(db.getSkillAssumption());
	}

	@Test
	void endpointsAreProtected() throws Exception {
		mvc.perform(patch("/skill/{id}", db.getSkillVariables().getId()))
				.andExpect(status().isForbidden());
		mvc.perform(post("/skill"))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/skill?id=1"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/skill/connect/1/2"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/skill/disconnect/1/2"))
				.andExpect(status().isForbidden());
	}

}
