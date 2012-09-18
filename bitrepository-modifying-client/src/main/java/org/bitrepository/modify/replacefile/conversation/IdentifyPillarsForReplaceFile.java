/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: IdentifyPillarsForDeleteFile.java 639 2011-12-15 10:24:45Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/IdentifyPillarsForDeleteFile.java $
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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * The first state of the ReplaceFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForReplaceFile extends IdentifyingState {
    private final ReplaceFileConversationContext context;

    /**
     * @param context The conversation's context.
     */
    public IdentifyPillarsForReplaceFile(ReplaceFileConversationContext context) {
        super(context.getContributors());
        this.context = context;
    }


    /**
     * Extends the default behaviour with a idempotent aspects. This assumes that the replace on a pillar is successful if
     * if new file is the one already present on the pillar.
     *
     * Any other none-positive response is handled as a fatal problem.
     */
    @Override
    protected void handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
        //ToDo implement idem potent behaviour
        IdentifyPillarsForReplaceFileResponse response = (IdentifyPillarsForReplaceFileResponse) msg;
        getContext().getMonitor().contributorFailed(
                msg.getResponseInfo().getResponseText(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
        throw new UnableToFinishException("Can not continue with replace operation, as " + msg.getFrom() +
                " is unable to perform the deletion.");
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new ReplacingFile(context, getSelector().getSelectedComponents());
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForReplaceFileRequest msg = new IdentifyPillarsForReplaceFileRequest();
        initializeMessage(msg);
        msg.setFileID(context.getFileID());
        msg.setTo(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying pillars for replace file");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "IdentifyPillarsForReplaceFile";
    }

    @Override
    protected boolean canFinish() {
        return (getOutstandingComponents().isEmpty());
    }

    @Override
    protected void checkForChecksumPillar(MessageResponse msg) {
        IdentifyPillarsForReplaceFileResponse response = (IdentifyPillarsForReplaceFileResponse) msg;
        if (response.getPillarChecksumSpec() != null) {
            context.addChecksumPillar(response.getPillarID());
        }
    }
}
