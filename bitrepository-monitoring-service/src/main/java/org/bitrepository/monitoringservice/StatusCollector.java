/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice;

import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.eventhandler.EventHandler;

public class StatusCollector {

    /** The getStatusClient */
    private GetStatusClient getStatusClient;
    /** The EventHandler */
    private EventHandler eventHandler;
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** Timer for collecting statuses on a regular basis */
    private Timer timer;
    /** Time between getStatus collections */
    private long collectionInterval = 300000;
    
    public StatusCollector(GetStatusClient getStatusClient, Settings settings, ComponentStatusStore statusStore) {
        this.getStatusClient = getStatusClient;
        eventHandler = new GetStatusEventHandler(statusStore);
        collectionInterval = settings.getReferenceSettings().getGetStatusSettings().getCollectionInterval();
        timer = new Timer("GetStatus collection timer", TIMER_IS_DAEMON);
    }
    
    /** Start the collection of statuses */
    public void start() {
        timer.schedule(new StatusCollectorTimerTask(), 0, collectionInterval);
    }
    
    /** Stop the collection of statuses */
    public void stop() {
        timer.cancel();
    }
    
    private class StatusCollectorTimerTask extends TimerTask {
        @Override
        public void run() {
            getStatusClient.getStatus(eventHandler);
        }
    }
    
}
