-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:alarmservicedb';

-- Update table versions.
UPDATE tableversions SET version=2 WHERE tablename='alarm';


-- Alter alarm table. 
ALTER TABLE alarm (
    ADD COLUMN collection_id VARCHAR(255),
    ADD FOREIGN KEY (component_guid) REFERENCES component(component_guid)
);

CREATE INDEX collectionidindex ON alarm ( collection_id );

