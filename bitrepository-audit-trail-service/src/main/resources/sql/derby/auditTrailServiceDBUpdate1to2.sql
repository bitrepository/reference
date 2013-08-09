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

connect 'jdbc:derby:auditservicedb';

-- TODO before running this script, please replace the placeholder(s):
TODO collection_id_placeholder
EXIT

-- Insert the version of the database
insert into tableversions ( tablename, version ) values ( 'auditservicedb', 2);

-- collection table version 1:
-- Create the collection table
-- set it to version one
-- and add a collection.
create table collection (
    collection_key bigint not null generated always as identity primary key,
                                    -- The key for the entry in the collection table.
    collectionid varchar(255)       -- The actual id of the collection.
);
insert into tableversions ( tablename, version ) values ( 'collection', 1); -- TODO fix placeholder:
insert into collection ( collectionid ) values ( 'collection_id_placeholder' );

-- file table version 1->2:
-- add the collection_key column to the table.
-- set the value to the key of the default collection.
-- change the column file_guid to file_key
-- update the version to 2.
alter table file add column collection_key bigint; 
-- TODO fix placeholder:
update file set collection_key = ( select collection_key from collection where collectionid = 'collection_id_placeholder') 
            where collection_key is null;
rename column file.file_guid to file_key;
update tableversions set version = 2 where tablename = 'file';


-- contributor table version 1->2:
-- Change the column contributor_guid to contributor_key
-- update the version number to 2
rename column contributor.contributor_guid to contributor_key;
update tableversions set version = 2 where tablename = 'contributor';

-- actor table version 1->2:
-- Change the column actor_guid to actor_key
-- update the version number to 2
rename column actor.actor_guid to actor_key;
update tableversions set version = 2 where tablename = 'actor';

-- audittrail table version 1->2:
-- Change the column audit_guid to audit_key
-- Change the column contributor_guid to contributor_key
-- Change the column file_guid to file_key
-- Change the column actor_guid to actor_key
-- update the version number to 2
rename column audittrail.audit_guid to audit_key;
rename column audittrail.contributor_guid to contributor_key;
rename column audittrail.file_guid to file_key;
rename column audittrail.actor_guid to actor_key;
update tableversions set version = 2 where tablename = 'auditauditservicedb';
