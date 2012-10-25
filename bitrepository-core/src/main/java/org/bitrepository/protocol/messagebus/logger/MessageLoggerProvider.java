package org.bitrepository.protocol.messagebus.logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocolversiondefinition.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines how messages received or sent are logged. Meant to be used in the shared message bus
 * instances.
 *
 * Custom logs can be defined for individual classes, which can be added through the
 * addMessageLogger method with a specific logger class for the operation.
 */
public class MessageLoggerProvider implements MessageLogger {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final static MessageLoggerProvider instance = new MessageLoggerProvider();

    private final Map<String, MessageLogger> messageToLoggerMap = new HashMap<String, MessageLogger>();
    private final MessageLogger defaultLogger = new DefaultMessagingLogger();

    private MessageLoggerProvider() {}

    public static MessageLoggerProvider getInstance() {
        return instance;
    }

    public void logMessageSent(Message message) {
        lookupLogger(message).logMessageSent(message);
    }

    public void logMessageReceived(Message message) {
        lookupLogger(message).logMessageReceived(message);
    }

    public void registerLogger(Collection<String> messageSimpleNames, MessageLogger customLogger) {
        for (String messageName:messageSimpleNames) {
            messageToLoggerMap.put(messageName, customLogger);
        }
    }

    public void registerLogger(OperationType operation, MessageLogger customLogger) {
        Collection<String> messageSimpleNames = Arrays.asList(
            "IdentifyContributorsFor" + operation.value() + "Request",
            "IdentifyPillarsFor" + operation.value() + "Request",
            "IdentifyContributorsFor" + operation.value() + "Response",
            "IdentifyPillarsFor" + operation.value() + "Response",
            operation.value() + "Request",
            operation.value() + "ProgressResponse",
            operation.value() + "FinalResponse"
        );
        registerLogger(messageSimpleNames, customLogger);
    }

    private MessageLogger lookupLogger(Message message) {
        String messageName = message.getClass().getSimpleName();
        if (messageToLoggerMap.containsKey(messageName)) {
            return messageToLoggerMap.get(messageName);
        } else {
            return defaultLogger;
        }
    }
}
