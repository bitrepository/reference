/*
 * #%L
 * bitrepository-access-client
 * *
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
package org.bitrepository.modify.putfile.conversation;

import java.math.BigInteger;
import java.net.URL;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * A conversation for PutFile.
 * Logic for behaving sanely in PutFile conversations.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimplePutFileConversation extends AbstractConversation {
    /** The sender to use for dispatching messages */
    final MessageSender messageSender;
    /** The configuration specific to the SLA related to this conversion. */
    final Settings settings;
    
    /** The URL which the pillar should download the file from. */
    final URL downloadUrl;
    /** The ID of the file which should be downloaded from the supplied URL. */
    final String fileID;
    /** The size of the file to be put.*/
    final BigInteger fileSize;
    /** The state of the PutFile transaction.*/
    PutFileState conversationState;
    /** The checksum of the file, which the pillars should download. Used for validation at pillar-side.*/
    final ChecksumDataForFileTYPE validationChecksums;
    /** The checksums to request from the pillar.*/
    final ChecksumSpecTYPE requestChecksums;
    /** The audit trail information for this conversation.*/
    final String auditTrailInformation;
    /** The client ID */
    final String clientID;
    
    /**
     * Constructor.
     * Initializes all the variables for the conversation.
     *
     * @param messageSender The instance to send the messages with.
     * @param settings The settings of the client.
     * @param urlToDownload The URL where the file to be 'put' is located.
     * @param fileId The id of the file.
     * @param sizeOfFile The size of the file.
     * @param checksumForValidationAtPillar The checksum of the file to upload. Used at pillar-side for validation.
     * Can be null, if no pillar-side validation is wanted.
     * @param checksumRequestsForValidation The checksum requested for client-side validation of correct put.
     * Can be null, if no client-side validation is wanted.
     * @param eventHandler The event handler.
     * @param auditTrailInformation The audit trail information for the conversation.
     */
    public SimplePutFileConversation(MessageSender messageSender,
            Settings settings,
            URL urlToDownload,
            String fileId,
            BigInteger sizeOfFile,
            ChecksumDataForFileTYPE checksumForValidationAtPillar,
            ChecksumSpecTYPE checksumRequestsForValidation,
            EventHandler eventHandler,
            FlowController flowController,
            String auditTrailInformation, 
            String clientID) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.downloadUrl = urlToDownload;
        this.fileID = fileId;
        this.fileSize = sizeOfFile;
        this.validationChecksums = checksumForValidationAtPillar;
        this.requestChecksums = checksumRequestsForValidation;
        conversationState = new IdentifyPillarsForPutFile(this);
        this.auditTrailInformation = auditTrailInformation;
        this.clientID = clientID;
    }
    
    @Override
    public boolean hasEnded() {
        return conversationState instanceof PutFileFinished;
    }
    
    public URL getResult() {
        return downloadUrl;
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

    @Override
    public void endConversation() {
        conversationState.endConversation();
    }

    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }
    
}
