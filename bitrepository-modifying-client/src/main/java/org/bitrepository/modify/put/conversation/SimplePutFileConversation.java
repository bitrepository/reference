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
package org.bitrepository.modify.put.conversation;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.modify.put.PutFileClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.URL;
import java.util.UUID;

/**
 * A conversation for PutFile.
 * Logic for behaving sanely in PutFile conversations.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimplePutFileConversation extends AbstractConversation<URL> {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The sender to use for dispatching messages */
    final MessageSender messageSender;
    /** The configuration specific to the SLA related to this conversion. */
    final PutFileClientSettings settings;

    /** The URL which the pillar should download the file from. */
    final URL downloadUrl;
    /** The ID of the file which should be downloaded from the supplied URL. */
    final String fileID;
    /** The size of the file to be put.*/
    final BigInteger fileSize;
    /** The event handler to send notifications of the get file progress */
    final EventHandler eventHandler;
    /** The response status for the identification of the pillars.*/
    final PillarsResponseStatus identifyResponseStatus;
    /** The response status for the actual put to the pillars.*/
    final PillarsResponseStatus putResponseStatus;
    /** The state of the PutFile transaction.*/
    PutFileState conversationState;
    /** The exception if the operation failed.*/
    OperationFailedException operationFailedException;
    
    // TODO handle the checksum
    /** The checksum of the file, which the pillars should download.*/
//  final ChecksumDataForFileTYPE checksum;

    /**
     * Constructor.
     * Initializes all the variables for the conversation.
     *
     * @param messageSender The instance to send the messages with.
     * @param settings The settings of the client.
     * @param urlToDownload The URL where the file to be 'put' is located.
     * @param fileId The id of the file.
     * @param sizeOfFile The size of the file.
     * @param checksum The checksum of the file to upload.
     * @param eventHandler The event handler.
     */
    public SimplePutFileConversation(MessageSender messageSender,
            PutFileClientSettings settings,
            URL urlToDownload,
            String fileId,
            BigInteger sizeOfFile,
            EventHandler eventHandler) {
        super(messageSender, UUID.randomUUID().toString());

        this.messageSender = messageSender;
        this.settings = settings;
        this.downloadUrl = urlToDownload;
        this.fileID = fileId;
        this.fileSize = sizeOfFile;
        this.eventHandler = eventHandler;
        this.identifyResponseStatus = new PillarsResponseStatus(settings.getPillarIDs());
        this.putResponseStatus = new PillarsResponseStatus(settings.getPillarIDs());
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof PutFileFinished;
    }

    @Override
    public URL getResult() {
        return downloadUrl;
    }

    @Override
    public void startConversion() throws OperationFailedException {
        IdentifyPillarsForPut initialState = new IdentifyPillarsForPut(this);
        conversationState = initialState;
        initialState.start();
        if (eventHandler == null) {
            waitFor(settings.getConversationTimeout());
        }
        if (operationFailedException != null) {
            throw operationFailedException;
        }
    }

    @Override
    public synchronized void onMessage(PutFileFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(PutFileProgressResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForPutFileResponse message) {
        conversationState.onMessage(message);
    }

    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    private synchronized void unBlock() {	
        notifyAll();
    }

    /**
     * Method for throwing an exception.
     * @param exception The exception to throw.
     */
    void throwException(OperationFailedException exception) {
        operationFailedException = exception;
        unBlock();		
    }

    @Override
    public void failConversion(String message) {
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
