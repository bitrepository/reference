/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: GetFileState.java 213 2011-07-05 10:07:06Z bam $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getfile/conversation/GetFileState.java $
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
package org.bitrepository.modify.put.conversation;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The state for the PutFile communication, where the Put is finished.
 */
public class PutFileFinished extends PutFileState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    protected PutFileFinished(SimplePutFileConversation conversation) {
        super(conversation);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received IdentifyPillarsForPutFileResponse from '" + response.getPillarID() 
                + "' after the PutFile has ended.");
    }

    @Override
    public void onMessage(PutFileProgressResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received PutFileProgressResponse from '" + response.getPillarID() 
                + "' after the PutFile has ended.");
    }

    @Override
    public void onMessage(PutFileFinalResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received PutFileFinalResponse from '" + response.getPillarID() 
                + "' after the PutFile has ended.");
    }
}
