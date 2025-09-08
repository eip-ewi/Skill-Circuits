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

insert into apikey (name, uuid, secret, enabled, created_at, expires_at, created_by_id)
    values ("Skill Circuits", "skills-key", "skills-secret", 1, now(), adddate(now(), interval 1 year), 1);

insert into apikey_permissions (keys_id, permissions_id)
    select last_insert_id(), id from permission
    where name in ("COURSE_READ", "COURSE_UPDATE", "EDITION_CREATE", "EDITION_READ", "EDITION_UPDATE", "EDITION_DELETE",
                   "PERSON_CREATE", "PERSON_READ", "PERSON_UPDATE", "PROGRARM_READ", "ROLE_CREATE", "ROLE_UPDATE",
                   "ROLE_DELETE", "ROLE_READ", "PERMISSION_READ");
