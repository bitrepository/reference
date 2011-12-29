/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id: DeleteFileFinished.java 601 2011-12-05 15:29:25Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/DeleteFileFinished.java $
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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;

/**
 * The final state for the ReplaceFile operation.
 */
public class ReplaceFileFinished extends ReplaceFileState {
    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    protected ReplaceFileFinished(SimpleReplaceFileConversation conversation) {
        super(conversation);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForReplaceFileResponse response) {
        monitor.warning("Received IdentifyPillarsForReplaceFileResponse from '" + response.getPillarID() 
                + "' after the ReplaceFile opertaion has ended.");
    }

    @Override
    public void onMessage(ReplaceFileProgressResponse response) {
        monitor.warning("Received ReplaceFileProgressResponse from '" + response.getPillarID() 
                + "' after the ReplaceFile operation has ended.");
    }

    @Override
    public void onMessage(ReplaceFileFinalResponse response) {
        monitor.warning("Received ReplaceFileFinalResponse from '" + response.getPillarID() 
                + "' after the ReplaceFile operation has ended.");
    }

    @Override
    public void start() {
        // do nothing.
    }

    @Override
    public boolean hasEnded() {
        return true;
    }
    
}
