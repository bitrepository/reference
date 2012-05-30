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

import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DBSpecifics;
import org.bitrepository.common.database.DatabaseSpecificsFactory;
import org.bitrepository.common.settings.Settings;

/**
 * The interface to the database for storing the Alarms.
 */
public class AlarmServiceDAO implements AlarmStore {
    /** The connection to the database.*/
    private DBConnector dbConnector;
    
    /** 
     * Constructor.
     * @param settings The settings.
     */
    public AlarmServiceDAO(Settings settings) {
        DBSpecifics dbSpecifics = DatabaseSpecificsFactory.retrieveDBSpecifics(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmDatabaseSpecifics());
        dbConnector = new DBConnector(dbSpecifics, 
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabaseUrl());
    }
    
    @Override
    public void addAlarm(Alarm alarm) {
        AlarmDatabaseIngestor ingestor = new AlarmDatabaseIngestor(dbConnector.getConnection());
        ingestor.ingestAlarm(alarm);
    }
    
    @Override
    public List<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate,
            String fileID, Integer count, boolean ascending) {
        AlarmDatabaseExtractionModel extractModel = new AlarmDatabaseExtractionModel(componentID, alarmCode, minDate, 
                maxDate, fileID, count, ascending);
        
        AlarmDatabaseExtractor extractor = new AlarmDatabaseExtractor(extractModel, dbConnector.getConnection());
        return extractor.extractAlarms();
    }
}
