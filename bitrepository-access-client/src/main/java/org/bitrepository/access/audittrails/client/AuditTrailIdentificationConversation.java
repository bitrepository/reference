/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.audittrails.client;

import java.math.BigInteger;
import java.util.UUID;

import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.AbstractConversationState;
import org.bitrepository.protocol.conversation.ConversationEventMonitor;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FinishedState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

public class AuditTrailIdentificationConversation extends AbstractConversation {
    /** The text audittrail information for requesting the operation.*/
    private final String auditTrailInformation;
    /** The configuration specific to the BitRepositoryCollection related to this conversion. */
    final Settings settings;
    /** The conversation state (State pattern) */
    private ConversationState state;
    
    public AuditTrailIdentificationConversation (
            Settings settings, MessageSender messageSender, EventHandler eventHandler, String auditTrailInformation) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, new FlowController(settings));
        this.auditTrailInformation = auditTrailInformation;
        this.settings = settings;
        state = new IdentifyingAuditTrailContributersState(getMonitor(), settings.getIdentificationTimeout());
    }
    
    @Override
    public void endConversation() {
        // TODO Auto-generated method stub
    }

    // Move to abstract class when ready to shift conversation state to use general message calls.
    @Override
    public void onMessage(IdentifyContributorsForGetAuditTrailsResponse message) {
        state.onMessage(message);        
    }

    @Override
    public ConversationState getConversationState() {
        return state;
    }

    private class IdentifyingAuditTrailContributersState extends AbstractConversationState {
        public IdentifyingAuditTrailContributersState(ConversationEventMonitor monitor, long stateTimeout) {
            super(monitor, stateTimeout);
        }     
        
        @Override
        protected void startState() {
            IdentifyContributorsForGetAuditTrailsRequest identifyRequest = new IdentifyContributorsForGetAuditTrailsRequest();
            identifyRequest.setCorrelationID(getConversationID());
            identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
            identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
            identifyRequest.setCollectionID(settings.getCollectionID());
            identifyRequest.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
            identifyRequest.setTo(settings.getCollectionDestination());
            identifyRequest.setAuditTrailInformation(auditTrailInformation);

            messageSender.sendMessage(identifyRequest);
            monitor.identifyPillarsRequestSent("Identifying pillars for getting file "); 
        }
        
        @Override
        public void onMessage(IdentifyContributorsForGetAuditTrailsResponse message) {
            state.onMessage(message);        
        }
        

        @Override
        protected void handleStateTimeout() {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected ConversationState endState() {
            return new FinishedState(monitor);
        }
    }

    @Override
    public boolean hasEnded() {
        // TODO Auto-generated method stub
        return state instanceof FinishedState;
    }
}
