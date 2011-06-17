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

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
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
    private final MessageSender messageSender;
    /** The conversation ID. */
    private final String conversationID;
    /** The conversation mediator that handles this conversation. */
    protected ConversationMediator mediator;
    /** The logger for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Initialize a conversation on the given messagebus.
     *
     * @param messagebus The message bus used for exchanging messages.
     * @param conversationID The conversation ID for this conversation.
     */
    public AbstractMessagebusBackedConversation(MessageSender messageSender, String conversationID) {
        this.messageSender = messageSender;
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
    public void sendMessage(Alarm content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetChecksumsFinalResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetChecksumsRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetChecksumsProgressResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileFinalResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileIDsFinalResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileIDsRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileIDsProgressResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileProgressResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }
    
    @Override
    public void sendMessage(GetStatusRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetStatusProgressResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(GetStatusFinalResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetChecksumsResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetChecksumsRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileIDsRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileIDsResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForPutFileResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForPutFileRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(PutFileFinalResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(PutFileRequest content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void sendMessage(PutFileProgressResponse content) {
        content.setCorrelationID(getConversationID());
        messageSender.sendMessage(content);
    }

    @Override
    public void onMessage(GetAuditTrailsFinalResponse message) {
       log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetAuditTrailsProgressResponse message) {
       log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }
    
    @Override
    public void onMessage(GetAuditTrailsRequest message) {
       log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetChecksumsFinalResponse message) {
       log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetChecksumsProgressResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileFinalResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetFileProgressResponse message) {
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
    public void onMessage(PutFileFinalResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(PutFileRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(PutFileProgressResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }
    
    @Override
    public void onMessage(Alarm message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetStatusRequest message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetStatusProgressResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }

    @Override
    public void onMessage(GetStatusFinalResponse message) {
        log.debug("Received message " + message.getCorrelationID() + " but did not know how to handle it.");
    }
}
