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
import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;

/**
 * The interface to the database for storing the Alarms.
 */
public class AlarmServiceDAO implements AlarmStore {
    /** The connector to the database.*/
    private DBConnector dbConnector;
    
    /** 
     * Constructor.
     * @param settings The settings.
     */
    public AlarmServiceDAO(Settings settings) {
        dbConnector = new DBConnector(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());
    }
    
    @Override
    public void addAlarm(Alarm alarm) {
        AlarmDatabaseIngestor ingestor = new AlarmDatabaseIngestor(dbConnector);
        ingestor.ingestAlarm(alarm);
    }
    
    @Override
    public List<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate,
            String fileID, String collectionID, Integer count, boolean ascending) {
        AlarmDatabaseExtractionModel extractModel = new AlarmDatabaseExtractionModel(collectionID, componentID, alarmCode, 
                minDate, maxDate, fileID, count, ascending);
        
        AlarmDatabaseExtractor extractor = new AlarmDatabaseExtractor(extractModel, dbConnector);
        return extractor.extractAlarms();
    }


    @Override
    public void start() {
        //Nothing to do.
    }

    @Override
    public void shutdown() {
        dbConnector.destroy();
    }
}
