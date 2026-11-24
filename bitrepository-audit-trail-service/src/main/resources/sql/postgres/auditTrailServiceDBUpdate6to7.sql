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

-- Widen audit_key from INT to BIGINT, as it is approaching the 32-bit limit.
-- NB: Changing the column type rewrites the whole audittrail table and its primary key index
-- under an exclusive lock. On large databases expect long downtime and temporarily up to twice the disk usage.

UPDATE tableversions SET version = 7 WHERE tablename = 'auditservicedb';
UPDATE tableversions SET version = 6 WHERE tablename = 'audittrail';

ALTER TABLE audittrail ALTER COLUMN audit_key TYPE BIGINT;
-- SERIAL creates an INTEGER sequence, which would still overflow at 2^31-1.
ALTER SEQUENCE audittrail_audit_key_seq AS BIGINT;
