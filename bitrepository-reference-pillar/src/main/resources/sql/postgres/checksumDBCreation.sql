---
-- #%L
-- Bitrepository Reference Pillar
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

INSERT INTO tableversions ( tablename, version ) VALUES ( 'checksums', 2);

--*************************************************************************--
-- Name:     checksums
-- Descr.:   Container for the checksum entry information: the file ids and 
--           collection ids, their checksums and the timestamp of the
--           calculation of this checksum. 
-- Purpose:  Keep track of the checksum entries. 
-- Expected entry count: Very many, one for each file..
--*************************************************************************--
CREATE TABLE checksums (
    guid SERIAL PRIMARY KEY,            -- The sequence number and unique key for this table.
    fileid VARCHAR(255) NOT NULL,       -- The id of the file.
    collectionid VARCHAR(255) NOT NULL, -- The id of the collection.
    checksum VARCHAR(255),              -- The checksum of the file.
    calculationdate TIMESTAMP           -- The timestamp for the calculation of the checksum.
);

CREATE INDEX fileindex ON checksums ( fileid, collectionid );
CREATE INDEX filedateindex ON checksums ( fileid, calculationdate );
