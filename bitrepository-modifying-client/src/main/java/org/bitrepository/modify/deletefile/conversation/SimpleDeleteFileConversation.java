/*
 * #%L
 * Bitrepository Modifying Client
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
package org.bitrepository.modify.deletefile.conversation;

import java.util.Collection;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.deletefile.selector.PillarSelectorForDeleteFile;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * A conversation for the DeleteFile operation.
 * Logic for behaving sanely in DeleteFile conversations.
 */
public class SimpleDeleteFileConversation extends AbstractConversation {
    /** The sender to use for dispatching messages */
    final MessageSender messageSender;
    /** The configuration specific to the SLA related to this conversion. */
    final Settings settings;
    
    /** The ID of the file which should be deleted. */
    final String fileID;
    /** The ID of the pillar to delete the file from. */
    final Collection<String> pillarId;
    /** The checksums specification for the pillar.*/
    final ChecksumDataForFileTYPE checksumForFileToDelete;
    /** The checksum specification requested from the pillar.*/
    final ChecksumSpecTYPE checksumSpecRequested;
    /** The state of the PutFile transaction.*/
    DeleteFileState conversationState;
    /** The audit trail information for the conversation.*/
    final String auditTrailInformation;
    /** The selector for finding the pillar.*/
    final PillarSelectorForDeleteFile pillarSelector;
    /** The client ID */
    final String clientID;

    /**
     * Constructor.
     * Initializes all the variables for the conversation.
     * 
     * @param messageSender The instance to send the messages with.
     * @param settings The settings of the client.
     * @param fileId The id of the file.
     * @param pillarIds The id of the pillars perform the operation upon.
     * @param checksumOfFileToDelete The checksum of the file to delete.
     * @param checksumSpecForPillar The checksum specifications for the file to delete.
     * @param eventHandler The event handler.
     * @param flowController The flow controller for the conversation.
     * @param auditTrailInformation The audit trail information for the conversation.
     */
    public SimpleDeleteFileConversation(MessageSender messageSender,
            Settings settings,
            String fileId,
            Collection<String> pillarIds,
            ChecksumDataForFileTYPE checksumSpecForPillar,
            ChecksumSpecTYPE checksumSpecRequested,
            EventHandler eventHandler,
            FlowController flowController,
            String auditTrailInformation,
            String clientID) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.fileID = fileId;
        this.pillarId = pillarIds;
        this.checksumForFileToDelete = checksumSpecForPillar;
        this.checksumSpecRequested = checksumSpecRequested;
        this.auditTrailInformation = auditTrailInformation;
        this.clientID = clientID;
        conversationState = new IdentifyPillarsForDeleteFile(this);
        pillarSelector = new PillarSelectorForDeleteFile(pillarIds);
    }
    
    @Override
    public synchronized void onMessage(IdentifyPillarsForDeleteFileResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public synchronized void onMessage(DeleteFileProgressResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public synchronized void onMessage(DeleteFileFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof DeleteFileFinished;
    }
    
    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }
    
    @Override
    public void endConversation() {
        conversationState.endConversation();
    }
}
