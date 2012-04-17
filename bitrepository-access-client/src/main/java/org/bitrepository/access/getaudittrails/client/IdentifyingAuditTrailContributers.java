/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.conversation.ConversationContext;
import org.bitrepository.protocol.conversation.GeneralConversationState;
import org.bitrepository.protocol.conversation.IdentifyingState;
import org.bitrepository.protocol.pillarselector.ComponentSelector;
import org.bitrepository.protocol.pillarselector.MultipleComponentSelector;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class IdentifyingAuditTrailContributers extends IdentifyingState {
    private final AuditTrailConversationContext context;
    private final MultipleComponentSelector selector;

    public IdentifyingAuditTrailContributers (AuditTrailConversationContext context) {
        super();
        this.context = context;
        List<String> expectedContributers = new ArrayList<String>(context.getComponentQueries().length);
        for (AuditTrailQuery entry:context.getComponentQueries()) {
            expectedContributers.add(entry.getComponentID());
        }
        selector = new AuditTrailContributorSelector(expectedContributers);
    }

    @Override
    protected void sendRequest() {
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest = new IdentifyContributorsForGetAuditTrailsRequest();
        identifyRequest.setCorrelationID(context.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setCollectionID(context.getSettings().getCollectionID());
        identifyRequest.setReplyTo(context.getSettings().getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setTo(context.getSettings().getCollectionDestination());
        identifyRequest.setAuditTrailInformation(context.getAuditTrailInformation());
        identifyRequest.setFrom(context.getClientID());

        context.getMessageSender().sendMessage(identifyRequest);
        context.getMonitor().identifyPillarsRequestSent("Identifying contributers for audit trails");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identify contributers for Audit Trails";
    }

    @Override
    public ComponentSelector getSelector() {
        return selector;
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingAuditTrails(context, selector.getSelectedComponents());
    }
}