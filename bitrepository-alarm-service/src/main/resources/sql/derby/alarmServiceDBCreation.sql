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

connect 'jdbc:derby:alarmservicedb;create=true';

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

INSERT INTO tableversions ( tablename, version )
            VALUES ( 'alarm', 2);
INSERT INTO tableversions ( tablename, version )
            VALUES ( 'component', 2);
INSERT INTO tableversions (tablename, version) 
            VALUES ('alarmservicedb', 3);

--*************************************************************************--
-- Name:     component
-- Descr.:   Container for the components ids and their guids.
-- Purpose:  Keeps track of the different component ids. 
-- Expected entry count: Few.
--*************************************************************************--
CREATE TABLE component (
    component_guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    -- The guid for the component id.
    component_id VARCHAR(255),      -- The actual component id.
    UNIQUE(component_id)            -- Ensure that there cannot be two components that have the same id
);

CREATE INDEX componentindex ON component ( component_id );

--*************************************************************************--
-- Name:     alarm
-- Descr.:   Contains the alarm data. The component_guid refers to the 
--           component table.
-- Purpose:  Keeps track of the alarms.
-- Expected entry count: Very, very many.
--*************************************************************************--
CREATE TABLE alarm (
    guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     -- The guid for the alarm.
    component_guid BIGINT NOT NULL,  -- The guid for the component behind the alarm.
    alarm_code VARCHAR(50) NOT NULL, -- The code for the alarm.
    alarm_text CLOB NOT NULL,        -- The text for the alarm.
    alarm_date TIMESTAMP NOT NULL,   -- The date for the alarm.
    file_id VARCHAR(255),            -- The id for the file (allowed to be null).
    collection_id VARCHAR(255),      -- The id of the collection that the alarm belongs to (allowed to be null)
    FOREIGN KEY ( component_guid ) REFERENCES component ( component_guid )
                                     -- Foreign key reference, to enforce database consistency
);

CREATE INDEX codeindex ON alarm ( alarm_code );
CREATE INDEX alarmdateindex ON alarm ( alarm_date );
CREATE INDEX fileidindex ON alarm ( file_id );
CREATE INDEX collectionidindex ON alarm ( collection_id );


