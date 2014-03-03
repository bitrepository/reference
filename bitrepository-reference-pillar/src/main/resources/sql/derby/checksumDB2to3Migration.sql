
connect 'jdbc:derby:checksumdb';

-- Update table versions.
UPDATE tableversions SET version = 3 WHERE tablename = 'checksums';

CREATE INDEX calculationindex ON checksums ( calculationdate );
