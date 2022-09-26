---
-- #%L
-- Bitrepository Integrity Client
-- %%
-- Copyright (C) 2010 - 2022 Royal Danish Library and The State Archives, Denmark
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as
-- published by the Free Software Foundation, either version 2.1 of the
-- License, or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Lesser Public License for more details.
--
-- You should have received a copy of the GNU General Lesser Public
-- License along with this program.  If not, see
-- <http://www.gnu.org/licenses/lgpl-2.1.html>.
-- #L%
---

connect 'jdbc:derby:integritydb';

-- database version
UPDATE tableversions SET version = 8 WHERE tablename = 'integritydb';

-- table version
UPDATE tableversions SET version = 3 WHERE tablename = 'pillarstats';

ALTER TABLE pillarstats ADD COLUMN oldest_checksum_timestamp BIGINT DEFAULT NULL;
