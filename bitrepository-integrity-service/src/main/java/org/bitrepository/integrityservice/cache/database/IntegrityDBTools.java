package org.bitrepository.integrityservice.cache.database;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTIONS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TABLE;

import java.util.List;

import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

public class IntegrityDBTools {

    /** The connector to the database.*/
    private final DBConnector dbConnector;
    
    public IntegrityDBTools(DBConnector dbConnector) {
        this.dbConnector = dbConnector;
    }
   
    /**
     * Method to add a new collection to the integrity db.
     * @param collectionID, the ID of the collection to add. 
     * @throws IntegrityDBStateException if the collectionID already exist in the DB.  
     */
    public void addCollection(String collectionID) throws IntegrityDBStateException {
        List<String> existingCollections = getCollectionList();
        if(existingCollections.contains(collectionID)) {
            throw new IntegrityDBStateException("Collection '" + collectionID +"' already exists in integrityDB, can't add.");
        }
        
        String sql = "INSERT INTO " + COLLECTIONS_TABLE + " ( " + COLLECTION_ID + " ) VALUES ( ? )";
        DatabaseUtils.executeStatement(dbConnector, sql, collectionID);
        
    }
    /**
     * Method to remove a collectionID from the database. 
     * The process of removing the collection ID includes removing rows with foreign key references to the collection. 
     * Thus the tables, files, fileinfo, stats, collectionstats, pillarstats and collections are touched. 
     * @param collectionID, The ID of the collection to remove. 
     * @throws IntegrityDBStateException if the collectionID does not already exist in the database
     */
    public void removeCollection(String collectionID) throws IntegrityDBStateException {
        List<String> existingCollections = getCollectionList();
        if(!existingCollections.contains(collectionID)) {
            throw new IntegrityDBStateException("Collection '" + collectionID +"' is not present in collection, can't remove.");
        }
        
        Long collectionKey = getCollectionKey(collectionID);
        
        String purgeFileInfoForCollectionSql = "DELETE FROM " + FILE_INFO_TABLE
                + " WHERE EXISTS ( SELECT 1 FROM " + FILES_TABLE 
                    + " WHERE " + COLLECTION_KEY + " = ?)";
        DatabaseUtils.executeStatement(dbConnector, purgeFileInfoForCollectionSql, collectionKey);
                
        String purgeFilesForCollectionSql = "DELETE FROM files WHERE collection_key = ?";
        DatabaseUtils.executeStatement(dbConnector, purgeFilesForCollectionSql, collectionKey);
        
        String purgeCollectionStatsSql = "DELETE FROM " + COLLECTION_STATS_TABLE
                + " WHERE EXISTS (SELECT 1 FROM " + STATS_TABLE 
                    + " WHERE " + COLLECTION_KEY + " = ?)";
        DatabaseUtils.executeStatement(dbConnector, purgeCollectionStatsSql, collectionKey);
                
        String purgePillarStatsSql = "DELETE FROM " + PILLAR_STATS_TABLE
                + " WHERE EXISTS (SELECT 1 FROM " + STATS_TABLE 
                + " WHERE " + COLLECTION_KEY + " = ?)";
        DatabaseUtils.executeStatement(dbConnector, purgePillarStatsSql, collectionKey);

        String purgeStatsSql = "DELETE FROM " + STATS_TABLE + " WHERE " + COLLECTION_KEY + " = ?";
        DatabaseUtils.executeStatement(dbConnector, purgeStatsSql, collectionKey);
        
        String removeCollectionIDSql = "DELETE FROM " + COLLECTIONS_TABLE + " WHERE " + COLLECTION_ID + " = ?";
        DatabaseUtils.executeStatement(dbConnector, removeCollectionIDSql, collectionID);;
        
    }
    
    private List<String> getCollectionList() {
        String selectSql = "SELECT " + COLLECTION_ID + " FROM " + COLLECTIONS_TABLE;
        return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
    }
    
    private Long getCollectionKey(String collectionID) {
        Long key;
        String sql = "SELECT " + COLLECTION_KEY + " FROM " + COLLECTIONS_TABLE 
                + " WHERE " + COLLECTION_ID + "= ?";
        key = DatabaseUtils.selectLongValue(dbConnector, sql, collectionID);
        return key;
    }
    
}
