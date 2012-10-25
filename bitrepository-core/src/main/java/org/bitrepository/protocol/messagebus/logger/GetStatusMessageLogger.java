package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.Message;

/**
 * Custom logger for the status messages. Will only log mesages on trace level.
 */
public class GetStatusMessageLogger extends DefaultMessagingLogger {
    @Override
    protected boolean shouldLogFullMessage(Message message) {
        return false;
    }

    /**
     * To avoid spawning the log with the GetStatus message 'heartbeats', the message are only
     * logged at trace level.
     * @param message The message string to log.
     */
    @Override
    protected void logShortMessage(String message) {
        log.trace(message);
    }
}
