/*
 * #%L
 * Bitrepository Alarm Client
 * 
 * $Id: TestAlarmHandler.java 239 2011-07-22 13:51:09Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-alarm-client/src/test/java/org/bitrepository/alarm/TestAlarmHandler.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.alarm;

import org.bitrepository.bitrepositorymessages.Alarm;

/**
 * Simple AlarmHandler for testing what the latest alarm was. 
 */
public class TestAlarmHandler implements AlarmHandler {
    /** The String representation of the latest object received on notify.*/
    private String latestAlarmMessage;
    
    /** The latest alarm.*/
    private Alarm latestAlarm;
    
    /**
     * Constructor.
     */
    public TestAlarmHandler() {}
    
    @Override
    public void handleAlarm(Alarm msg) {
        latestAlarm = msg;
        latestAlarmMessage = msg.toString();
    }

    @Override
    public void handleOther(Object msg) {
        latestAlarmMessage = msg.toString();
    }
    
    /**
     * Method for retrieving the latest Alarm message received.
     * @return The latest Alarm received.
     */
    public Alarm getLatestAlarm() {
        return latestAlarm;
    }

    /**
     * Method for retrieving the 'toString()' of the latest received object to notify.
     * @return The String representation of the latest object received.
     */
    public String getLatestAlarmMessage() {
        return latestAlarmMessage;
    }
}
