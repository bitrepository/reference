CREATE INDEX checksumdateindex ON fileinfo2(checksum_timestamp);
CREATE INDEX lastseenindex ON fileinfo2(last_seen_getfileids);

SELECT pg_catalog.setval('collectionstats2_collectionstat_key_seq', (SELECT max(collectionstat_key) from collectionstats), true);
SELECT pg_catalog.setval('pillarstats2_pillarstat_key_seq', (SELECT max(pillarstat_key) from pillarstats), true);
SELECT pg_catalog.setval('stats2_stat_key_seq', (SELECT max(stat_key) from stats), true);

