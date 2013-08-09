package org.bitrepository.integrityservice.cache.database;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTIONS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrityDAOUtils {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The connector to the database.*/
    protected final DBConnector dbConnector;
    /** Caching of CollectionKeys (mapping from ID to Key) */
    private final Map<String, Long> collectionKeyCache;
    
    
    public IntegrityDAOUtils(DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        this.dbConnector = dbConnector;
        collectionKeyCache = new HashMap<String, Long>();
    }
    
    /**
     *  @return The list of collections defined in the database.
     */
    public List<String> retrieveCollectionsInDatabase() {
        String selectSql = "SELECT " + COLLECTION_ID + " FROM " + COLLECTIONS_TABLE;
        return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
    }
    
    /**
     * Method to retrieve the database key for the given collectionId
     * @param collectionId The ID of the collection 
     */
    protected Long retrieveCollectionKey(String collectionId) {
        Long key = collectionKeyCache.get(collectionId);
        if(key == null) {
            log.trace("Retrieving key for collection '{}'.", collectionId);
            String sql = "SELECT " + COLLECTION_KEY + " FROM " + COLLECTIONS_TABLE 
                    + " WHERE " + COLLECTION_ID + "= ?";
            key = DatabaseUtils.selectLongValue(dbConnector, sql, collectionId);
            collectionKeyCache.put(collectionId, key);
        }
        return key;
    }
}
