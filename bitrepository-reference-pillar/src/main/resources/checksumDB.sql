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

connect 'jdbc:derby:checksumdb;create=true';

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
            values ( 'checksums', 1);


--*************************************************************************--
-- Name:     checksums
-- Descr.:   Container for the checksum entry information: the file ids, their 
--           checksums and the timestamp of the calculation of this checksum. 
-- Purpose:  Keep track of the checksum entries. 
-- Expected entry count: Very many, one for each file..
--*************************************************************************--
create table checksums (
    sequence_number bigint not null generated always as identity primary key,
                                    -- The sequence number and unique key for this table.
    fileid varchar(255) not null,   -- The id of the file.
    checksum varchar(255),          -- The checksum of the file.
    calculationdate timestamp       -- The timestamp for the calculation of the checksum.
);

create index fileindex on checksums ( fileid );
create index filedateindex on checksums ( fileid, calculationdate );
