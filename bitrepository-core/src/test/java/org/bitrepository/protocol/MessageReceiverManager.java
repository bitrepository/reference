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
package org.bitrepository.protocol;

import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.messagebus.MessageBus;

import java.util.LinkedList;
import java.util.List;

/**
 * Takes care of the receiver bookkeeping in connect with test setup and testdown.
 */
public class MessageReceiverManager {
    private final List<MessageReceiver> messageReceivers =
            new LinkedList<>();
    private final MessageBus messageBus;

    public MessageReceiverManager(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public void addReceiver(MessageReceiver receiver) {
        messageReceivers.add(receiver);
    }

    public void startListeners() {
        for (MessageReceiver receiver : messageReceivers) {
            messageBus.addListener(receiver.getDestination(), receiver.getMessageListener());
        }
    }

    public void stopListeners() {
        for (MessageReceiver receiver : messageReceivers) {
            messageBus.removeListener(receiver.getDestination(), receiver.getMessageListener());
        }
    }

    public void checkNoMessagesRemainInReceivers() {
        for (MessageReceiver receiver : messageReceivers) {
            receiver.checkNoMessagesRemain();
        }
    }

    public void clearMessagesInReceivers() {
        for (MessageReceiver receiver : messageReceivers) {
            receiver.clearMessages();
        }
    }
}