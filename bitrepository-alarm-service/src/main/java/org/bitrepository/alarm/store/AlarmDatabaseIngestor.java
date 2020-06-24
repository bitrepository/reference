/*
 * #%L
 * Bitrepository Audit Trail Service
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

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_CODE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_COLLECTION_ID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_COMPONENT_GUID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_DATE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_FILE_ID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_TABLE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_TEXT;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_GUID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_ID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_TABLE;

/**
 * Handles the ingest of the Alarm messages into the database.
 * 
 * Ingested in the following order:
 * ALARM_COMPONENT_GUID
 * ALARM_CODE
 * ALARM_TEXT
 * ALARM_DATE
 * ALARM_FILE_ID
 * ALARM_COLLECTION_ID
 */
public class AlarmDatabaseIngestor {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The connector to the database, where the alarms should be ingested.*/
    private final DBConnector dbConnector;
    
    /**
     * @param dbConnector The connector to the database, where the audit trails are to be ingested.
     */
    public AlarmDatabaseIngestor(DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        
        this.dbConnector = dbConnector;
    }
    
    /**
     * Ingest the given alarm into the database.
     * @param alarm The alarm to the ingested into the alarm database.
     */
    public void ingestAlarm(Alarm alarm) {
        ArgumentValidator.checkNotNull(alarm, "Alarm alarm");
        
        String sqlInsert = "INSERT INTO " + ALARM_TABLE + " ( " + createIngestElementString(alarm) + " )"
                + " VALUES ( " + createIngestArgumentString(alarm) + " )";
        DatabaseUtils.executeStatement(dbConnector, sqlInsert, extractArgumentsFromEvent(alarm));
    }

    /**
     * @param alarm The alarm to ingest into the database.
     * @return Creates the set of elements to be ingested into the database.
     */
    private String createIngestElementString(Alarm alarm) {
        StringBuilder res = new StringBuilder();
        
        addElement(res, alarm.getAlarmRaiser(), ALARM_COMPONENT_GUID);
        addElement(res, alarm.getAlarmCode(), ALARM_CODE);
        addElement(res, alarm.getAlarmText(), ALARM_TEXT);
        addElement(res, alarm.getOrigDateTime(), ALARM_DATE);
        addElement(res, alarm.getFileID(), ALARM_FILE_ID);
        addElement(res, alarm.getCollectionID(), ALARM_COLLECTION_ID);
        
        return res.toString();
    }
    
    /**
     * Adds the field for a given element to the string builder if the element is not null.
     * @param res The StringBuilder where the restrictions are combined.
     * @param element The element to be ingested. Is validated whether it is null.
     * @param name The name of the field in the database corresponding to the element. 
     */
    private void addElement(StringBuilder res, Object element, String name) {
        if(element == null) {
            return;
        }
        
        if(res.length() == 0) {
            res.append(" ");
        } else {
            res.append(" , ");
        }
        
        res.append(name);
    }
    
    /**
     * @param alarm The alarm to ingest into the database.
     * @return The string for the arguments for the elements of the event to be ingested into the database.
     */
    private String createIngestArgumentString(Alarm alarm) {
        StringBuilder res = new StringBuilder();
        
        addArgument(res, alarm.getAlarmRaiser());
        addArgument(res, alarm.getAlarmCode());
        addArgument(res, alarm.getAlarmText());
        addArgument(res, alarm.getOrigDateTime());
        addArgument(res, alarm.getFileID());
        addArgument(res, alarm.getCollectionID());
        
        return res.toString();
    }
    
    /**
     * Adds a question mark for a given element to the string builder if the element is not null.
     * @param res The StringBuilder where the restrictions are combined.
     * @param element The element to be ingested. Is validated whether it is null.
     */
    private void addArgument(StringBuilder res, Object element) {
        if(element == null) {
            return;
        }
        
        if(res.length() == 0) {
            res.append(" ? ");
        } else {
            res.append(", ? ");
        }
    }

    
    /**
     * @param alarm the alarm
     * @return The list of elements in the model which are not null.
     */
    private Object[] extractArgumentsFromEvent(Alarm alarm) {
        List<Object> res = new ArrayList<>();

        if(alarm.getAlarmRaiser() != null) {
            Long componentGuid = retrieveComponentGuid(alarm.getAlarmRaiser());
            res.add(componentGuid);
        }
        
        if(alarm.getAlarmCode() != null) {
            res.add(alarm.getAlarmCode().toString());
        }
        
        if(alarm.getAlarmText() != null) {
            res.add(alarm.getAlarmText());
        }
        
        if(alarm.getOrigDateTime() != null) {
            res.add(CalendarUtils.convertFromXMLGregorianCalendar(alarm.getOrigDateTime()).getTime());
        }
        
        if(alarm.getFileID() != null) {
            res.add(alarm.getFileID());
        }
        
        if(alarm.getCollectionID() != null) {
            res.add(alarm.getCollectionID());
        }
        
        return res.toArray();
    }
    
    /**
     * Retrieve the guid for a given component. If the component does not exist within the component table, 
     * then it is created.
     * 
     * @param componentId The name of the alarm producing component.
     * @return The guid of the component with the given name.
     */
    private synchronized long retrieveComponentGuid(String componentId) {
        String sqlRetrieve = "SELECT " + COMPONENT_GUID + " FROM " + COMPONENT_TABLE 
                + " WHERE " + COMPONENT_ID + " = ?";
        
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, componentId);
        
        if(guid == null) {
            log.debug("Inserting component '" + componentId + "' into the component table.");
            String sqlInsert = "INSERT INTO " + COMPONENT_TABLE + " ( " + COMPONENT_ID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, componentId);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, componentId);
        }
        
        return guid;
    }
}
