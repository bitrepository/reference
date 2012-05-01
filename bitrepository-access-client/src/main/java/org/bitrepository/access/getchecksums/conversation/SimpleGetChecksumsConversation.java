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

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.conversation.AbstractConversation;
import org.bitrepository.client.conversation.ConversationEventMonitor;
import org.bitrepository.client.conversation.ConversationState;
import org.bitrepository.client.conversation.FinishedState;

/**
 * A conversation for GetChecksums.
 *
 * Logic for behaving sanely in GetChecksums conversations.
 */
public class SimpleGetChecksumsConversation extends AbstractConversation {
    private final GetChecksumsConversationContext context;
    
    /** The sender to use for dispatching messages */
    //final MessageSender messageSender; 
    /** The configuration specific to the BitRepositoryCollection related to this conversion. */
    //final Settings settings;

    /** The url which the pillar should upload the file to. */
    //final URL uploadUrl;
    /** The ID of the file which should be uploaded to the supplied url */
    //final FileIDs fileIDs;
    /** Selects a pillar based on responses. */
//    final PillarSelectorForGetChecksums selector;
    /** The conversation state (State pattern) */
//    GetChecksumsState conversationState;
    /** The specifications for which checksums to retrieve.*/
//    final ChecksumSpecTYPE checksumSpecifications;
    /** The text audittrail information for requesting the operation.*/
//    final String auditTrailInformation;
    /** The client ID */
//    final String clientID;
    
//    Map<String, ResultingChecksums> mapOfResults = null;

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
    public SimpleGetChecksumsConversation(GetChecksumsConversationContext context) {
        super(context.getMessageSender(), context.getConversationID(), null, null);
        this.context = context;
        context.setState(new IdentifyPillarsForGetChecksums(context));
    }
    
    
    /*public SimpleGetChecksumsConversation(MessageSender messageSender, Settings settings, URL url,
            FileIDs fileIds, ChecksumSpecTYPE checksumsSpecs, Collection<String> pillars, String clientID, 
            EventHandler eventHandler, FlowController flowController, String auditTrailInformation) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.uploadUrl = url;
        this.fileIDs = fileIds;
        this.selector = new PillarSelectorForGetChecksums(pillars);
        this.checksumSpecifications = checksumsSpecs;
        this.conversationState = new IdentifyPillarsForGetChecksums(this);
        this.auditTrailInformation = auditTrailInformation;
        this.clientID = clientID;
    }*/
    
    @Override
    public void onMessage(Message message) {
        context.getState().handleMessage(message);
    }

    @Override
    public ConversationState getConversationState() {
        // Only used to start conversation, which has been oveloaded. This is because the current parent state isn't of
        // type ConversationState in the AuditTrailCLient.
        return null;
    }

    @Override
    public void startConversation() {
        context.getState().start();
    }

    @Override
    public void endConversation() {
        context.setState(new FinishedState(context));
    }

    /**
     * Override to use the new context provided monitor.
     * @return The monitor for distributing update information
     */
    public ConversationEventMonitor getMonitor() {
        return context.getMonitor();
    }

    @Override
    public boolean hasEnded() {
        return context.getState() instanceof FinishedState;
    }
    

    /*@Override
    public boolean hasEnded() {
        return conversationState.hasEnded();
    }
    */
    /*public Map<String,ResultingChecksums> getResult() {
        return mapOfResults;
    }*/
    
    /**
     * Method for reporting the results of a conversation.
     * @param results The results.
     */
    /*void setResults(Map<String, ResultingChecksums> results) {
        this.mapOfResults = results;
    }*/
/*
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
    }*/
}
