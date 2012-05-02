/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.store;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface to the database for storing the Alarms.
 */
public class AlarmServiceDAO implements AlarmStore {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The connection to the database.*/
    private DBConnector dbConnector;
    /** The settings.*/
    private final Settings settings;
    
    /** 
     * Constructor.
     * @param settings The settings.
     */
    public AlarmServiceDAO(Settings settings) {
        this.settings = settings;
        
        // TODO make a better instantiation, which is not depending on Derby.
        dbConnector = new DerbyDBConnector();
        
        try {
            getConnection();
        } catch (IllegalStateException e) {
            log.warn("No existing database.", e);
            initDatabaseConnection();
            getConnection();
        }
    }
    
    /**
     * Retrieve the access to the database. If it cannot be done, then it is automatically attempted to instantiate 
     * the database based on the SQL script.
     * @return The connection to the database.
     */
    private void initDatabaseConnection() {
        log.info("Trying to instantiate the database.");
        // TODO handle this!
        //        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
        //                "src/main/resources/integrityDB.sql");
        File sqlDatabaseFile = new File("src/main/resources/alarmServiceDB.sql");
        dbConnector.createDatabase(sqlDatabaseFile);
    }
    
    /**
     * Retrieve the connection to the database.
     * TODO improve performance (only reconnect every-so-often)... 
     * @return The connection to the database.
     */
    protected Connection getConnection() {
        try { 
            Connection dbConnection = dbConnector.getEmbeddedDBConnection(
                    settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabaseUrl());
            return dbConnection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate the database with the url '"
                    + settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabaseUrl() + "'", e);
        }
    }
    
    @Override
    public void addAlarm(Alarm alarm) {
        AlarmDatabaseIngestor ingestor = new AlarmDatabaseIngestor(getConnection());
        ingestor.ingestAlarm(alarm);
    }
    
    @Override
    public List<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate,
            String fileID, Integer count) {
        AlarmDatabaseExtractionModel extractModel = new AlarmDatabaseExtractionModel(componentID, alarmCode, minDate, 
                maxDate, fileID, count);
        
        AlarmDatabaseExtractor extractor = new AlarmDatabaseExtractor(extractModel, getConnection());
        return extractor.extractAuditEvents();
    }
    
}
