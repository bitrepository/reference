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
    public void notify(Alarm msg) {
        latestAlarm = msg;
        latestAlarmMessage = msg.toString();
    }

    @Override
    public void notify(Object msg) {
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
