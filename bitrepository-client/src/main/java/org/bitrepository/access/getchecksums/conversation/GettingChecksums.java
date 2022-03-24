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
package org.bitrepository.access.getchecksums.conversation;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.CalendarUtils;

import java.math.BigInteger;
import java.util.Collection;

import static org.bitrepository.common.utils.FileIDsUtils.createFileIDs;

/**
 * Models the behavior of a GetChecksums conversation during the operation phase. That is, it begins with the
 * sending of {@link GetChecksumsRequest} messages and finishes with on the retrieval of the
 * {@link GetChecksumsFinalResponse} messages from the responding pillars.
 * <p/>
 * Note that this is only used by the GetChecksumsConversation in the same package, therefore the visibility is package
 * protected.
 */
public class GettingChecksums extends PerformingOperationState {
    private final GetChecksumsConversationContext context;

    /**
     * @param context      The conversation context.
     * @param contributors The list of components the checksums should be collected from.
     */
    public GettingChecksums(GetChecksumsConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof GetChecksumsFinalResponse) {
            GetChecksumsFinalResponse response = (GetChecksumsFinalResponse) msg;
            boolean isPartialResult = response.isPartialResult() != null && response.isPartialResult();
            getContext().getMonitor().contributorComplete(new ChecksumsCompletePillarEvent(
                    response.getFrom(), response.getCollectionID(), response.getResultingChecksums(),
                    response.getChecksumRequestForExistingFile(), isPartialResult
            ));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for GetChecksums response.");
        }
    }

    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending GetChecksumsRequest's", activeContributors.keySet().toString());
        for (ContributorQuery query : context.getContributorQueries()) {
            if (activeContributors.containsKey(query.getComponentID())) {
                GetChecksumsRequest msg = new GetChecksumsRequest();
                initializeMessage(msg);
                msg.setChecksumRequestForExistingFile(context.getChecksumSpec());
                msg.setFileIDs(createFileIDs(context.getFileID()));
                if (context.getUrlForResult() != null) {
                    msg.setResultAddress(context.getUrlForResult().toExternalForm() + "-" + query.getComponentID());
                }
                msg.setPillarID(query.getComponentID());
                msg.setDestination(activeContributors.get(query.getComponentID()));

                if (query.getMinTimestamp() != null) {
                    msg.setMinTimestamp(CalendarUtils.getXmlGregorianCalendar(query.getMinTimestamp()));
                }
                if (query.getMaxTimestamp() != null) {
                    msg.setMaxTimestamp(CalendarUtils.getXmlGregorianCalendar(query.getMaxTimestamp()));
                }
                if (query.getMaxNumberOfResults() != null) {
                    msg.setMaxNumberOfResults(BigInteger.valueOf(query.getMaxNumberOfResults()));
                }
                context.getMessageSender().sendMessage(msg);
            }
        }
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "GetChecksums";
    }

}
