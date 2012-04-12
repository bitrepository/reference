package org.bitrepository.pillar;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;

public class MockAlarmDispatcher extends AlarmDispatcher {

    public MockAlarmDispatcher(Settings settings, MessageBus messageBus) {
        super(settings, messageBus);
    }

    private int callsForSendAlarm = 0;
    @Override
    public void sendAlarm(Alarm alarm) {
        callsForSendAlarm++;
    }
    public int getCallsForSendAlarm() {
        return callsForSendAlarm;
    }
    public void resetCallsForSendAlarm() {
        callsForSendAlarm = 0;
    }
}
