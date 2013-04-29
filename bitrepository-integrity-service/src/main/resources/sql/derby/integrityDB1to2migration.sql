-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:integritydb';

-- Update table versions.
UPDATE tableversions SET version=2 WHERE tablename='fileinfo';
UPDATE tableversions SET version=2 WHERE tablename='pillar';
UPDATE tableversions SET version=2 WHERE tablename='files';
INSERT INTO tableversions (tablename, version) VALUES ('integritydb', 2);
INSERT INTO tableversions (tablename, version) VALUES ('stats', 1);
INSERT INTO tableversions (tablename, version) VALUES ('collectionstats', 1);
INSERT INTO tableversions (tablename, version) VALUES ('pillarstats', 1);

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
    ADD COLUMN collection_key BIGINT NOT NULL,
    ADD FOREIGN KEY (collection_key) REFERENCES collections(collection_key),
    ADD UNIQUE (file_id, collection_key)
);

RENAME COLUMN files.files_guid TO files_key;
CREATE INDEX collectionfileindex ON files (file_id, collection_key);

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


CREATE TABLE stats (
    stat_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    starttime TIMESTAMP,         
    last_update TIMESTAMP 
);

CREATE TABLE collectionstats (
    collectionstat_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stat_key BIGINT NOT NULL,
    collection_key BIGINT NOT NULL,
    file_count BIGINT,
    file_size BIGINT, 
    checksum_errors_count BIGINT, 
    UNIQUE (stat_key, collection_key), 
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key),
    FOREIGN KEY (collection_key) REFERENCES collections(collection_key)
);

CREATE TABLE pillarstats (
    pillarstat_key BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stat_key BIGINT NOT NULL,
    pillar_key BIGINT NOT NULL,
    collection_key BIGINT NOT NULL,
    file_count BIGINT, 
    file_size BIGINT,  
    missing_files_count BIGINT,
    checksum_errors_count BIGINT, 
    UNIQUE (stat_key, pillar_key), 
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key),
    FOREIGN KEY (pillar_key) REFERENCES pillar(pillar_key),
    FOREIGN KEY (collection_key) REFERENCES collections(collection_key)
);
