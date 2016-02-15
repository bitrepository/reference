

UPDATE collection_progress SET latest_file_timestamp2 = (EXTRACT (epoch FROM latest_file_timestamp AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);
UPDATE collection_progress SET latest_checksum_timestamp2 = (EXTRACT (epoch FROM latest_checksum_timestamp AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);

ALTER TABLE collection_progress DROP COLUMN latest_file_timestamp;
ALTER TABLE collection_progress DROP COLUMN latest_checksum_timestamp;

ALTER TABLE collection_progress RENAME COLUMN latest_file_timestamp2 TO latest_file_timestamp;
ALTER TABLE collection_progress RENAME COLUMN latest_checksum_timestamp2 TO latest_checksum_timestamp;


-- Migrate stats table
ALTER TABLE stats ADD COLUMN stat_time2 BIGINT;
ALTER TABLE stats ADD COLUMN last_update2 BIGINT;

DROP INDEX lastupdatetimeindex;

UPDATE stats SET stat_time2 = (EXTRACT (epoch FROM stat_time AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);
UPDATE stats SET last_update2 = (EXTRACT (epoch FROM last_update AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);

ALTER TABLE stats DROP COLUMN stat_time;
ALTER TABLE stats DROP COLUMN last_update;

ALTER TABLE stats RENAME COLUMN stat_time2 TO stat_time;
ALTER TABLE stats RENAME COLUMN last_update2 TO last_update;

ALTER TABLE stats ALTER COLUMN stat_time SET NOT NULL;
ALTER TABLE stats ALTER COLUMN last_update SET NOT NULL;

CREATE INDEX lastupdatetimeindex ON stats (last_update);


-- Migrate collectionstats table
ALTER TABLE collectionstats ADD COLUMN latest_file_date2 BIGINT;

UPDATE collectionstats SET latest_file_date2 = (EXTRACT (epoch FROM latest_file_date AT TIME ZONE (SELECT current_setting('TIMEZONE'))) * 1000);

ALTER TABLE collectionstats DROP COLUMN latest_file_date;

ALTER TABLE collectionstats RENAME COLUMN latest_file_date2 TO latest_file_date;

ALTER TABLE collectionstats ALTER COLUMN latest_file_date SET NOT NULL;

