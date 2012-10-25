package org.bitrepository.protocol.messagebus.logger;

public class GetStatusMessageLogger extends DefaultMessagingLogger {
    protected void logShortMessage(String message) {
        log.trace(message);
    }
}
