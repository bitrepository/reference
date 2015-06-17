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
UPDATE tableversions SET version = 4 WHERE tablename = 'fileinfo';
UPDATE tableversions SET version = 3 WHERE tablename = 'pillar';
UPDATE tableversions SET version = 2 WHERE tablename = 'collections';
UPDATE tableversions SET version = 5 WHERE tablename = 'integritydb';
UPDATE tableversions SET version = 2 WHERE tablename = 'stats';
UPDATE tableversions SET version = 2 WHERE tablename = 'collectionstats';
UPDATE tableversions SET version = 2 WHERE tablename = 'pillarstats';
INSERT INTO tableversions (tablename, version) VALUES ('collection_progress', 1);

CREATE TABLE collections2 (
    collectionID VARCHAR(255) PRIMARY KEY
);
INSERT INTO collections2 (collectionID) (SELECT collection_id FROM collections);


CREATE TABLE pillar2 (
    pillarID VARCHAR(100) PRIMARY KEY
);
INSERT INTO pillar2 (pillarID) (SELECT pillar_id FROM PILLAR);


CREATE TABLE stats2 (
    stat_key SERIAL PRIMARY KEY, -- The key for a set of statistics.
    stat_time TIMESTAMP NOT NULL, 
                                 -- The time the statistics entry were made.
    last_update TIMESTAMP NOT NULL,
                                 -- The last time the statistics were updated
    collectionID VARCHAR(255) NOT NULL -- The key of the collection that the statistics belongs to 
);
INSERT INTO stats2 (stat_key, stat_time, last_update, collectionID) (SELECT stat_key, stat_time, last_update, collection_id FROM stats JOIN collections ON stats.collection_key = collections.collection_key);
-- create fk on collectionid


CREATE TABLE pillarstats2 (
    pillarstat_key SERIAL PRIMARY KEY,
                                 -- The key for the pillarstat.
    stat_key INT NOT NULL,       -- The key for the statistics entity.
    pillarID VARCHAR(100) NOT NULL,     -- The key of the pillar that the statistics belongs to 
    file_count BIGINT,           -- The number of files on the pillar when the stats were made
    file_size BIGINT,            -- The total size of the files on the pillar when the stats were made
    missing_files_count BIGINT,     -- The number of the missing files on the pillar when the stats were made
    checksum_errors_count BIGINT,   -- The number of checksum errors on the pillar when the stats were made
    missing_checksums_count BIGINT, -- The number of missing checksums on the pillar
    obsolete_checksums_count BIGINT, --The number of obsolete checksums on the pillar. 
    UNIQUE (stat_key, pillarID), 
);
INSERT INTO pillarstats2 (pillarstat_key, stat_key, pillarID, file_count, file_size, missing_files_count, checksum_errors_count, missing_checksums_count, obsolete_checksums_count)
    (SELECT pillarstat_key, stat_key, pillarID, file_count, file_size, missing_files_count, checksum_errors_count, 0, 0 FROM pillarstats JOIN pillar ON pillarstats.pillar_key = pillar.pillar_key);
-- add fk constraints to pillar stats


CREATE TABLE collectionstats2 (
    collectionstat_key SERIAL PRIMARY KEY,
    stat_key INT NOT NULL,
    file_count BIGINT,
    file_size BIGINT, 
    checksum_errors_count BIGINT,   
    latest_file_date TIMESTAMP NOT NULL, 
    UNIQUE (stat_key),           
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key)
);
INSERT INTO collectionstats2 (collectionstat_key, stat_key, file_count, file_size, checksum_errors_count, latest_file_date) 
    (SELECT collectionstat_key, stat_key, 0, file_size, checksum_errors_count, NOW() FROM collectionstats);


CREATE TABLE fileinfo2 (
    fileID VARCHAR(255) NOT NULL, 
    collectionID VARCHAR(255) NOT NULL, 
    pillarID VARCHAR(255) NOT NULL, 
    filesize BIGINT,
    checksum VARCHAR(100),
    file_timestamp TIMESTAMP,
    checksum_timestamp TIMESTAMP,
    last_seen_getfileids TIMESTAMP,
    last_seen_getchecksums TIMESTAMP,

    PRIMARY KEY (collectionID, pillarID, fileID)
);
INSERT INTO fileinfo2 (fileID, collectionID, pillarID, filesize, checksum, file_timestamp, checksum_timestamp, last_seen_getfileids, last_seen_getchecksums) 
    (SELECT file_id, collection_id, pillar_id, file_size, checksum, last_file_update, last_checksum_update, NULL, NULL FROM fileinfo 
    JOIN files ON fileinfo.file_key = files.file_key
    JOIN pillar ON fileinfo.pillar_key = pillar.pillar_key
    JOIN collections ON files.collection_key = collections.collection_key);


CREATE TABLE collection_progress (
    collectionID VARCHAR(255) NOT NULL,
    pillarID VARCHAR(100) NOT NULL,
    latest_file_timestamp TIMESTAMP DEFAULT NULL,
    latest_checksum_timestamp TIMESTAMP DEFAULT NULL
);

DROP TABLE collectionstats;
DROP TABLE pillarstats;
DROP TABLE stats;
DROP TABLE fileinfo;
DROP TABLE files;
DROP TABLE pillars;
DROP TABLE collections;

ALTER TABLE collectionstats2 RENAME TO collectionstats;
ALTER TABLE pillarstats2 RENAME TO pillarstats;
ALTER TABLE stats2 RENAME TO stats;
ALTER TABLE fileinfo2 RENAME TO fileinfo;
ALTER TABLE pillars2 RENAME TO pillars;
ALTER TABLE collections2 RENAME TO collections;


