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

connect 'jdbc:derby:checksumdb';

-- Update table versions.
UPDATE tableversions SET version = 4 WHERE tablename = 'checksums';

DROP INDEX calculationindex;

ALTER TABLE checksums ADD COLUMN calculationdate2 BIGINT;
UPDATE checksums SET calculationdate2 = ({fn timestampdiff(SQL_TSI_FRAC_SECOND, timestamp('1970-1-1-00.00.00.000000'), calculationdate)} / 1000000);
ALTER TABLE checksums DROP COLUMN calculationdate;
RENAME COLUMN checksums.calculationdate2 TO calculationdate;

CREATE INDEX calculationindex ON checksums ( calculationdate );
