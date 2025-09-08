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

insert into edition (name, start_date, end_date, enrollability, is_archived, course_id, cohort_id)
    select "2025/2026 Q1", now(), adddate(now(), interval 1 year), 0, 0, course.id, cohort.id
    from course join cohort where course.code = "CSE1100" and cohort.name = "BSc CSE";
insert into edition (name, start_date, end_date, enrollability, is_archived, course_id, cohort_id)
    select "2025/2026 Q1", now(), adddate(now(), interval 1 year), 0, 0, course.id, cohort.id
    from course join cohort where course.code = "CSE1300" and cohort.name = "BSc CSE";
insert into edition (name, start_date, end_date, enrollability, is_archived, course_id, cohort_id)
    select "2025/2026 Q1", now(), adddate(now(), interval 1 year), 0, 0, course.id, cohort.id
    from course join cohort where course.code = "CSE1400" and cohort.name = "BSc CSE";

