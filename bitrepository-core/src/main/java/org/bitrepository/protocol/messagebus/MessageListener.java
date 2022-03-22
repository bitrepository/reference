/*
 * #%L
 * Bitrepository Protocol
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.messagebus;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;


/**
 * Defines a consumer of messages.
 * <p>
 * If an implementation does not support a method, it may throw {@link UnsupportedOperationException}
 */
public interface MessageListener {
    /**
     * Action to perform upon receiving a general message.
     *
     * @param message        The message received.
     * @param messageContext Includes information about the message not already included in the message object.
     */
    void onMessage(Message message, MessageContext messageContext);
}
