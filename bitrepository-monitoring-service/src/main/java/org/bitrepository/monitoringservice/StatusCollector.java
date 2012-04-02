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
