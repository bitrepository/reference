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
package org.bitrepository.protocol;

import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for conversations. This super class will handle sending all messages with the correct
 * conversation id, and simply log messages received. Overriding implementations should override the behaviour for
 * receiving specific messages.
 *
 * @param <T> The result of this conversation.
 */
public abstract class AbstractMessagebusBackedConversation<T> implements Conversation<T> {
    /** The message bus used for sending messages. */
    private final MessageBus messagebus;
    /** The conversation ID. */
    private final String conversationID;
    /** The conversation mediator that handles this conversation. */
    protected ConversationMediator mediator;
    /** The logger for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Initialise a conversation on the given messagebus.
     *
     * @param messagebus The message bus used for exchanging messages.
     * @param conversationID The conversation ID for this conversation.
     */
    public AbstractMessagebusBackedConversation(MessageBus messagebus, String conversationID) {
        this.messagebus = messagebus;
        this.conversationID = conversationID;
    }


    @Override
    public void setMediator(ConversationMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public String getConversationID() {
        return conversationID;
    }

    @Override
    public T waitFor() {
        synchronized (this) {
            while (!isEnded()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        return getResult();
    }

    @Override
    public T waitFor(long timeout) throws ConversationTimedOutException {
        long time = System.currentTimeMillis();
        synchronized (this) {
            while (!isEnded() && time + timeout < System.currentTimeMillis()) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        if (!isEnded()) {
            throw new ConversationTimedOutException("Conversation timed out");
        }
        return getResult();
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsComplete content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileComplete content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsComplete content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetChecksumsResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetChecksumsRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileIDsRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileIDsResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForPutFileResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForPutFileRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileComplete content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileRequest content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileResponse content) {
        content.setCorrelationID(getConversationID());
        messagebus.sendMessage(destinationId, content);
    }

    @Override
    public void onMessage(GetChecksumsComplete message) {
       log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetChecksumsResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileComplete message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileIDsComplete message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileIDsResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(PutFileComplete message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(PutFileRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(PutFileResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }
}
