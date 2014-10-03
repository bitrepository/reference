package org.bitrepository.integrityservice.cache.database;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_CHECKSUM_ERRORS;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILECOUNT;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILESIZE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_STAT_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM_STATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_STATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_CHECKSUM_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_FILE_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_PILLAR_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_LAST_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TIME;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.settings.repositorysettings.Collections;

/**
 * DAO class implementing the specifics for a Derby based backend for the integrityDB 
 */
public class DerbyIntegrityDAO extends IntegrityDAO {

    public DerbyIntegrityDAO(DatabaseManager databaseManager, Collections collections) {
        super(databaseManager, collections);
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

    @Override
    protected String getMissingFilesOnPillarSql() {
        String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                + " JOIN " + FILE_INFO_TABLE 
                + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?"
                + " AND "+ FILES_TABLE + "." + COLLECTION_KEY + "= ?" 
                + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ("
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ? )"
                + " ORDER BY " + FILES_TABLE + "." + FILES_KEY
                + " OFFSET ? ROWS"
                + " FETCH FIRST ? ROWS ONLY";
        
        return selectSql;
    }
    
    @Override
    protected String getFilesOnPillarSql() {
        String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                + " JOIN " + FILE_INFO_TABLE 
                + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?"
                + " AND " + FILES_TABLE + "." + COLLECTION_KEY + "= ?"
                + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ("
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ?)" 
                + " ORDER BY " + FILES_TABLE + "." + FILES_KEY
                + " OFFSET ? ROWS"
                + " FETCH FIRST ? ROWS ONLY";
        
        return selectSql;
    }
    
    @Override
    protected String getFilesWithChecksumErrorsOnPillarSql() {
        String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                + " JOIN " + FILE_INFO_TABLE 
                + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                + " WHERE " + FILE_INFO_TABLE + "." + FI_CHECKSUM_STATE + " = ?"
                + " AND "+ FILES_TABLE + "." + COLLECTION_KEY + "= ?"
                + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = (" 
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ?)" 
                + " ORDER BY " + FILES_TABLE + "." + FILES_KEY
                + " OFFSET ? ROWS"
                + " FETCH FIRST ? ROWS ONLY";
        
        return selectSql;
    }

    @Override
    protected String getDateForNewestFileEntryForCollectionSql() {
        String retrieveSql = "SELECT " + FI_LAST_FILE_UPDATE + " FROM " + FILE_INFO_TABLE 
                + " JOIN " + FILES_TABLE 
                + " ON " + FILE_INFO_TABLE + "." + FILES_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?" 
                + " AND " + FI_FILE_STATE + " = ?" 
                + " ORDER BY " + FI_LAST_FILE_UPDATE + " DESC "
                + " FETCH FIRST ROW ONLY";
        return retrieveSql;
    }

    @Override
    protected String getDateForNewestFileEntryForPillarSql() {
        String retrieveSql = "SELECT " + FI_LAST_FILE_UPDATE + " FROM " + FILE_INFO_TABLE 
                + " JOIN " + FILES_TABLE 
                + " ON " + FILE_INFO_TABLE + "." + FILES_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?" 
                + " AND " + FI_FILE_STATE + " = ?" 
                + " AND " + FI_PILLAR_KEY + " = (" 
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ? )" 
                + " ORDER BY " + FI_LAST_FILE_UPDATE + " DESC "
                + " FETCH FIRST ROW ONLY";
        return retrieveSql;
    }

    @Override
    protected String getDateForNewestChecksumEntryForPillarSql() {
        String retrieveSql = "SELECT " + FI_LAST_CHECKSUM_UPDATE + " FROM " + FILE_INFO_TABLE 
                + " JOIN " + FILES_TABLE 
                + " ON " + FILE_INFO_TABLE + "." + FILES_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?"
                + " AND " + FI_FILE_STATE + " = ?"
                + " AND " + FI_CHECKSUM_STATE + " <> ?"
                + " AND " + FI_PILLAR_KEY + " = ("
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ? )"
                + " ORDER BY " + FI_LAST_CHECKSUM_UPDATE + " DESC "
                + " FETCH FIRST ROW ONLY";
        return retrieveSql;
    }

    

    
}
