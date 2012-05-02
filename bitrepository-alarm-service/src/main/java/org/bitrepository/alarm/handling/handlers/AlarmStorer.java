package org.bitrepository.alarm.handling.handlers;

import org.bitrepository.alarm.handling.AlarmHandler;
import org.bitrepository.alarm.store.AlarmStore;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmStorer implements AlarmHandler {
    /** The logger.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());
    /** The store for storing the alarms.*/
    private final AlarmStore store;
    
    /**
     * Constructor.
     * @param store The alarmstore to store the alarms.
     */
    public AlarmStorer(AlarmStore store) {
        this.store = store;
    }
    
    @Override
    public void handleAlarm(AlarmMessage message) {
        log.debug("Adding alarm from message '{}'", message);
        store.addAlarm(message.getAlarm());
    }
    
    @Override
    public void handleOther(Object message) {
        log.debug("Recieved other message, which cannot be stored as an alarm: {}", message);
    }
    
    @Override
    public void close() { 
        log.debug("Closing the alarmhandler '" + this.getClass().getCanonicalName() + "'");
    }
}
