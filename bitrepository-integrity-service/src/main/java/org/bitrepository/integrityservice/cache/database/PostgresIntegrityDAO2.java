package org.bitrepository.integrityservice.cache.database;

import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

public class PostgresIntegrityDAO2 extends IntegrityDAO2 {

    public PostgresIntegrityDAO2(DBConnector dbConnector, Settings settings) {
        super(dbConnector, settings);
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
    protected String getFindMissingFilesAtPillarSql() {
        String findMissingFilesSql = "SELECT DISTINCT(fileID) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " EXCEPT SELECT fileID FROM fileinfo"
                    + " WHERE collectionID = ?"
                    + " AND pillarID = ?"
                + " ORDER BY fileID"
                + " OFFSET ?"
                + " LIMIT ?";
        return findMissingFilesSql;
    }

}
