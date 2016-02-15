ALTER TABLE checksums RENAME COLUMN calculationdate2 TO calculationdate;
CREATE INDEX calculationindex ON checksums ( calculationdate );
