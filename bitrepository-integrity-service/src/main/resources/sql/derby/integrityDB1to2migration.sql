-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:integritydb';

-- Update table versions.
UPDATE tableversions SET version=2 WHERE tablename='fileinfo';
UPDATE tableversions SET version=2 WHERE tablename='pillar';
UPDATE tableversions SET version=2 WHERE tablename='files';
INSERT INTO tableversions (tablename, version) VALUES ('integritydb', 2);

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
    UNIQUE (collection_id)
);

CREATE INDEX collectionindex ON collections (collection_id);

-- Add version information for the new table collections.
INSERT INTO tableversions ( tablename, version ) VALUES ( 'collections', 1);

-- Add constraints to files table.
ALTER TABLE files (
    ADD UNIQUE (file_id)
);

-- Add constraints to files table.
ALTER TABLE pillar (
    ADD UNIQUE (pillar_id)
);


