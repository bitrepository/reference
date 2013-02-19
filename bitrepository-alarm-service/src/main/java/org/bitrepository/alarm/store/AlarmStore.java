/*
 * #%L
 * Bitrepository Alarm Service
 * 
 * $Id$
 * $HeadURL$
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

import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.service.LifeCycledService;

/**
 * The AlarmStore is where the alarms are persisted.
 */
public interface AlarmStore extends LifeCycledService{
    /**
     * Add an alarm to the store.
     * @param alarm The alarm to be added to the store.
     */
    void addAlarm(Alarm alarm);
    
    /**
     * Extracts the alarms based on the given optional restictions.
     * @param componentID [OPTIONAL] The id of the component.
     * @param alarmCode [OPTIONAL] The alarm code.
     * @param minDate [OPTIONAL] The earliest date for the alarms.
     * @param maxDate [OPTIONAL] The latest date for the alarms.
     * @param fileID [OPTIONAL] The id of the file, which the alarms are connected.
     * @param count [OPTIONAL] The maximum number of alarms to retrieve from the store.
     * @param ascending Whether the alarms should be delivered ascending.
     * @return The requested collection of alarms from the store.
     */
    Collection<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate, 
            String fileID, Integer count, boolean ascending);
}
