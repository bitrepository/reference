---
-- #%L
-- Bitrepository Reference Pillar
-- %%
-- Copyright (C) 2010 - 2016 The State and University Library, The Royal Library and The State Archives, Denmark
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

-- Update table versions.
UPDATE tableversions SET version = 4 WHERE tablename = 'checksums';

DROP INDEX calculationindex;

ALTER TABLE checksums ADD COLUMN calculationdate2 BIGINT;
UPDATE checksums SET calculationdate2 = (EXTRACT (epoch FROM calculationdate AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);
ALTER TABLE checksums DROP COLUMN calculationdate;
RENAME COLUMN checksums.calculationdate2 TO calculationdate;
ALTER TABLE audittrail ALTER COLUMN operation_date SET NOT NULL;

CREATE INDEX calculationindex ON checksums ( calculationdate );
