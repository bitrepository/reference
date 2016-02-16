---
-- #%L
-- Bitrepository Integrity Service
-- %%
-- Copyright (C) 2010 - 2016 The State and University Library, The Royal Library and The State Archives, Denmark
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as
-- published by the Free Software Foundation, either version 2.1 of the
-- License, or (at your option) any later version.
-- 
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Lesser Public License for more details.
-- 
-- You should have received a copy of the GNU General Lesser Public
-- License along with this program.  If not, see
-- <http://www.gnu.org/licenses/lgpl-2.1.html>.
-- #L%
---
CREATE INDEX checksumdateindex ON fileinfo2(checksum_timestamp);
CREATE INDEX lastseenindex ON fileinfo2(last_seen_getfileids);

SELECT pg_catalog.setval('collectionstats2_collectionstat_key_seq', (SELECT max(collectionstat_key) from collectionstats), true);
SELECT pg_catalog.setval('pillarstats2_pillarstat_key_seq', (SELECT max(pillarstat_key) from pillarstats), true);
SELECT pg_catalog.setval('stats2_stat_key_seq', (SELECT max(stat_key) from stats), true);

