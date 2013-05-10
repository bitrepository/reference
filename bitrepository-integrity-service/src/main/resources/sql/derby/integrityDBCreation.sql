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
-- HEADER

connect 'jdbc:derby:integritydb;create=true';

--**************************************************************************--
-- Name:        tableversions
-- Description: This table contains an overview of the different tables 
--              within this database along with their respective versions.
-- Purpose:     To keep track of the versions of the tables within the 
--              database. Used for differentiating between different version
--              of the tables, especially when upgrading.
-- Expected entry count: only those in this script.
--**************************************************************************--
CREATE TABLE tableversions (
    tablename VARCHAR(100) NOT NULL, -- Name of table
    version INT NOT NULL             -- version of table
);

INSERT INTO tableversions (tablename, version) VALUES ('fileinfo', 2);
INSERT INTO tableversions (tablename, version) VALUES ('files', 2);
INSERT INTO tableversions (tablename, version) VALUES ('pillar', 2);
INSERT INTO tableversions (tablename, version) VALUES ('collections' ,1);
INSERT INTO tableversions (tablename, version) VALUES ('integritydb', 2);
INSERT INTO tableversions (tablename, version) VALUES ('stats', 1);
INSERT INTO tableversions (tablename, version) VALUES ('collectionstats', 1);
INSERT INTO tableversions (tablename, version) VALUES ('pillarstats', 1);

--*************************************************************************--
-- Name:     collections
-- Descr.:   Contains the information about the collections.
-- Purpose:  Keeps track of the names of the files within the system.
-- Expected entry count: Few
--*************************************************************************--
CREATE TABLE collections (
    collection_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The key for a given file.
    collection_id VARCHAR(255) NOT NULL,
                                 -- The id for the file.
    UNIQUE (collection_id)
);

CREATE INDEX collectionindex ON collections (collection_id);

--*************************************************************************--
-- Name:     files
-- Descr.:   Contains the information about the files.
-- Purpose:  Keeps track of the names of the files within the system.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE files (
    file_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The key for a given file.
    file_id VARCHAR(255) NOT NULL,
                                 -- The id for the file.
    creation_date TIMESTAMP,     -- The date for the creation of the file.
                                 -- Or the time where it was first seen by
                                 -- the integrity client.
    collection_key BIGINT NOT NULL,
                                 -- The key of the collection that the file belongs to. 
    FOREIGN KEY (collection_key) REFERENCES collections(collection_key),
                                 -- Foreign key constraint on collection_key, enforcing the presence of the reffered id
    UNIQUE (file_id, collection_key)
                                 -- Enforce that a file can only exist once in a collection
    
);

CREATE INDEX fileindex ON files (file_id);
CREATE INDEX filedateindex ON files (file_id, creation_date);

--*************************************************************************--
-- Name:     pillar
-- Descr.:   Contains the information about the pillars.
-- Purpose:  Keeps track of the information about the pillars.
-- Expected entry count: Few
--*************************************************************************--
CREATE TABLE pillar (
    pillar_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The key for the pillar.
    pillar_id VARCHAR(100) NOT NULL,
                                 -- The id of the pillar.
    UNIQUE (pillar_id)
);

CREATE INDEX pillarindex ON pillar (pillar_id);

--*************************************************************************--
-- Name:     fileinfo
-- Descr.:   The main table for containing the information about the files
--           on the different pillars.
-- Purpose:  Keeps track of the information connected to a specific file id
--           on a specific pillar.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE fileinfo (
    fileinfo_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The unique id for a specific file on a specific pillar.
    file_key BIGINT NOT NULL,    -- The key for the file.
    pillar_key BIGINT NOT NULL,  -- The key for the pillar.
    checksum VARCHAR(100),       -- The checksum for the given file on the given pillar.
    file_size BIGINT,            -- The size of the file. 
    last_file_update TIMESTAMP,  -- The last time a 'GetFileIDs' for the fileinfo has been answered.
    last_checksum_update TIMESTAMP,
                                 -- The date for the latest checksum calculation.
    file_state INT,              -- The state of the file. 0 For EXISTING, 1 for MISSING, 
                                 -- and everything else for UNKNOWN.
    checksum_state INT,           -- Checksum integrity state. Either 0 for VALID, 1 for INCONSISTENT,
                                 -- and everything else for UNKNOWN.
    FOREIGN KEY (file_key) REFERENCES files(file_key),
                                 -- Foreign key constraint on file_key, enforcing the presence of the referred id
    FOREIGN KEY (pillar_key) REFERENCES pillar(pillar_key),
                                 -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    UNIQUE (file_key, pillar_key)
                                 -- Enforce that a file only can exist once on a pillar
);

CREATE INDEX checksumdateindex ON fileinfo (last_checksum_update);
CREATE INDEX filestateindex ON fileinfo (file_state);
CREATE INDEX checksumstateindex ON fileinfo (checksum_state);

--*************************************************************************--
-- Name:     statistics 
-- Descr.:   Contains the information collected statistics.
-- Purpose:  Keeps track of the collected statistics.
-- Expected entry count: Many (over time)
--*************************************************************************--
CREATE TABLE stats (
    stat_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The key for a set of statistics.
    stat_time TIMESTAMP NOT NULL, 
                                 -- The time the statistics entry were made.
    last_update TIMESTAMP NOT NULL,
                                 -- The last time the statistics were updated
    collection_key BIGINT NOT NULL,
                                 -- The key of the collection that the statistics belongs to 
    FOREIGN KEY (collection_key) REFERENCES collections(collection_key)
                                 -- Foreign key constraint on collection_key, enforcing the presence of the referred key
);

CREATE INDEX lastupdatetimeindex ON stats (last_update);

--*************************************************************************--
-- Name:     collectionstats
-- Descr.:   Contains the information about collection statistics.
-- Purpose:  Keeps track of the statistics for a collection.
-- Expected entry count: many (over time)
--*************************************************************************--
CREATE TABLE collectionstats (
    collectionstat_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The key for the collectionstat.
    stat_key BIGINT NOT NULL,
                                 -- The key for the statistics entity.
    file_count BIGINT,           -- The number of files that the collection contained when the stats were made
    file_size BIGINT,            -- The total size of the files in the collection when the stats were made
    checksum_errors_count BIGINT, 
                                 -- The number of checksum errors in the collection when the stats were made
    UNIQUE (stat_key), 
                                 -- Enforce that there can only be one collectionstat for a statistics
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key)
                                 -- Foreign key constraint on stat_key, enforcing the presence of the referred key
);

--*************************************************************************--
-- Name:     pillarstats
-- Descr.:   Contains the information about pillar statistics.
-- Purpose:  Keeps track of the statistics for a pillar.
-- Expected entry count: many (over time)
--*************************************************************************--
CREATE TABLE pillarstats (
    pillarstat_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The key for the pillarstat.
    stat_key BIGINT NOT NULL,
                                 -- The key for the statistics entity.
    pillar_key BIGINT NOT NULL,
                                 -- The key of the pillar that the statistics belongs to 
    file_count BIGINT,           -- The number of files on the pillar when the stats were made
    file_size BIGINT,            -- The total size of the files on the pillar when the stats were made
    missing_files_count BIGINT,  -- The number of the missing files on the pillar when the stats were made
    checksum_errors_count BIGINT, 
                                 -- The number of checksum errors on the pillar when the stats were made
    UNIQUE (stat_key, pillar_key), 
                                 -- Enforce that there can only be one collectionstat for a statistics
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key),
                                 -- Foreign key constraint on stat_key, enforcing the presence of the referred key
    FOREIGN KEY (pillar_key) REFERENCES pillar(pillar_key)
                                 -- Foreign key constraint on collection_key, enforcing the presence of the referred key
);
