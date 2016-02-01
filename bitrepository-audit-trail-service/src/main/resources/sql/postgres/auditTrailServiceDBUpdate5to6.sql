---
-- #%L
-- Bitrepository Audit Trail Service
-- %%
-- Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

UPDATE tableversions SET version = 6 WHERE tablename = 'auditservicedb';
UPDATE tableversions SET version = 5 WHERE tablename = 'audittrail';

ALTER TABLE audittrail ADD COLUMN operation_date2 BIGINT;

DROP INDEX dateindex;
DROP INDEX auditindex;

UPDATE audittrail SET operation_date2 = (EXTRACT (epoch FROM operation_date AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);

ALTER TABLE audittrail DROP COLUMN operation_date;

ALTER TABLE audittrail RENAME COLUMN operation_date2 TO operation_date;

ALTER TABLE audittrail ALTER COLUMN operation_date SET NOT NULL;

CREATE INDEX dateindex ON audittrail (operation_date);
CREATE INDEX auditindex ON audittrail (file_key, contributor_key);
