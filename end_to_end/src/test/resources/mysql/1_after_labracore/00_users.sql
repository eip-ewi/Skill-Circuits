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

insert into person () values ();
insert into concrete_person (id, username, display_name, number, email, external_id, default_role, account_disabled, two_fa_enabled)
    select last_insert_id(), "cseteacher1", "CSE Teacher 1", 1, "cseteacher1@tudelft.nl", "cseteacher1@tudelft.nl", 3, 0, 0;

insert into person () values ();
    insert into concrete_person (id, username, display_name, number, email, external_id, default_role, account_disabled, two_fa_enabled)
    select last_insert_id(), "csestudent1", "CSE Student 1", 101, "csestudent1@student.tudelft.nl", "csestudent1@tudelft.nl", 0, 0, 0;
insert into person () values ();
    insert into concrete_person (id, username, display_name, number, email, external_id, default_role, account_disabled, two_fa_enabled)
    select last_insert_id(), "csestudent2", "CSE Student 2", 102, "csestudent2@student.tudelft.nl", "csestudent2@tudelft.nl", 0, 0, 0;
insert into person () values ();
    insert into concrete_person (id, username, display_name, number, email, external_id, default_role, account_disabled, two_fa_enabled)
    select last_insert_id(), "csestudent3", "CSE Student 3", 103, "csestudent3@student.tudelft.nl", "csestudent3@tudelft.nl", 0, 0, 0;
insert into person () values ();
    insert into concrete_person (id, username, display_name, number, email, external_id, default_role, account_disabled, two_fa_enabled)
    select last_insert_id(), "csestudent4", "CSE Student 4", 104, "csestudent4@student.tudelft.nl", "csestudent4@tudelft.nl", 0, 0, 0;
