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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.conversation.ConversationContext;
import org.bitrepository.protocol.conversation.PerformingOperationState;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

public class GettingAuditTrails extends PerformingOperationState {
    private final AuditTrailConversationContext context;
    private Map<String,String> activeContributers;
    private PillarsResponseStatus responseStatus;

    public GettingAuditTrails(AuditTrailConversationContext context, List<SelectedPillarInfo> contributors) {
        super();
        this.context = context;
        this.activeContributers = new HashMap<String,String>();
        for (SelectedPillarInfo contributorInfo : contributors) {
            activeContributers.put(contributorInfo.getID(), contributorInfo.getDestination());
        }

        this.responseStatus = new PillarsResponseStatus(activeContributers.keySet());
    }

    @Override
    protected void sendRequest() {
        GetAuditTrailsRequest msg = new GetAuditTrailsRequest();
        msg.setCorrelationID(context.getConversationID());
        msg.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        msg.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        msg.setCollectionID(context.getSettings().getCollectionID());
        msg.setReplyTo(context.getSettings().getReferenceSettings().getClientSettings().getReceiverDestination());
        msg.setFileIDs(context.getFileIDs());
        msg.setResultAddress(context.getUrlForResult());
        msg.setAuditTrailInformation(context.getAuditTrailInformation());

        context.getMonitor().requestSent("Sending request for audit trails", activeContributers.keySet().toString());
        for(AuditTrailQuery query : context.getComponentQueries()) {
            if (activeContributers.containsKey(query.getComponentID())) {
                msg.setContributor(query.getComponentID());
                msg.setTo(activeContributers.get(query.getComponentID()));
                if (query.getMinSequenceNumber() != null) {
                    msg.setMinSequenceNumber(BigInteger.valueOf(query.getMinSequenceNumber().intValue()));
                }
                context.getMessageSender().sendMessage(msg);
            }
        }
    }

    @Override
    protected void processMessage(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof GetAuditTrailsProgressResponse) {
           getContext().getMonitor().progress(msg.getResponseInfo().getResponseText());
        } else if (msg instanceof GetAuditTrailsFinalResponse) {
            GetAuditTrailsFinalResponse response = (GetAuditTrailsFinalResponse)msg;
            responseStatus.responseReceived(response.getContributor());
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for Audit Trail response.");
        }
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Fetch audit trails";
    }

    @Override
    protected PillarsResponseStatus getResponseStatus() {
        return responseStatus;
    }
}