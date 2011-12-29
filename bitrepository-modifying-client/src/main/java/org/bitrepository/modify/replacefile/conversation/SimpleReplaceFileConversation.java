/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id: SimpleDeleteFileConversation.java 633 2011-12-14 09:05:28Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/SimpleDeleteFileConversation.java $
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
package org.bitrepository.modify.replacefile.conversation;

import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.replacefile.pillarselector.PillarSelectorForReplaceFile;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * A conversation for the ReplaceFile operation.
 * Logic for behaving sanely in ReplaceFile conversations.
 */
public class SimpleReplaceFileConversation extends AbstractConversation {
    /** The sender to use for dispatching messages */
    final MessageSender messageSender;
    /** The configuration specific to the SLA related to this conversion. */
    final Settings settings;
    
    /** The ID of the file which should be replaced. */
    final String fileID;
    /** The ID of the pillars to replace the file from. */
    final Collection<String> pillarId;
    /** The checksums specification for the pillar.*/
    final ChecksumDataForFileTYPE checksumForFileToDelete;
    /** The checksum specification requested from the pillar.*/
    final ChecksumSpecTYPE checksumRequestedForFileToDelete;
    /** The url of the new file to replace the old one.*/
    final URL urlOfNewFile;
    /** The size of the new file.*/
    final long sizeOfNewFile;
    /** The checksum for validating the new file at pillar-side.*/
    final ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar;
    /** The request for a checksum of the new file for client-size validation.*/
    final ChecksumSpecTYPE checksumRequestForNewFile;
    /** The state of the PutFile transaction.*/
    ReplaceFileState conversationState;
    /** The audit trail information for the conversation.*/
    final String auditTrailInformation;
    /** The pillar selector*/
    final PillarSelectorForReplaceFile pillarSelector;

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
    public SimpleReplaceFileConversation(MessageSender messageSender,
            Settings settings,
            String fileId,
            Collection<String> pillarIds,
            ChecksumDataForFileTYPE checksumSpecForPillar,
            ChecksumSpecTYPE checksumRequestedForFileToDelete,
            URL url,
            long sizeOfNewFile,
            ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar,
            ChecksumSpecTYPE checksumRequestForNewFile,
            EventHandler eventHandler,
            FlowController flowController,
            String auditTrailInformation) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        ArgumentValidator.checkNotNull(checksumForNewFileValidationAtPillar, "checksumForNewFileValidationAtPillar");
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.fileID = fileId;
        this.pillarId = pillarIds;
        this.checksumForFileToDelete = checksumSpecForPillar;
        this.checksumRequestedForFileToDelete = checksumRequestedForFileToDelete;
        this.urlOfNewFile = url;
        this.sizeOfNewFile = sizeOfNewFile;
        this.checksumForNewFileValidationAtPillar = checksumForNewFileValidationAtPillar;
        this.checksumRequestForNewFile = checksumRequestForNewFile;
        this.auditTrailInformation = auditTrailInformation;
        conversationState = new IdentifyPillarsForReplaceFile(this);
        pillarSelector = new PillarSelectorForReplaceFile(pillarIds);
    }
    
    @Override
    public synchronized void onMessage(IdentifyPillarsForReplaceFileResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public synchronized void onMessage(ReplaceFileProgressResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public synchronized void onMessage(ReplaceFileFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof ReplaceFileFinished;
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
