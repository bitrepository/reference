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
import java.util.Collection;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

public class GettingAuditTrails extends PerformingOperationState {
    private final AuditTrailConversationContext context;

    /*
     * @param context The conversation context.
     * @param contributors The list of components the audit trails should be collected from.
     */
    public GettingAuditTrails(AuditTrailConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
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
                msg.setDestination(activeContributors.get(query.getComponentID()));
                if (query.getMinSequenceNumber() != null) {
                    msg.setMinSequenceNumber(BigInteger.valueOf(query.getMinSequenceNumber().intValue()));
                }
                if (query.getMaxSequenceNumber() != null) {
                    msg.setMaxSequenceNumber(BigInteger.valueOf(query.getMaxSequenceNumber().intValue()));
                }
                if (query.getMaxNumberOfResults() != null) {
                    msg.setMaxNumberOfResults(BigInteger.valueOf(query.getMaxNumberOfResults().intValue()));
                }
                context.getMessageSender().sendMessage(msg);
            }
        }
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        GetAuditTrailsFinalResponse response = (GetAuditTrailsFinalResponse)msg;
        boolean isPartialResult = response.isPartialResult() == null ? false : response.isPartialResult();
        getContext().getMonitor().contributorComplete(
                new AuditTrailResult(response.getFrom(), response.getResultingAuditTrails(), isPartialResult));
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "GetAuditTrails";
    }
}