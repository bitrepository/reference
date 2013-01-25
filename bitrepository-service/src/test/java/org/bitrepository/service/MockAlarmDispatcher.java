package org.bitrepository.service;

import java.util.LinkedList;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

public class MockAlarmDispatcher extends AlarmDispatcher {

    LinkedList<Alarm> alarms = new LinkedList<Alarm>();
    
    public MockAlarmDispatcher(Settings settings, MessageSender sender) {
        super(settings, sender);
    }
    
    @Override
    protected void sendAlarm(Alarm alarm) {
        alarms.add(alarm);
    }
    
    public Alarm poll() {
        return alarms.poll();
    }
    
}
