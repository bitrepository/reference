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

insert into tableversions ( tablename, version ) values ( 'audit', 5);
insert into tableversions ( tablename, version ) values ( 'file', 2);
insert into tableversions ( tablename, version ) values ( 'actor', 1);
insert into tableversions ( tablename, version ) values ( 'auditcontributordb', 5);

--*************************************************************************--
-- Name:     file
-- Descr.:   Container for the files ids and their guids.
-- Purpose:  Keeps track of the different file ids. 
-- Expected entry count: A lot. Though not as many as 'audittrail'.
--*************************************************************************--
CREATE TABLE file (
    file_guid SERIAL PRIMARY KEY,   -- The guid for the file id.
    fileid VARCHAR(255),            -- The actual file id.
    collectionid VARCHAR(255)       -- The collection for the file id.
);

CREATE INDEX fileindex ON file ( fileid, collectionid );

--*************************************************************************--
-- Name:     actor
-- Descr.:   Contains the 
-- Purpose:  Keeps track of the different actors.
-- Expected entry count: Some, though not many.
--*************************************************************************--
CREATE TABLE actor (
    actor_guid SERIAL PRIMARY KEY,  -- The guid for the actor.
    actor_name VARCHAR(255)         -- The name of the actor.
);

CREATE INDEX actorindex ON actor ( actor_name );

--*************************************************************************--
-- Name:     audittrail
-- Descr.:   Container for the audits with their sequence number, the guid
--           for the file, the action which cause the audit, the id for the
--           actor, and the date for the audit.
-- Purpose:  Keeps track of the different audits.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE audittrail (
    sequence_number SERIAL PRIMARY KEY,
                                    -- The sequence number and unique key for this table.
    file_guid INT,                  -- The identifier for the file. Used to lookup in the file table.
    actor_guid INT,                 -- The identifier for the actor which performed the action for the audit. 
                                    -- Used for looking up in the 
    operation VARCHAR(100),         -- The name of the action behind the audit.
    operation_date BIGINT,          -- The date when the action was performed.
    audit TEXT,                     -- The audit trail delivered from the actor. 
    information TEXT,               -- The information about the audit.
    operationID VARCHAR(100),       -- The conversation/operation ID the the audit belongs to.
    fingerprint VARCHAR(100),       -- The fingerprint (SHA-1 sum) for the certificate used in the operation.
    FOREIGN KEY ( file_guid ) REFERENCES file ( file_guid ),
                                    -- Foreign key constraint for enforcing relationship
    FOREIGN KEY ( actor_guid ) REFERENCES actor ( actor_guid )
                                    -- Foreign key constraint for enforcing relationship
);

CREATE INDEX dateindex ON audittrail ( operation_date );
CREATE INDEX fileidindex ON audittrail ( file_guid );
