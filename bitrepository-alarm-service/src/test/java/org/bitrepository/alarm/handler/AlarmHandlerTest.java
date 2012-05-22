package org.bitrepository.alarm.handler;

import org.bitrepository.alarm.handling.AlarmHandler;
import org.bitrepository.alarm.handling.AlarmMediator;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.IntegrationTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlarmHandlerTest extends IntegrationTest {
    @Test(groups = {"regressiontest"})
    public void alarmMediatorTest() throws Exception {
        addDescription("Test the mediator handling of alarm messages.");
        addStep("Setup mediator and create alarm handler.", "Should be ok.");
        AlarmMediator mediator = new AlarmMediator(messageBus, alarmDestinationID);
        MockAlarmHandler alarmHandler = new MockAlarmHandler();
        mediator.addHandler(alarmHandler);
        Assert.assertEquals(alarmHandler.getCallsForClose(), 0);
        Assert.assertEquals(alarmHandler.getCallsForHandleAlarm(), 0);
        
        addStep("Try giving it a non-alarm message", "Should not call the alarm handler.");
        Message msg = new Message();
        mediator.onMessage(msg);
        Assert.assertEquals(alarmHandler.getCallsForClose(), 0);
        Assert.assertEquals(alarmHandler.getCallsForHandleAlarm(), 0);
        
        addStep("Giv the mediator an AlarmMessage", "Should be sent to the alarm handler");
        AlarmMessage alarmMsg = new AlarmMessage();
        mediator.onMessage(alarmMsg);
        Assert.assertEquals(alarmHandler.getCallsForClose(), 0);
        Assert.assertEquals(alarmHandler.getCallsForHandleAlarm(), 1);
        
        addStep("Close the mediator.", "Should also close the alarm handler.");
        mediator.close();
        Assert.assertEquals(alarmHandler.getCallsForClose(), 1);
        Assert.assertEquals(alarmHandler.getCallsForHandleAlarm(), 1);
    }
    
    protected class MockAlarmHandler implements AlarmHandler {

        private int callsForHandleAlarm = 0;
        @Override
        public void handleAlarm(AlarmMessage msg) {
            callsForHandleAlarm++;
        }
        public int getCallsForHandleAlarm() {
            return callsForHandleAlarm;
        }

        private int callsForClose = 0;
        @Override
        public void close() {
            callsForClose++;
        }
        public int getCallsForClose() {
            return callsForClose;
        }
        
    }
}
