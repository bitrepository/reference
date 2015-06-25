package org.bitrepository.integrityservice.cache.database;

import java.util.List;

import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

public class IntegrityDBTools extends IntegrityDAOUtils {
    
    public IntegrityDBTools(DBConnector dbConnector) {
        super(dbConnector);
    }
   
    /**
     * Method to add a new collection to the integrity db.
     * @param collectionID, the ID of the collection to add. 
     * @throws IntegrityDBStateException if the collectionID already exist in the DB.  
     */
    public void addCollection(String collectionID) throws IntegrityDBStateException {
        List<String> existingCollections = retrieveCollectionsInDatabase();
        if(existingCollections.contains(collectionID)) {
            throw new IntegrityDBStateException("Collection '" + collectionID +"' already exists in integrityDB, can't add.");
        }
        
        String sql = "INSERT INTO collections ( collectionID ) VALUES ( ? )";
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
        List<String> existingCollections = retrieveCollectionsInDatabase();
        if(!existingCollections.contains(collectionID)) {
            throw new IntegrityDBStateException("Collection '" + collectionID +"' is not present in collection, can't remove.");
        }

        String removeCollectionSql = "DELETE FROM collections WHERE collectionID = ? CASCADE";
        DatabaseUtils.executeStatement(dbConnector, removeCollectionSql, collectionID);;
        
    }
    
}
