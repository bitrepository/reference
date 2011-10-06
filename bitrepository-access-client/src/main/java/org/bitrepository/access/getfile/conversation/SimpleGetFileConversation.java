/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfile.conversation;

import java.net.URL;
import java.util.UUID;

import org.bitrepository.access.getfile.selectors.PillarSelectorForGetFile;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.protocol.time.TimeMeasureComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A conversation for GetFile.
 *
 * Logic for behaving sanely in GetFile conversations.
 */
public class SimpleGetFileConversation extends AbstractConversation<URL> {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The sender to use for dispatching messages */
    final MessageSender messageSender; 
    /** The configuration specific to the SLA related to this conversion. */
    final Settings settings;

    /** The url which the pillar should upload the file to. */
    final URL uploadUrl;
    /** The ID of the file which should be uploaded to the supplied url */
    final String fileID;
    /** Selects a pillar based on responses. */
    final PillarSelectorForGetFile selector;
    /** The event handler to send notifications of the get file progress */
    final EventHandler eventHandler;
    /** The conversation state (State pattern) */
    GetFileState conversationState;
    OperationFailedException operationFailedException;

    /**
     * Initialises the file directory, and the message bus used for sending messages.
     * The fileDir is retrieved from the configuration.
     *
     * @param messageBus The message bus used for sending messages.
     * @param expectedNumberOfPillars The number of pillars to wait for replies from, when identifying pillars.
     * @param getFileDefaultTimeout The timeout when getting a file. If the conversation identifies pillars, this value
     * is replaced by twice the time the pillar estimated.
     * @param fileDir The directory to store retrieved files in.
     */
    public SimpleGetFileConversation(
            MessageSender messageSender, 
            Settings settings,
            PillarSelectorForGetFile selector,
            String fileID,
            URL uploadUrl,
            EventHandler eventHandler) {
        super(messageSender, UUID.randomUUID().toString());

        this.messageSender = messageSender;
        this.settings = settings;
        this.selector = selector;
        this.uploadUrl = uploadUrl;
        this.fileID = fileID;
        this.eventHandler = eventHandler;
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof GetFileFinished;
    }

    @Override
    public URL getResult() {
        return uploadUrl;
    }

    /** Will start the conversation and either:<ol>
     * <li> If no event handler has been defined the method will block until the conversation has finished.</li>
     * <li> If a event handler has been defined the method will return after the conversation is started.</li>
     * </ol>
     */
    @Override
    public void startConversation() throws OperationFailedException {
        IdentifyingPillarsForGetFile initialConversationState = new IdentifyingPillarsForGetFile(this);
        conversationState = initialConversationState;
        initialConversationState.start();		
        if (eventHandler == null) {
            waitFor(settings.getReferenceSettings().getClientSettings().getConversationTimeout().longValue());
        }
        if (operationFailedException != null) {
            throw operationFailedException;
        }
    }

    @Override
    public synchronized void onMessage(GetFileFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(GetFileProgressResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForGetFileResponse message) {
        conversationState.onMessage(message);
    }

    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    private synchronized void unBlock() {	
        notifyAll();
    }

    /**
     * Used in case of a blocking call to the conversation, eg. no eventHandler supplied,
     *   for throwing an exception. Will set the conversation exception and unblock the call.
     * @param exception The exception to throw on unblock. 
     */
    public void throwException(OperationFailedException exception) {
        operationFailedException = exception;
        unBlock();		
    }

    @Override
    public synchronized void failConversation(String message) {
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(
                    OperationEventType.Failed, message));
        } else {
            throwException(new OperationFailedException(message));
        }
        conversationState.endConversation();
        unBlock();
    }
}
