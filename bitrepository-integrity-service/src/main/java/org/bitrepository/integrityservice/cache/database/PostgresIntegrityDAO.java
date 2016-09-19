/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.integrityservice.cache.database;

import java.util.List;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

/**
 * Class handling the specifics of IntegrityDAO when the database is based on Postgresql 
 */
public class PostgresIntegrityDAO extends IntegrityDAO {

    public PostgresIntegrityDAO(DBConnector dbConnector) {
        super(dbConnector);
    }

    @Override
    protected synchronized void initializePillars() {
        List<String> pillars = SettingsUtils.getAllPillarIDs();
        for(String pillar : pillars) {
            String sql = "INSERT INTO pillar (pillarID)"
                    + " (SELECT ? WHERE NOT EXISTS ("
                            + " SELECT pillarID FROM pillar"
                            + " WHERE pillarID = ?))";
            DatabaseUtils.executeStatement(dbConnector, sql, pillar, pillar);
        }
    }
    
    @Override
    protected synchronized void initializeCollections() {
        List<String> collections = SettingsUtils.getAllCollectionsIDs();
        for(String collection : collections) {
            String sql = "INSERT INTO collections (collectionID)"
                    + " (SELECT ? WHERE NOT EXISTS ("
                            + " SELECT collectionID FROM collections"
                            + " WHERE collectionID = ?))";
            DatabaseUtils.executeStatement(dbConnector, sql, collection, collection);
        }
    }

    @Override
    protected String getFindFilesWithMissingCopiesSql() {
        String findFilesSql = "SELECT fileid FROM fileinfo"
                + " WHERE collectionid = ?"
                + " GROUP BY fileid"
                + " HAVING COUNT(fileid) < ?"
                + " OFFSET ?"
                + " LIMIT ?";

        return findFilesSql;
    }

    @Override
    protected String getAllFileIDsSql() {
        String getAllFileIDsSql = "SELECT fileID FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " ORDER BY fileID"
                + " OFFSET ?"
                + " LIMIT ?";
        return getAllFileIDsSql;
    }

    @Override
    protected String getLatestCollectionStatsSql() {
        String latestCollectionStatSql = "SELECT file_count, file_size, checksum_errors_count, latest_file_date,"
                + " stat_time, last_update FROM collectionstats"
                + " JOIN stats ON collectionstats.stat_key = stats.stat_key"
                + " WHERE stats.collectionID = ?"
                + " ORDER BY stats.stat_time DESC"
                + " LIMIT ?";
        return latestCollectionStatSql;
    }

    @Override
    protected String getFileIdAtIndexSql() {
        String getFileIDAtIndexSql = "SELECT DISTINCT( fileID ) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " OFFSET ? "
                + " LIMIT ?";
        return getFileIDAtIndexSql;
    }
}
