package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.Message;

public interface MessageLogger {

    /** Creates a message received log for the specific message */
    void logMessageSent(Message message);

    /** Creates a message received log for the specific message */
    void logMessageReceived(Message message);
}
