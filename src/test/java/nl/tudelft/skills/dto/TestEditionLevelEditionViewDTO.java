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
package nl.tudelft.skills.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import nl.tudelft.librador.dto.view.View;
import nl.tudelft.skills.TestSkillCircuitsApplication;
import nl.tudelft.skills.dto.old.view.checkpoint.CheckpointViewDTO;
import nl.tudelft.skills.dto.old.view.edition.EditionLevelEditionViewDTO;
import nl.tudelft.skills.dto.old.view.edition.EditionLevelModuleViewDTO;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TestEditionLevelEditionViewDTO {
	private final TestDatabaseLoader db;

	@Autowired
	public TestEditionLevelEditionViewDTO(TestDatabaseLoader db) {
		this.db = db;
	}

	@Test
	public void testModuleOrder() {
		EditionLevelEditionViewDTO view = View.convert(db.getEditionRL(),
				EditionLevelEditionViewDTO.class);
		assertThat(view.getModulesAlphabetic()).isEqualTo(List.of(View.convert(db.getModule(),
				EditionLevelModuleViewDTO.class),
				View.convert(db.getModuleProofTechniques(),
						EditionLevelModuleViewDTO.class)));
	}

	@Test
	public void testGroupOrder() {
		EditionLevelEditionViewDTO view = View.convert(db.getEditionRL(),
				EditionLevelEditionViewDTO.class);
		assertThat(view.getGroups()).isEqualTo(List.of(View.convert(db.getModule(),
				EditionLevelModuleViewDTO.class),
				View.convert(db.getModuleProofTechniques(),
						EditionLevelModuleViewDTO.class)));
	}

	@Test
	public void testCheckpointOrder() {
		EditionLevelEditionViewDTO view = View.convert(db.getEditionRL(),
				EditionLevelEditionViewDTO.class);
		assertThat(view.getCheckpointsInEdition()).isEqualTo(List.of(
				View.convert(db.getCheckpointLectureOne(),
						CheckpointViewDTO.class),
				View.convert(db.getCheckpointLectureTwo(),
						CheckpointViewDTO.class)));
	}

}
