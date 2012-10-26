package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;

/**
 * Custom logger adding GetFile message specific parameters.
 */
public class AlarmMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof AlarmMessage) {
            AlarmMessage alarmMessage = (AlarmMessage) message;
            messageSB.append(" Alarm=" + alarmMessage.getAlarm());
        }
        return messageSB;
    }
}
