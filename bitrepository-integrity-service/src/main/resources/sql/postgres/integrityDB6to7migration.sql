---
-- #%L
-- Bitrepository Integrity Client
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

-- Update table versions.
UPDATE tableversions SET version = 6 WHERE tablename = 'fileinfo';
UPDATE tableversions SET version = 7 WHERE tablename = 'integritydb';
UPDATE tableversions SET version = 2 WHERE tablename = 'collection_progress';
UPDATE tableversions SET version = 3 WHERE tablename = 'stats';
UPDATE tableversions SET version = 3 WHERE tablename = 'collectionstats';


-- Migrate fileinfo table
ALTER TABLE fileinfo ADD COLUMN file_timestamp2 BIGINT;
ALTER TABLE fileinfo ADD COLUMNT checksum_timestamp2 BIGINT;
ALTER TABLE fileinfo ADD COLUMNT last_seen_getfileids2 BIGINT;
ALTER TABLE fileinfo ADD COLUMNT last_seen_getchecksums2 BIGINT;

DROP INDEX checksumdateindex;
DROP INDEX lastseenindex;

UPDATE fileinfo SET file_timestamp2 = (EXTRACT (epoch FROM file_timestamp) * 1000);
UPDATE fileinfo SET checksum_timestamp2 = (EXTRACT (epoch FROM checksum_timestamp) * 1000);
UPDATE fileinfo SET last_seen_getfileids2 = (EXTRACT (epoch FROM last_seen_getfileids) * 1000);
UPDATE fileinfo SET last_seen_getchecksums2 = (EXTRACT (epoch FROM last_seen_getchecksums) * 1000);

ALTER TABLE fileinfo DROP COLUMN file_timestamp;
ALTER TABLE fileinfo DROP COLUMN checksum_timestamp;
ALTER TABLE fileinfo DROP COLUMN last_seen_getfileids;
ALTER TABLE fileinfo DROP COLUMN last_seen_getchecksums;

ALTER TABLE fileinfo RENAME COLUMN file_timestamp2 TO file_timestamp;
ALTER TABLE fileinfo RENAME COLUMN checksum_timestamp2 TO checksum_timestamp;
ALTER TABLE fileinfo RENAME COLUMN last_seen_getfileids2 TO last_seen_getfileids;
ALTER TABLE fileinfo RENAME COLUMN last_seen_getchecksums2 TO last_seen_getchecksums;


-- Migrate colletion_progress table
ALTER TABLE collection_progress ADD COLUMN latest_file_timestamp2 BIGINT DEFAULT NULL;
ALTER TABLE collection_progress ADD COLUMN latest_checksum_timestamp2 BIGINT DEFAULT NULL;

UPDATE collection_progress SET latest_file_timestamp2 = (EXTRACT (epoch FROM latest_file_timestamp) * 1000);
UPDATE collection_progress SET latest_checksum_timestamp2 = (EXTRACT (epoch FROM latest_checksum_timestamp) * 1000);

ALTER TABLE collection_progress DROP COLUMN latest_file_timestamp;
ALTER TABLE collection_progress DROP COLUMN latest_checksum_timestamp;

ALTER TABLE collection_progress RENAME COLUMN latest_file_timestamp2 TO latest_file_timestamp;
ALTER TABLE collection_progress RENAME COLUMN latest_checksum_timestamp2 TO latest_checksum_timestamp;


-- Migrate stats table
ALTER TABLE stats ADD COLUMN stat_time2 BIGINT;
ALTER TABLE stats ADD COLUMN last_update2 BIGINT;

DROP INDEX lastupdatetimeindex;

UPDATE stats SET stat_time2 = (EXTRACT (epoch FROM stat_time) * 1000);
UPDATE stats SET last_update2 = (EXTRACT (epoch FROM last_update) * 1000);

ALTER TABLE stats DROP COLUMN stat_time;
ALTER TABLE stats DROP COLUMN last_update;

ALTER TABLE stats RENAME COLUMN stat_time2 TO stat_time;
ALTER TABLE stats RENAME COLUMN last_update2 TO last_update;

ALTER TABLE stats ALTER COLUMN stat_time SET NOT NULL;
ALTER TABLE stats ALTER COLUMN last_update SET NOT NULL;

CREATE INDEX lastupdatetimeindex ON stats (last_update);


-- Migrate collectionstats table
ALTER TABLE collectionstats ADD COLUMN latest_file_date2 BIGINT;

UPDATE collectionstats SET latest_file_date2 = (EXTRACT (epoch FROM latest_file_date) * 1000);

ALTER TABLE collectionstats DROP COLUMN latest_file_date;

ALTER TABLE collectionstats RENAME COLUMN latest_file_date2 TO latest_file_date;

ALTER TABLE collectionstats ALTER COLUMN latest_file_date SET NOT NULL;



