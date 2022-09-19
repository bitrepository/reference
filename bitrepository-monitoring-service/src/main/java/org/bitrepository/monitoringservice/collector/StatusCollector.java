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
package org.bitrepository.monitoringservice.collector;

import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.XmlUtils;
import org.bitrepository.monitoringservice.alarm.MonitorAlerter;
import org.bitrepository.monitoringservice.status.StatusStore;

import javax.xml.datatype.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The collector of status messages.
 */
public class StatusCollector {
    private final GetStatusClient getStatusClient;
    private final StatusStore statusStore;
    private final EventHandler eventHandler;
    private static final boolean TIMER_IS_DAEMON = true;
    private static final String NAME_OF_TIMER = "GetStatus collection timer";
    private static final Timer timer = new Timer(NAME_OF_TIMER, TIMER_IS_DAEMON);
    /** Collection interval in milliseconds */
    private final long collectionInterval;

    /**
     * @param getStatusClient The status client.
     * @param settings        The settings.
     * @param statusStore     The storage for the status results.
     * @param alerter         The alerter.
     */
    public StatusCollector(GetStatusClient getStatusClient, Settings settings, StatusStore statusStore, MonitorAlerter alerter) {
        this.getStatusClient = getStatusClient;
        eventHandler = new GetStatusEventHandler(statusStore, alerter);
        this.statusStore = statusStore;
        Duration collectionIntervalXmlDuration =
                settings.getReferenceSettings().getMonitoringServiceSettings().getCollectionInterval();
        collectionInterval = XmlUtils.xmlDurationToMilliseconds(collectionIntervalXmlDuration);
    }

    /**
     * Start the collection of statuses
     */
    public void start() {
        timer.schedule(new StatusCollectorTimerTask(), 0, collectionInterval);
    }

    /**
     * Stop the collection of statuses
     */
    public void stop() {
        timer.cancel();
    }

    /**
     * The timer task for collecting the status.
     * Tells the store that a new status request has been issued, and then starts the conversation for retrieving the
     * status from all the contributors.
     */
    private class StatusCollectorTimerTask extends TimerTask {
        @Override
        public void run() {
            statusStore.updateReplyCounts();
            getStatusClient.getStatus(eventHandler);
        }
    }
}
