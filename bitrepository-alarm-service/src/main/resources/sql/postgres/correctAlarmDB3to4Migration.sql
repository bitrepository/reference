ALTER TABLE alarm RENAME COLUMN alarm_date2 TO alarm_date;

ALTER TABLE alarm ALTER COLUMN alarm_date SET NOT NULL;

CREATE INDEX alarmdateindex ON alarm (alarm_date);
