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
create table tableversions (
    tablename varchar(100) not null, -- Name of table
    version int not null             -- version of table
);

insert into tableversions ( tablename, version )
            value ( 'fileinfo', 1);
insert into tableversions ( tablename, version )
            value ( 'file', 1);
insert into tableversions ( tablename, version )
            value ( 'pillar', 1);
insert into tableversions ( tablename, version )
            value ( 'checksum', 1);

--*************************************************************************--
-- Name:     fileinfo
-- Descr.:   The main table for containing the information about the files
--           on the different pillars.
-- Purpose:  Keeps track of the information connected to a specific file id
--           on a specific pillar.
-- Expected entry count: Very, very many.
--*************************************************************************--
create table fileinfo (
    guid bigint not null generated always as indentity primary key,
                                 -- The unique id for a specific file on a specific pillar.
    file_guid bigint not null,   -- The guid for the file.
    pillar_guid bigint not null, -- The guid for the pillar.
    checksum_spec bigint,        -- The guid for the specific checksum, which
                                 -- was used for the latest checksum calculation.
    checksum varchar(100),       -- The checksum for the given file on the given pillar.
    last_file_update date,       -- The last time a 'GetFileIDs' for the fileinfo has been answered.
    last_checksum_update date    -- The date for the latest checksum calculation.
);

create index fileindex on fileinfo ( file_guid);
create index filepillarindex on fileinfo ( file_guid, pillar_guid );
create index checksumdateindex on fileinfo ( last_checksum_update );

--*************************************************************************--
-- Name:     file
-- Descr.:   Contains the information about the file.
-- Purpose:  Keeps track of the names of the files within the system.
-- Expected entry count: Very, very many.
--*************************************************************************--
create table file (
    file_guid bigint not null generated always as identity primary key,
                                 -- The guid for a given file.
    file_id varchar (255) not null,
                                 -- The id for the file.
    creation_date date,          -- The date for the creation of the file.
);

create index fileindex on file ( file_id );
create index filedateindex on file ( creation_date );

--*************************************************************************--
-- Name:     pillar
-- Descr.:   Contains the information about the pillars.
-- Purpose:  Keeps track of the information about the pillars.
-- Expected entry count: Few
--*************************************************************************--
create table pillar (
    pillar_guid bigint not null generated always as identity primary key,
                                 -- The GUID for the pillar.
    pillar_id not null,          -- The id of the pillar.
    checksum_spec_guid bigint    -- If it is a ChecksumPillar, then this 
                                 -- would be refering to the type of checksum
                                 -- the pillar is using.
);

create index pillarindex on pillar ( pillar_id );

--*************************************************************************--
-- Name:     checksumspec
-- Descr.:   Contains the information about the cheksum specifications.
-- Purpose:  Keeps track of the different checksum specification. E.g. the 
--           name of the algorithm and the salt.
-- Expected entry count: Some, though not many
--*************************************************************************--
create table checksumspec (
    checksum_guid bigint not null generated always as identity primary key,
                                 -- The guid for this checksum specification.
    checksum_algorithm varchar(100) not null,
                                 -- The name of the algorithm for the checksum.
    checksum_salt varchar(100) not null,
                                 -- The salt for the checksum calculation.
);

create index checksumindex on checksumspec ( checksum_algorithm, checksum_salt );
