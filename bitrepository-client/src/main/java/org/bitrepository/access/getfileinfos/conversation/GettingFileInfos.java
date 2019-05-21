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
package org.bitrepository.access.getfileinfos.conversation;

import java.math.BigInteger;
import java.util.Collection;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositorymessages.GetFileInfosFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileIDsUtils;

/**
 * Models the behavior of a GetFileInfos conversation during the operation phase. That is, it begins with the 
 * sending of <code>GetFileInfosRequest</code> messages and finishes with on the reception of the 
 * <code>GetFileInfosFinalResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetFileInfosConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class GettingFileInfos extends PerformingOperationState {
    private final GetFileInfosConversationContext context;

    /*
     * @param context The conversation context.
     * @param contributors The list of components the checksums should be collected from.
     */
    public GettingFileInfos(GetFileInfosConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof GetFileInfosFinalResponse) {
            GetFileInfosFinalResponse response = (GetFileInfosFinalResponse) msg;
            boolean isPartialResult = response.isPartialResult() == null ? false : response.isPartialResult();
            getContext().getMonitor().contributorComplete(new FileInfosCompletePillarEvent(
                    response.getFrom(), response.getCollectionID(), response.getResultingFileInfos(),
                    response.getChecksumRequestForExistingFile(), isPartialResult
                    ));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for GetFileInfos response.");
        }        
    }

    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending GetFileInfosRequest's", activeContributors.keySet().toString());
        for(ContributorQuery query : context.getContributorQueries()) {
            if (activeContributors.containsKey(query.getComponentID())) {
                GetFileInfosRequest msg = new GetFileInfosRequest();
                initializeMessage(msg);
                msg.setChecksumRequestForExistingFile(context.getChecksumSpec());
                msg.setFileIDs(FileIDsUtils.createFileIDs(context.getFileID()));
                if(context.getUrlForResult() != null) {
                    msg.setResultAddress(context.getUrlForResult().toExternalForm() + "-" + query.getComponentID());
                }
                msg.setPillarID(query.getComponentID());
                msg.setDestination(activeContributors.get(query.getComponentID()));

                if (query.getMinTimestamp() != null) {
                    msg.setMinChecksumTimestamp(CalendarUtils.getXmlGregorianCalendar(query.getMinTimestamp()));
                }
                if (query.getMaxTimestamp() != null) {
                    msg.setMaxChecksumTimestamp(CalendarUtils.getXmlGregorianCalendar(query.getMaxTimestamp()));
                } if (query.getMaxNumberOfResults() != null) {
                    msg.setMaxNumberOfResults(BigInteger.valueOf(query.getMaxNumberOfResults().intValue()));
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
        return "GetFileInfos";
    }
    
}
