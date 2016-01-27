---
-- #%L
-- Bitrepository Alarm Service
-- %%
-- Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
-- Integrity DB migration from version 3 to 4


-- Update table versions.
UPDATE tableversions SET version=4 WHERE tablename='alarmservicedb';
UPDATE tableversions SET version=4 WHERE tablename='alarm';

ALTER TABLE alarm ADD COLUMN alarm_date2 BIGINT;

DROP INDEX alarmdateindex;

UPDATE alarm SET alarm_date2 = (EXTRACT (epoch FROM alarm_date) * 1000);

ALTER TABLE alarm DROP COLUMN alarm_date;

RENAME COLUMN alarm.alarm_date2 TO alarm_date;

ALTER TABLE alarm ALTER COLUMN alarm_date SET NOT NULL;

CREATE INDEX alarmdateindex ON alarm (alarm_date);
