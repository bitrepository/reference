-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:integritydb';

-- Update table versions.
UPDATE tableversions SET version=2 WHERE tablename='fileinfo';
UPDATE tableversions SET version=2 WHERE tablename='pillar';
UPDATE tableversions SET version=2 WHERE tablename='files';
INSERT INTO tableversions (tablename, version) VALUES ('integritydb', 2);

-- Add collections table. 
CREATE TABLE collections (
    collection_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    collection_id VARCHAR(255) NOT NULL,
    UNIQUE (collection_id)
);

CREATE INDEX collectionindex ON collections (collection_id);

-- Add version information for the new table collections.
INSERT INTO tableversions ( tablename, version ) VALUES ( 'collections', 1);

-- Add constraints to files table.
ALTER TABLE files (
    ADD UNIQUE (file_id),
);

RENAME COLUMN files.files_guid TO files_key;

-- Add constraints to files table.
ALTER TABLE pillar (
    ADD UNIQUE (pillar_id)
);

RENAME COLUMN pillar.pillar_guid TO pillar_key;

RENAME COLUMN fileinfo.file_guid TO file_key
RENAME COLUMN fileinfo.pillar_guid TO pillar_key

-- Add constraints to fileinfo table.
ALTER TABLE fileinfo (
    ADD COLUMN file_size BIGINT
    ADD FOREIGN KEY (file_key) REFERENCES files(file_key),
    ADD FOREIGN KEY (pillar_key) REFERENCES pillar(pillar_key),
    ADD UNIQUE (file_key, pillar_key) 
);

RENAME COLUMN fileinfo.guid TO fileinfo_key;

CREATE INDEX filekeyindex ON fileinfo (file_key);


