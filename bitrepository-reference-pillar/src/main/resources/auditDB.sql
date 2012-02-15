-- TODO: HEADER

connect 'jdbc:derby:auditdb;create=true';

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
            values ( 'audit', 1);
insert into tableversions ( tablename, version )
            values ( 'file', 1);
insert into tableversions ( tablename, version )
            values ( 'actor', 1);


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
    file_id bigint,                 -- The identifier for the file. Used to lookup in the file table.
    actor_id bigint,                -- The identifier for the actor which performed the action for the audit. 
                                    -- Used for looking up in the 
    action varchar(100),            -- The name of the action behind the audit.
    action_date date                -- The date when the action was performed.
);

create index sequenceindex on audittrail ( sequence_number );
create index dateindex on audittrail ( action_date );
create index fileidindex on audittrail ( file_id );

--*************************************************************************--
-- Name:     file
-- Descr.:   Container for the files ids and their guids.
-- Purpose:  Keeps track of the different file ids. 
-- Expected entry count: A lot. Though not as many as 'audittrail'.
--*************************************************************************--
create table file (
    file_guid bigint not null generated always as identity primary key,
                                    -- The guid for the file id.
    fileid varchar(255)             -- The actual file id.
);

create index fileindex on file ( fileid );

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
