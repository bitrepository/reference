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
INSERT INTO tableversions (tablename, version) VALUES ('files', 1);
INSERT INTO tableversions (tablename, version) VALUES ('pillar', 1);
INSERT INTO tableversions (tablename, version) VALUES ('collections' ,1);
INSERT INTO tableversions (tablename, version) VALUES ('integritydb', 2);

--*************************************************************************--
-- Name:     collections
-- Descr.:   Contains the information about the collections.
-- Purpose:  Keeps track of the names of the files within the system.
-- Expected entry count: Few
--*************************************************************************--
CREATE TABLE collections (
    collection_guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The guid for a given file.
    collection_id VARCHAR(255) NOT NULL,
                                 -- The id for the file.
);

CREATE INDEX collectionindex ON collections (collection_id);

--*************************************************************************--
-- Name:     files
-- Descr.:   Contains the information about the files.
-- Purpose:  Keeps track of the names of the files within the system.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE files (
    file_guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The guid for a given file.
    file_id VARCHAR(255) NOT NULL,
                                 -- The id for the file.
    creation_date TIMESTAMP      -- The date for the creation of the file.
                                 -- Or the time where it was first seen by
                                 -- the integrity client.
);

CREATE INDEX fileindex ON files ( file_id );
CREATE INDEX filedateindex ON files ( file_id, creation_date );

--*************************************************************************--
-- Name:     pillar
-- Descr.:   Contains the information about the pillars.
-- Purpose:  Keeps track of the information about the pillars.
-- Expected entry count: Few
--*************************************************************************--
CREATE TABLE pillar (
    pillar_guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The GUID for the pillar.
    pillar_id VARCHAR(100) NOT NULL
                                 -- The id of the pillar.
);

CREATE INDEX pillarindex ON pillar ( pillar_id );

--*************************************************************************--
-- Name:     fileinfo
-- Descr.:   The main table for containing the information about the files
--           on the different pillars.
-- Purpose:  Keeps track of the information connected to a specific file id
--           on a specific pillar.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE fileinfo (
    guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 -- The unique id for a specific file on a specific pillar.
    file_guid BIGINT NOT NULL,   -- The guid for the file.
    pillar_guid BIGINT NOT NULL, -- The guid for the pillar.
    checksum VARCHAR(100),       -- The checksum for the given file on the given pillar.
    last_file_update TIMESTAMP,  -- The last time a 'GetFileIDs' for the fileinfo has been answered.
    last_checksum_update TIMESTAMP,
                                 -- The date for the latest checksum calculation.
    file_state INT,              -- The state of the file. 0 For EXISTING, 1 for MISSING, 
                                 -- and everything else for UNKNOWN.
    checksum_state INT,           -- Checksum integrity state. Either 0 for VALID, 1 for INCONSISTENT,
                                 -- and everything else for UNKNOWN.
    FOREIGN KEY (file_guid) REFERENCES files(file_guid),
                                 -- Foreign key constraint on file_guid, enforcing the presence of the referred id
    FOREIGN KEY (pillar_guid) REFERENCES pillar(pillar_guid),
                                 -- Foreign key constraint on pillar_guid, enforcing the presence of the referred id
    UNIQUE (file_guid, pillar_guid)
                                 -- Enforce that a file only can exist once on a pillar
);

CREATE INDEX fileguidindex ON fileinfo ( file_guid );
CREATE INDEX filepillarindex ON fileinfo ( file_guid, pillar_guid );
CREATE INDEX checksumdateindex ON fileinfo ( last_checksum_update );
