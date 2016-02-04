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

connect 'jdbc:derby:auditcontributerdb;create=true';

--**************************************************************************--
-- Name:        tableversions
-- Description: This table contains an overview of the different tables 
--              within this database along with their respective versions.
-- Purpose:     To keep track of the versions of the tables within the 
--              database. Used for differentiating between different version
--              of the tables, especially when upgrading.
-- Expected entry count: only those in this script.
--**************************************************************************--
create table tableversions (
    tablename varchar(100) not null, -- Name of table
    version int not null             -- version of table
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
create table file (
    file_guid bigint not null generated always as identity primary key,
                                    -- The guid for the file id.
    fileid varchar(255),            -- The actual file id.
    collectionid varchar(255)       -- The collection for the file id.
);

create index fileindex on file ( fileid, collectionid );

--*************************************************************************--
-- Name:     actor
-- Descr.:   Contains the 
-- Purpose:  Keeps track of the different actors.
-- Expected entry count: Some, though not many.
--*************************************************************************--
create table actor (
    actor_guid bigint not null generated always as identity primary key,
                                    -- The guid for the actor.
    actor_name varchar(255)         -- The name of the actor.
);

create index actorindex on actor ( actor_name );

--*************************************************************************--
-- Name:     audittrail
-- Descr.:   Container for the audits with their sequence number, the guid
--           for the file, the action which cause the audit, the id for the
--           actor, and the date for the audit.
-- Purpose:  Keeps track of the different audits.
-- Expected entry count: Very, very many.
--*************************************************************************--
create table audittrail (
    sequence_number bigint not null generated always as identity primary key,
                                    -- The sequence number and unique key for this table.
    file_guid bigint,               -- The identifier for the file. Used to lookup in the file table.
    actor_guid bigint,              -- The identifier for the actor which performed the action for the audit. 
                                    -- Used for looking up in the 
    operation varchar(100),         -- The name of the action behind the audit.
    operation_date BIGINT,          -- The date (millis since epoch) when the action was performed.
    audit CLOB,                     -- The audit trail delivered from the actor. 
    information CLOB,               -- The information about the audit.
    operationID VARCHAR(100),       -- The conversation/operation ID the the audit belongs to.
    fingerprint VARCHAR(100),       -- The fingerprint (SHA-1 sum) for the certificate used in the operation.
    FOREIGN KEY ( file_guid ) REFERENCES file ( file_guid ),
                                    -- Foreign key constraint for enforcing relationship
    FOREIGN KEY ( actor_guid ) REFERENCES actor ( actor_guid )
                                    -- Foreign key constraint for enforcing relationship
);

create index dateindex on audittrail ( operation_date );
create index fileidindex on audittrail ( file_guid );
