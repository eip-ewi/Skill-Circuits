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
import nl.tudelft.skills.dto.old.view.module.ModuleLevelModuleViewDTO;
import nl.tudelft.skills.dto.old.view.module.ModuleLevelSubmoduleViewDTO;
import nl.tudelft.skills.test.TestDatabaseLoader;

@Transactional
@SpringBootTest(classes = TestSkillCircuitsApplication.class)
public class TestModuleLevelModuleViewDTO {
	private final TestDatabaseLoader db;

	@Autowired
	public TestModuleLevelModuleViewDTO(TestDatabaseLoader db) {
		this.db = db;
	}

	@Test
	public void testGroupOrder() {
		ModuleLevelModuleViewDTO view = View.convert(db.getModuleProofTechniques(),
				ModuleLevelModuleViewDTO.class);
		assertThat(view.getGroups()).isEqualTo(List.of(View.convert(db.getSubmoduleCases(),
				ModuleLevelSubmoduleViewDTO.class),
				View.convert(db.getSubmoduleContradiction(),
						ModuleLevelSubmoduleViewDTO.class),
				View.convert(db.getSubmoduleContrapositive(),
						ModuleLevelSubmoduleViewDTO.class),
				View.convert(db.getSubmoduleGeneralisation(),
						ModuleLevelSubmoduleViewDTO.class),
				View.convert(db.getSubmoduleInduction(),
						ModuleLevelSubmoduleViewDTO.class),
				View.convert(db.getSubmoduleLogicBasics(),
						ModuleLevelSubmoduleViewDTO.class)));
	}

	@Test
	public void testCheckpointOrder() {
		ModuleLevelModuleViewDTO view = View.convert(db.getModuleProofTechniques(),
				ModuleLevelModuleViewDTO.class);
		assertThat(view.getCheckpointsInEdition()).isEqualTo(List.of(
				View.convert(db.getCheckpointLectureOne(),
						CheckpointViewDTO.class),
				View.convert(db.getCheckpointLectureTwo(),
						CheckpointViewDTO.class)));
	}

}
