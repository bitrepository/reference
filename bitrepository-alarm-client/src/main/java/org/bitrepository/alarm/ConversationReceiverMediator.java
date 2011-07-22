/*
 * #%L
 * bitrepository-access-client
 * *
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
package org.bitrepository.alarm;

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
import org.bitrepository.protocol.conversation.Conversation;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;

public class ConversationReceiverMediator<T> implements ConversationMediator {

    /** The messagebus.*/
    private final MessageBus messagebus;
    /** The destination where the messages are received.*/
    private final String listenerDestination;
    /** The handler. Where the received messages should be handled.*/
    private final AlarmHandler handler;

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the given destination.
     * @param messagebus
     * @param listenerDestination
     * @param handler
     */
    public ConversationReceiverMediator(MessageBus messagebus, String listenerDestination, AlarmHandler handler) {
        this.messagebus = messagebus;
        this.listenerDestination = listenerDestination;
        this.handler = handler;

        messagebus.addListener(listenerDestination, this);        
    }

    @Override
    public void addConversation(Conversation msg) {
        // TODO this should not be used.
    }

    @Override
    public void onMessage(Alarm msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetAuditTrailsRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetAuditTrailsProgressResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetAuditTrailsFinalResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetChecksumsFinalResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetChecksumsRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetChecksumsProgressResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetFileFinalResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetFileIDsRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetFileRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetFileProgressResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetStatusRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetStatusProgressResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(GetStatusFinalResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(PutFileFinalResponse msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(PutFileRequest msg) {
        handler.notify(msg);
    }

    @Override
    public void onMessage(PutFileProgressResponse msg) {
        handler.notify(msg);
    }
}
