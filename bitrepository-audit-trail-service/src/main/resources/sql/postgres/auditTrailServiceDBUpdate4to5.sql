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

CREATE TABLE collection_progress (
    collectionID VARCHAR(255) NOT NULL,
    contributorID VARCHAR(255) NOT NULL,
    latest_sequence_number BIGINT,
   
    FOREIGN KEY (collectionID) REFERENCES collection(collectionid),
    FOREIGN KEY (contributorID) REFERENCES contributor(contributor_id),
    UNIQUE (collectionID, contributorID)
);

INSERT INTO collection_progress (latest_sequence_number, collectionID, contributorID) (
SELECT MAX(sequence_number), collectionid, contributor_id FROM audittrail
JOIN file ON audittrail.file_key = file.file_key 
JOIN collection ON file.collection_key = collection.collection_key
JOIN contributor ON audittrail.contributor_key = contributor.contributor_key
GROUP BY collectionid, contributor_id);

UPDATE tableversions SET version = 5 WHERE tablename = 'auditservicedb';
INSERT INTO tableversions ( tablename, version ) VALUES ( 'collection_progress', 1); 
