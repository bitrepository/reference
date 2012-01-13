package org.bitrepository.protocol.messagebus;

import org.bitrepository.bitrepositorymessages.Message;


public interface SimpleMessageListener {

    /**
     * Action to perform upon receiving a general message.
     *
     * @param message The message received.
     */
    void onMessage(Message message);
}
