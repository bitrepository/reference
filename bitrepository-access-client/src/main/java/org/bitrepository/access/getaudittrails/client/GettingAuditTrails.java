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
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GettingAuditTrails extends PerformingOperationState {
    private final AuditTrailConversationContext context;
    private Map<String,String> activeContributors;
    private ContributorResponseStatus responseStatus;

    /*
     * @param context The conversation context.
     * @param contributors The list of components the audit trails should be collected from.
     */
    public GettingAuditTrails(AuditTrailConversationContext context, List<SelectedComponentInfo> contributors) {
        super();
        this.context = context;
        this.activeContributors = new HashMap<String,String>();
        for (SelectedComponentInfo contributorInfo : contributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
        this.responseStatus = new ContributorResponseStatus(activeContributors.keySet());
    }

    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending request for audit trails", activeContributors.keySet().toString());
        for(AuditTrailQuery query : context.getComponentQueries()) {
            if (activeContributors.containsKey(query.getComponentID())) {
                GetAuditTrailsRequest msg = new GetAuditTrailsRequest();
                initializeMessage(msg);
                msg.setFileID(context.getFileID());
                msg.setResultAddress(context.getUrlForResult());

                msg.setContributor(query.getComponentID());
                msg.setTo(activeContributors.get(query.getComponentID()));
                if (query.getMinSequenceNumber() != null) {
                    msg.setMinSequenceNumber(BigInteger.valueOf(query.getMinSequenceNumber().intValue()));
                }
                if (query.getMaxSequenceNumber() != null) {
                    msg.setMaxSequenceNumber(BigInteger.valueOf(query.getMaxSequenceNumber().intValue()));
                }
                context.getMessageSender().sendMessage(msg);
            }
        }
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof GetAuditTrailsFinalResponse) {
            GetAuditTrailsFinalResponse response = (GetAuditTrailsFinalResponse)msg;
            getContext().getMonitor().contributorComplete(
                    new AuditTrailResult("Audit trails received from " + response.getFrom(),
                            response.getFrom(), response.getResultingAuditTrails()));
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
    protected ContributorResponseStatus getResponseStatus() {
        return responseStatus;
    }
}