/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
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
package org.bitrepository.alarm.handler;

import org.bitrepository.alarm.AlarmHandler;
import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple AlarmHandler, which just logs the alarms received.
 */
public class AlarmLoggingHandler implements AlarmHandler {
    
    /** The logger to log the Alarms.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** Constructor. Nothing to initialize. */
    public AlarmLoggingHandler() { }
    
    @Override
    public void handleAlarm(Alarm msg) {
        AlarmDescription description = msg.getAlarmDescription();
        log.info("Received alarm with code '" + description.getAlarmCode() + "' and text '" 
                + description.getAlarmText() + "':\n{}", msg.toString());
    }
    
    @Override
    public void handleOther(Object msg) {
        log.warn("Received unexpected object: \n{}", msg.toString());
    }
}
