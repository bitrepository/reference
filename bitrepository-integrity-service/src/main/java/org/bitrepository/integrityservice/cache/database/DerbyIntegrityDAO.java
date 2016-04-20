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

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

/**
 * Class handling the specifics of IntegrityDAO when the database is based on Derby 
 */
public class DerbyIntegrityDAO extends IntegrityDAO {

    public DerbyIntegrityDAO(DBConnector dbConnector) {
        super(dbConnector);
    }

    @Override
    protected String getFindMissingFilesAtPillarSql() {
        String findMissingFilesSql = "SELECT a.fileid FROM"
                + " (SELECT DISTINCT(fileID) FROM fileinfo "
                    + " WHERE collectionID = ?) AS a"
                + " LEFT JOIN "
                + " (SELECT fileID FROM fileinfo "
                    + " WHERE collectionID = ? "
                    + " AND pillarID = ?) AS p"
                + " ON a.fileID = p.fileID"
                + " WHERE p.fileID IS NULL"
                + " OFFSET ? ROWS"
                + " FETCH FIRST ? ROWS ONLY";
        return findMissingFilesSql;
    }

    @Override
    protected synchronized void initializePillars() {
        List<String> pillars = new ArrayList<>(SettingsUtils.getAllPillarIDs());
        
        String getExistingPillars = "SELECT pillarID FROM pillar";
        
        List<String> pillarsInDb = DatabaseUtils.selectStringList(dbConnector, getExistingPillars, new Object[0]);
        pillars.removeAll(pillarsInDb);
        
        for(String pillar : pillars) {
            String sql = "INSERT INTO pillar (pillarID) VALUES (?)";
            DatabaseUtils.executeStatement(dbConnector, sql, pillar);
        }        
    }

    @Override
    protected synchronized void initializeCollections() {
        List<String> collections = new ArrayList<>(SettingsUtils.getAllCollectionsIDs());
        
        String getExistingPillars = "SELECT collectionID FROM collections";
        
        List<String> collectionsInDb = DatabaseUtils.selectStringList(dbConnector, getExistingPillars, new Object[0]);
        collections.removeAll(collectionsInDb);
        
        for(String collection : collections) {
            String sql = "INSERT INTO collections (collectionID) VALUES (?)";
            DatabaseUtils.executeStatement(dbConnector, sql, collection);
        }
    }
    
    @Override
    protected String getAllFileIDsSql() {
        String getAllFileIDsSql = "SELECT fileID FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " ORDER BY fileID"
                + " OFFSET ? ROWS"
                + " FETCH FIRST ? ROWS ONLY";
        return getAllFileIDsSql;
    }
    
    @Override
    protected String getLatestCollectionStatsSql() {
        String latestCollectionStatSql = "SELECT file_count, file_size, checksum_errors_count, latest_file_date,"
                + " stat_time, last_update FROM collectionstats"
                + " JOIN stats ON collectionstats.stat_key = stats.stat_key"
                + " WHERE stats.collectionID = ?"
                + " ORDER BY stats.stat_time DESC"
                + " FETCH FIRST ? ROWS ONLY";
        return latestCollectionStatSql;
    }

    @Override
    protected String getFileIdAtIndexSql() {
        String getFileIDAtIndexSql = "SELECT DISTINCT( fileID ) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " OFFSET ? ROWS"
                + " FETCH FIRST ? ROWS ONLY";
        return getFileIDAtIndexSql;
    }

}
