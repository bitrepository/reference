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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.OperationType;

/**
 * Defines how messages received or sent are logged. Meant to be used in the shared message bus
 * instances.
 *
 * Custom logs can be defined for individual classes, which can be added through the
 * addMessageLogger method with a specific logger class for the operation.
 */
public class MessageLoggerProvider implements MessageLogger {
    private final static MessageLoggerProvider instance = new MessageLoggerProvider();

    private final Map<String, MessageLogger> messageToLoggerMap = new HashMap<>();
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
