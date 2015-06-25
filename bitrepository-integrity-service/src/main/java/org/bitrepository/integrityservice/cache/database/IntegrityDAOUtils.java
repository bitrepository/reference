package org.bitrepository.integrityservice.cache.database;

import java.util.List;
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
    
    
    public IntegrityDAOUtils(DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        this.dbConnector = dbConnector;
    }
    
    /**
     *  @return The list of collections defined in the database.
     */
    public List<String> retrieveCollectionsInDatabase() {
        String selectSql = "SELECT collectionID FROM collections";
        return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
    }

}
