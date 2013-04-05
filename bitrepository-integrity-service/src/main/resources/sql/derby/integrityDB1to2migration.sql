-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:integritydb;create=true';

-- Update table versions.
UPDATE tableversions SET version=2 WHERE tablename='fileinfo';

-- Add constraints to fileinfo table.
ALTER TABLE fileinfo (
    ADD FOREIGN KEY (file_guid) REFERENCES files(file_guid),
    ADD FOREIGN KEY (pillar_guid) REFERENCES pillar(pillar_guid),
    ADD UNIQUE (file_guid, pillar_guid) 
);

-- Add collections table. 
CREATE TABLE collections (
    collection_guid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    collection_id VARCHAR(255) NOT NULL,
);

CREATE INDEX collectionindex ON collections (collection_id);

-- Add version information for the new table collections.
INSERT INTO tableversions ( tablename, version ) VALUES ( 'collections', 1);
