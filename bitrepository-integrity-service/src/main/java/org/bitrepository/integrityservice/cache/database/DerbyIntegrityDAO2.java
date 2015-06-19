package org.bitrepository.integrityservice.cache.database;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

public class DerbyIntegrityDAO2 extends IntegrityDAO2 {

    public DerbyIntegrityDAO2(DBConnector dbConnector, Settings settings) {
        super(dbConnector, settings);
    }

    @Override
    protected String getFindMissingFilesAtPillarSql() {
        String findMissingFilesSql = "SELECT DISTINCT(fileID) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " EXCEPT SELECT fileID FROM fileinfo"
                    + " WHERE collectionID = ?"
                    + " AND pillarID = ?"
                + " ORDER BY fileID"
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

}
