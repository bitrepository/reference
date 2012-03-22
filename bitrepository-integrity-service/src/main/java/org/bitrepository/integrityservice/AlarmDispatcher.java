/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.integrityservice.checking.IntegrityReport;

/**
 * The interface for the alarm dispatcher.
 */
public interface AlarmDispatcher {
    /**
     * Sends an alarm based on an integrity report.
     * @param report The report to base the alarm upon.
     */
    void integrityFailed(IntegrityReport report);
    
    /**
     * Method for sending an Alarm when something bad happens.
     * @param alarmConcerning What the alarm is concerning.
     * @param alarm The description of the alarm, e.g. What caused the alarm.
     */
    void sendAlarm(Alarm alarm);
}
