package org.bitrepository.protocol;

import java.util.LinkedList;
import java.util.List;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Takes care of the receiver bookkeeping in connect with test setup and testdown.
 */
public class MessageReceiverManager {
    private final List<MessageReceiver> messageReceivers = new LinkedList();
    private final MessageBus messageBus;

    public MessageReceiverManager (MessageBus messageBus) {
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