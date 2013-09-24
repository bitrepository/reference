/*
 * #%L
 * Bitrepository Access
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
package access.getchecksums.conversation;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.common.utils.FileIDsUtils;

/**
 * Models the behavior of a GetChecksums conversation during the identification phase. That is, it begins with the 
 * sending of <code>IdentifyPillarsForGetChecksumsRequest</code> messages and finishes with on the reception of the 
 * <code>IdentifyPillarsForGetChecksumsResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetChecksumsConversation in the same package, therefore the visibility is package 
 * protected.
 * This is the initial state for the whole GetChecksums communication.
 */
public class IdentifyPillarsForGetChecksums extends IdentifyingState {
    private final GetChecksumsConversationContext context;
    
    /**
     * @param context The context shared between the getChecksum operation states.
     */
    public IdentifyPillarsForGetChecksums(GetChecksumsConversationContext context) {
        super(context.getContributors());
        this.context = context;
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingChecksums(context, getSelector().getSelectedComponents());
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForGetChecksumsRequest msg = new IdentifyPillarsForGetChecksumsRequest();
        initializeMessage(msg);
        msg.setDestination(context.getSettings().getCollectionDestination());
        msg.setFileIDs(FileIDsUtils.createFileIDs(context.getFileID()));
        msg.setChecksumRequestForExistingFile(context.getChecksumSpec());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying pillars for GetChecksums");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "IdentifyPillarsForGetChecksums";
    }

}
