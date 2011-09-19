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
package org.bitrepository.access.getchecksums.conversation;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bitrepository.access.getchecksums.selector.PillarSelectorForGetChecksums;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.collection.settings.standardsettings.Settings;
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
 * A conversation for GetChecksums.
 *
 * Logic for behaving sanely in GetChecksums conversations.
 */
public class SimpleGetChecksumsConversation extends AbstractConversation<Map<String,ResultingChecksums>> {

    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The sender to use for dispatching messages */
    final MessageSender messageSender; 
    /** The configuration specific to the BitRepositoryCollection related to this conversion. */
    final Settings settings;

    /** The url which the pillar should upload the file to. */
    final URL uploadUrl;
    /** The ID of the file which should be uploaded to the supplied url */
    final FileIDs fileIDs;
    /** Selects a pillar based on responses. */
    final PillarSelectorForGetChecksums selector;
    /** The event handler to send notifications of the get file progress */
    final EventHandler eventHandler;
    /** The conversation state (State pattern) */
    GetChecksumsState conversationState;
    /** The exception caught if the operation failed.*/
    OperationFailedException operationFailedException;
    /** The specifications for which checksums to retrieve.*/
    final ChecksumSpecs checksumSpecifications;

    /**
     * Constructor.
     * @param messageSender The instance for sending the messages.
     * @param settings The settings for the GetChecksumsClient.
     * @param url The URL where to upload the results.
     * @param fileIds The IDs for the files to retrieve.
     * @param checksumsSpecs The specifications for the checksums to retrieve.
     * @param pillars The pillars to retrieve the checksums from.
     * @param eventHandler The handler of events.
     */
    public SimpleGetChecksumsConversation(MessageSender messageSender, Settings settings, URL url,
            FileIDs fileIds, ChecksumSpecs checksumsSpecs, Collection<String> pillars, EventHandler eventHandler) {
        super(messageSender, UUID.randomUUID().toString());
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.uploadUrl = url;
        this.fileIDs = fileIds;
        this.selector = new PillarSelectorForGetChecksums(pillars);
        this.eventHandler = eventHandler;
        this.checksumSpecifications = checksumsSpecs;
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof GetChecksumsFinished;
    }

    @Override
    public Map<String,ResultingChecksums> getResult() {
        if(hasEnded()) {
            return ((GetChecksumsFinished) conversationState).getResults();
        } else {
            return null;
        }
    }

    @Override
    public void startConversation() throws OperationFailedException {
        log.debug("Starting GetChecksum conversation: '" + getConversationID() + "'");
        IdentifyPillarsForGetChecksums initialConversationState = new IdentifyPillarsForGetChecksums(this);
        conversationState = initialConversationState;
        initialConversationState.start();                   
        if (eventHandler == null) {
            waitFor(TimeMeasureComparator.getTimeMeasureInLong(settings.getProtocol().getConversationTimeout()));
        }
        if (operationFailedException != null) {
            throw operationFailedException;
        }
    }

    @Override
    public synchronized void onMessage(GetChecksumsFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(GetChecksumsProgressResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
        conversationState.onMessage(message);
    }

    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    private synchronized void unBlock() {    
        notifyAll();
    }

    /**
     * Used in case of a blocking call to the conversation, e.g. no eventHandler supplied,
     *   for throwing an exception. Will set the conversation exception and unblock the call.
     * @param exception The exception to throw on unblock. 
     */
    public void throwException(OperationFailedException exception) {
        operationFailedException = exception;
        unBlock();                           
    }

    @Override
    public synchronized void failConversation(String message) {
        log.warn("Conversation failed: '" + getConversationID() + "'.");
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
