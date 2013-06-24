package org.bitrepository.integrityservice.cache.database;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_CHECKSUM_ERRORS;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILECOUNT;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILESIZE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_STAT_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_LAST_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TIME;

import org.bitrepository.service.database.DBConnector;
import org.bitrepository.settings.repositorysettings.Collections;

/**
 * DAO class implementing the specifics for a Derby based backend for the integrityDB 
 */
public class DerbyIntegrityDAO extends IntegrityDAO {

    public DerbyIntegrityDAO(DBConnector dbConnector, Collections collections) {
        super(dbConnector, collections);
    }

    @Override
    protected String getLatestCollectionStatsSQL() {
        return "SELECT c." + CS_FILECOUNT +  ", c." + CS_FILESIZE + ", c." + CS_CHECKSUM_ERRORS
                + ", s." + STATS_TIME + ", s." + STATS_LAST_UPDATE 
                + " FROM " + COLLECTION_STATS_TABLE + " c "
                + " JOIN " + STATS_TABLE + " s" 
                + " ON  c." + CS_STAT_KEY + " = s." + STATS_KEY
                + " WHERE s." + STATS_COLLECTION_KEY + " = ?"
                + " ORDER BY s." + STATS_TIME + " DESC "
                + " FETCH FIRST ? ROWS ONLY";
    }

    @Override
    protected String getLatestStatisticsKeySQL() {
        return "SELECT " + STATS_KEY + " FROM " + STATS_TABLE
                + " WHERE " + STATS_COLLECTION_KEY + " = ?"
                + " ORDER BY " + STATS_KEY + " DESC"
                + " FETCH FIRST ROW ONLY ";
    }
    
}
