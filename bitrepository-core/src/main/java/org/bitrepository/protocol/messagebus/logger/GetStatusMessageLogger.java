package org.bitrepository.protocol.messagebus.logger;

/*
 * #%L
 * BitRepository Core
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

/**
 * Custom logger for the status messages. Will only log messages on trace level.
 */
public class GetStatusMessageLogger extends DefaultMessagingLogger {
    @Override
    protected boolean shouldLogFullMessage(Message message) {
        return false;
    }

    /**
     * To avoid spawning the log with the GetStatus message 'heartbeats', the message are only
     * logged at trace level.
     *
     * @param message The message string to log.
     */
    @Override
    protected void logShortMessage(String message) {
        log.trace(message);
    }
}
