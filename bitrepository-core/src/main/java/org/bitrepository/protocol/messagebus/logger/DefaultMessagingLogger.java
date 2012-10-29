package org.bitrepository.protocol.messagebus.logger;

/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
        if (message instanceof MessageResponse) {
            appendResponseInfo(messageSB, (MessageResponse) message);
        }
        appendCustomInfo(messageSB, message);
        return messageSB;
    }

    private StringBuilder appendMessageIDString(StringBuilder messageSB, Message message) {
        messageSB.append(message.getClass().getSimpleName());
        messageSB.append("(" + getShortConversationID(message.getCorrelationID()) + ")");
        return messageSB;
    }

    private StringBuilder appendResponseInfo(StringBuilder messageSB, MessageResponse response) {
        messageSB.append(response.getResponseInfo().getResponseCode() + "(" +
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
