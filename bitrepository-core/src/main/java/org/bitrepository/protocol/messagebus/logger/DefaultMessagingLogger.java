package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the functionality for generic message logging. Open for extension for by custom loggers for specific
 * messages.
 */
public class DefaultMessagingLogger implements MessageLogger {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void logMessageSent(Message message) {
        StringBuilder messageSB = new StringBuilder("Sent ");
        if (shouldLogFullMessage(message)) {
            logFullMessage(appendFullRepresentation(messageSB, message).toString());
        } else {
            appendMessageIDString(messageSB, message);
            messageSB.append(" to " + message.getTo() + ": ");
            appendShortRepresentation(messageSB, message);
            logShortMessage(messageSB.toString());
        }
    }

    @Override
    public void logMessageReceived(Message message) {
        StringBuilder messageSB = new StringBuilder("Received ");
        if (shouldLogFullMessage(message)) {
            logFullMessage(appendFullRepresentation(messageSB, message).toString());
        } else {
            appendMessageIDString(messageSB, message);
            messageSB.append(" from " + message.getFrom() + ": ");
            appendShortRepresentation(messageSB, message);
            logShortMessage(messageSB.toString());
        }
    }

    /**
     *
     Indicated whether the full message should be logged. Can be overridden in custom loggers.
     */
    protected boolean shouldLogFullMessage(Message message) {
        return log.isTraceEnabled();
    }

    /**
     * Log the full message at trace level. May be overridden to log at a different level for concrete messages.
     * @param message The message string to log.
     * @return Whether the
     */
    protected void logFullMessage(String message) {
        log.trace(message);
    }
    private StringBuilder appendFullRepresentation(StringBuilder messageSB, Message message) {
        messageSB.append(message.toString());
        return messageSB;
    }

    /**
     * Log the short version of the message at info level.
     * May be overridden to log at a different level for concrete messages.
     * @param message The message string to log.
     */
    protected void logShortMessage(String message) {
        log.info(message);
    }
    private StringBuilder appendShortRepresentation(StringBuilder messageSB, Message message) {
        appendCustomInfo(messageSB, message);
        if (message instanceof MessageResponse) {
            appendResponseInfo(messageSB, (MessageResponse) message);
        }
        return messageSB;
    }

    private StringBuilder appendMessageIDString(StringBuilder messageSB, Message message) {
        messageSB.append(message.getClass().getSimpleName());
        messageSB.append("(" + getShortConversationID(message.getCorrelationID()) + ")");
        return messageSB;
    }

    private StringBuilder appendResponseInfo(StringBuilder messageSB, MessageResponse response) {
        messageSB.append(
                response.getResponseInfo().getResponseCode() + "(" +
                        response.getResponseInfo().getResponseText() + ")");
        return messageSB;
    }

    /**
     * Returns a shorted conversationID. Only the first part up til the first '-' is used
     * (but at least 4 long).
     */
    private String getShortConversationID(String fullConversationID) {
        if (fullConversationID.length() > 4) {
            if (fullConversationID.contains("-")) {
                return fullConversationID.substring(0, fullConversationID.indexOf("-", 4));
            } else {
                return fullConversationID.substring(0, 5);
            }
        } else {
            return fullConversationID;
        }
    }

    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        return messageSB;
    }
}
