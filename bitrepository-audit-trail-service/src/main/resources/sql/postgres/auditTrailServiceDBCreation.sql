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

--**************************************************************************--
-- Name:        tableversions
-- Description: This table contains an overview of the different tables 
--              within this database along with their respective versions.
-- Purpose:     To keep track of the versions of the tables within the 
--              database. Used for differentiating between different version
--              of the tables, especially when upgrading.
-- Expected entry count: only the tables in this script.
--**************************************************************************--
CREATE TABLE tableversions (
    tablename VARCHAR(100) NOT NULL, -- Name of table
    version SMALLINT NOT NULL        -- version of table
);

INSERT INTO tableversions ( tablename, version ) VALUES ( 'audittrail', 2);
INSERT INTO tableversions ( tablename, version ) VALUES ( 'file', 2);
INSERT INTO tableversions ( tablename, version ) VALUES ( 'contributor', 2);
INSERT INTO tableversions ( tablename, version ) VALUES ( 'actor', 2);
INSERT INTO tableversions ( tablename, version ) VALUES ( 'collection', 1);
INSERT INTO tableversions ( tablename, version ) VALUES ( 'auditservicedb', 2);

--*************************************************************************--
-- Name:     collection
-- Descr.:   Container for the collection ids and their keys.
-- Purpose:  Keeps track of the different collection ids. 
-- Expected entry count: very few. 
--*************************************************************************--
CREATE TABLE collection (
    collection_key SERIAL PRIMARY KEY,
                                    -- The key for the entry in the collection table.
    collectionid VARCHAR(255),      -- The actual id of the collection.
    UNIQUE ( collectionid )
);

--*************************************************************************--
-- Name:     file
-- Descr.:   Container for the files ids and their keys.
-- Purpose:  Keeps track of the different file ids. 
-- Expected entry count: A lot. Though not as many as 'audittrail'.
--*************************************************************************--
CREATE TABLE file (
    file_key SERIAL PRIMARY KEY,    -- The key for the entry in the file table.
    fileid VARCHAR(255),            -- The actual file id.
    collection_key INT NOT NULL,    -- The key for the collection for the file.
    UNIQUE ( fileid, collection_key ),
    FOREIGN KEY (collection_key) REFERENCES collection(collection_key)
                                 -- Foreign key constraint on collection_key, enforcing the presence of the referred key
);

--create index collectionindex on collection ( collectionid );

--*************************************************************************--
-- Name:     contributor
-- Descr.:   Container for the contributors ids and their guids.
-- Purpose:  Keeps track of the different contributor ids. 
-- Expected entry count: Few. Only the pillars and services for the 
--                       collection are contributors of audit trails.
--*************************************************************************--
CREATE TABLE contributor (
    contributor_key SERIAL PRIMARY KEY,
                                    -- The key for the contributor id.
    contributor_id VARCHAR(255),    -- The actual id of the contributor.
    UNIQUE ( contributor_id )
);

--create index contributorindex on contributor ( contributor_id );

--*************************************************************************--
-- Name:     actor
-- Descr.:   Contains the name of an actor.
-- Purpose:  Keeps track of the different actors.
-- Expected entry count: Some, though not many.
--*************************************************************************--
CREATE TABLE actor (
    actor_key SERIAL PRIMARY KEY,    -- The key for the actor.
    actor_name VARCHAR(255),         -- The name of the actor.
    UNIQUE ( actor_name )
);

--create index actorindex on actor ( actor_name );

--*************************************************************************--
-- Name:     preservation
-- Descr.:   Container for the preservation of audit trails based on 
--           contributors per collection.
-- Purpose:  Keeps track of the sequence number reached by the preservation
--           for each contributor per collection. 
-- Expected entry count: Few. Only the pillars and services for each 
--                       collection are contributors of audit trails.
--*************************************************************************--
CREATE TABLE preservation (
    preservation_key SERIAL PRIMARY KEY,
                                    -- The key for the preservation id.
    contributor_key INT,            -- The key of the contributor.
    collection_key INT,             -- The key for the collection.
    preserved_seq_number BIGINT,    -- The sequence number reached for the preservation
                                    -- of the audit trails for the contributor.
    FOREIGN KEY (contributor_key) REFERENCES contributor(contributor_key),
                                    -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    FOREIGN KEY (collection_key) REFERENCES collection(collection_key),
                                    -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    UNIQUE (collection_key, contributor_key)        
                                    -- Enforce that each contributor only can exist once per collection.
);

--*************************************************************************--
-- Name:     audittrail
-- Descr.:   Container for the audits with their sequence number, the guid
--           for the file, the action which cause the audit, the id for the
--           actor, and the date for the audit.
-- Purpose:  Keeps track of the different audits.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE audittrail (
    audit_key SERIAL PRIMARY KEY,   -- The key for this table.
    sequence_number BIGINT NOT NULL,-- The sequence number for the given audit trail.
    contributor_key INT NOT NULL,   -- The identifier for the contributor of this audittrail.
                                    -- Used for looking up in the contributor table.
    file_key INT NOT NULL,          -- The identifier for the file. Used to lookup in the file table.
    actor_key INT NOT NULL,         -- The identifier for the actor which performed the action for the audit. 
                                    -- Used for looking up in the actor table.
    operation VARCHAR(100),         -- The name of the action behind the audit.
    operation_date TIMESTAMP,       -- The date when the action was performed.
    audit VARCHAR(255),             -- The audit trail delivered from the actor. 
    information VARCHAR(255),       -- The information about the audit.
    
    FOREIGN KEY (contributor_key) REFERENCES contributor(contributor_key),
                                 -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    FOREIGN KEY (file_key) REFERENCES file(file_key),
                                 -- Foreign key constraint on file_key, enforcing the presence of the referred id                                 
    FOREIGN KEY (actor_key) REFERENCES actor(actor_key)
                                 -- Foreign key constraint on pillar_key, enforcing the presence of the referred id

);

CREATE INDEX dateindex ON audittrail ( operation_date );
CREATE INDEX auditindex ON audittrail ( contributor_key, file_key, actor_key );
