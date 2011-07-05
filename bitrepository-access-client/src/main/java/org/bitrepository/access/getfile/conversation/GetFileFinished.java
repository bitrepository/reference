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
package org.bitrepository.access.getfile.conversation;

import org.bitrepository.access.getfile.selectors.FastestPillarSelectorForGetFile;
import org.bitrepository.access.getfile.selectors.SpecificPillarSelectorForGetFile;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFileFinished extends GetFileState {

    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    public GetFileFinished(SimpleGetFileConversation conversation) {
        super(conversation);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse response) {
        if (conversation.selector instanceof SpecificPillarSelectorForGetFile) {
            log.debug("(ConversationID: " + conversation.getConversationID() +  ") " +
                    "Received IdentifyPillarsForGetFileResponse from " + response.getPillarID() + 
                    " after finishing conversation.");
        } else if (conversation.selector instanceof FastestPillarSelectorForGetFile) {
            log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                    "Received IdentifyPillarsForGetFileResponse from " + response.getPillarID() + 
                    " after finishing conversation.");
        }
    }
    
    @Override
    public void onMessage(GetFileProgressResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received GetFileProgressResponse from " + response.getPillarID() + " after finishing conversation.");
    }
    
    @Override
    public void onMessage(GetFileFinalResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received GetFileFinalResponse from " + response.getPillarID() + " after finishing conversation.");
    }
}
