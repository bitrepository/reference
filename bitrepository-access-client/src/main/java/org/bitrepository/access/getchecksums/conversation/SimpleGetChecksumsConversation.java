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
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * A conversation for GetChecksums.
 *
 * Logic for behaving sanely in GetChecksums conversations.
 */
public class SimpleGetChecksumsConversation extends AbstractConversation {
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
    /** The conversation state (State pattern) */
    GetChecksumsState conversationState;
    /** The specifications for which checksums to retrieve.*/
    final ChecksumSpecTYPE checksumSpecifications;
    /** The text audittrail information for requesting the operation.*/
    final String auditTrailInformation;
    
    Map<String, ResultingChecksums> mapOfResults = null;

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
            FileIDs fileIds, ChecksumSpecTYPE checksumsSpecs, Collection<String> pillars, EventHandler eventHandler,
            FlowController flowController, String auditTrailInformation) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.uploadUrl = url;
        this.fileIDs = fileIds;
        this.selector = new PillarSelectorForGetChecksums(pillars);
        this.checksumSpecifications = checksumsSpecs;
        this.conversationState = new IdentifyPillarsForGetChecksums(this);
        this.auditTrailInformation = auditTrailInformation;
    }

    @Override
    public boolean hasEnded() {
        return conversationState.hasEnded();
    }
    
    public Map<String,ResultingChecksums> getResult() {
        return mapOfResults;
    }
    
    /**
     * Method for reporting the results of a conversation.
     * @param results The results.
     */
    void setResults(Map<String, ResultingChecksums> results) {
        this.mapOfResults = results;
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

    @Override
    public void endConversation() {
        conversationState.endConversation();
    }

    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }
}
