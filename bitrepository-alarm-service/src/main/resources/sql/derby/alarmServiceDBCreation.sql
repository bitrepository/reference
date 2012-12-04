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
create table tableversions (
    tablename varchar(100) not null, -- Name of table
    version int not null             -- version of table
);

insert into tableversions ( tablename, version )
            values ( 'alarm', 1);
insert into tableversions ( tablename, version )
            values ( 'component', 1);


--*************************************************************************--
-- Name:     alarm
-- Descr.:   Contains the alarm data. The component_guid refers to the 
--           component table.
-- Purpose:  Keeps track of the alarms.
-- Expected entry count: Very, very many.
--*************************************************************************--
create table alarm (
    guid bigint not null generated always as identity primary key,
                                     -- The guid for the alarm.
    component_guid bigint not null,  -- The guid for the component behind the alarm.
    alarm_code varchar(50) not null, -- The code for the alarm.
    alarm_text CLOB not null,        -- The text for the alarm.
    alarm_date timestamp not null,   -- The date for the alarm.
    file_id varchar(255)             -- The id for the file (allowed to be null).
);

create index codeindex on alarm ( alarm_code );
create index alarmdateindex on alarm ( alarm_date );
create index fileidindex on alarm ( file_id );

--*************************************************************************--
-- Name:     component
-- Descr.:   Container for the components ids and their guids.
-- Purpose:  Keeps track of the different component ids. 
-- Expected entry count: Few.
--*************************************************************************--
create table component (
    component_guid bigint not null generated always as identity primary key,
                                    -- The guid for the component id.
    component_id varchar(255)       -- The actual component id.
);

create index componentindex on component ( component_id );
