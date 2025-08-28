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
package nl.tudelft.skills.repository.bookmark;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nl.tudelft.skills.model.ChoiceTask;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.TaskInfo;
import nl.tudelft.skills.model.bookmark.HiddenSkillBookmarkList;

public interface HiddenSkillBookmarkListRepository extends JpaRepository<HiddenSkillBookmarkList, Long> {

	@Query("""
			select list from HiddenSkillBookmarkList list
			where list.skill.submodule.module.edition.id in :#{#editions}
			""")
	Set<HiddenSkillBookmarkList> findAllByEditions(@Param("editions") Set<Long> editions);

	@Query("""
			select list from HiddenSkillBookmarkList list
			inner join list.skills skill
			where skill.id = :#{#skill.id}
			""")
	Set<HiddenSkillBookmarkList> findAllBySkillsContains(@Param("skill") Skill skill);

	@Query("""
			select list from HiddenSkillBookmarkList list
			inner join list.tasks task
			where task.id = :#{#task.id}
			""")
	Set<HiddenSkillBookmarkList> findAllByTasksContains(@Param("task") TaskInfo task);

	@Query("""
			select list from HiddenSkillBookmarkList list
			inner join list.choiceTasks task
			where task.id = :#{#task.id}
			""")
	Set<HiddenSkillBookmarkList> findAllByChoiceTasksContains(@Param("task") ChoiceTask task);

	void deleteBySkill(Skill skill);

}
