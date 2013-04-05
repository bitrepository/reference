-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:integritydb;create=true';

-- Update table versions
UPDATE tableversions SET version=2 WHERE tablename='fileinfo'

ALTER TABLE fileinfo (
    ADD FOREIGN KEY (file_guid) REFERENCES files(file_guid),
    ADD FOREIGN KEY (pillar_guid) REFERENCES pillar(pillar_guid),
    ADD UNIQUE (file_guid, pillar_guid) 
)
