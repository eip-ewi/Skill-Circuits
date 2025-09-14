--
-- Skill Circuits
-- Copyright (C) 2025 - Delft University of Technology
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as
-- published by the Free Software Foundation, either version 3 of the
-- License, or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.
--

use labracore;

insert into course_managers (manages_id, managers_id)
    select course.id, concrete_person.id
    from course join concrete_person where course.code = "CSE1300" and concrete_person.username = "cseteacher1";


# 0: STUDENT
# 1: TA
# 2: HEAD_TA
# 3: TEACHER

insert into role (type, edition_id, person_id)
    select 3, edition.id, concrete_person.id
    from edition inner join course on edition.course_id = course.id join concrete_person
    where course.code = "CSE1300" and edition.name = "2025/2026 Q1" and concrete_person.username = "cseteacher1";
