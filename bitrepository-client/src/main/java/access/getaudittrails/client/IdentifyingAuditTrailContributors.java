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
package access.getaudittrails.client;

import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;

public class IdentifyingAuditTrailContributors extends IdentifyingState {
    private final AuditTrailConversationContext context;

    public IdentifyingAuditTrailContributors(AuditTrailConversationContext context) {
        super(context.getContributors());
        this.context = context;
    }

    @Override
    protected void sendRequest() {
        IdentifyContributorsForGetAuditTrailsRequest msg = new IdentifyContributorsForGetAuditTrailsRequest();
        initializeMessage(msg);
        msg.setDestination(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying contributers for audit trails");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "IdentifyContributorsForGetAuditTrails";
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingAuditTrails(context, getSelector().getSelectedComponents());
    }
}