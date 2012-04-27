/*
 * #%L
 * Bitrepository Modifying Client
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
package org.bitrepository.modify.deletefile.conversation;

import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;

/**
 * The final state for the DeleteFile operation.
 */
public class DeleteFileFinished extends DeleteFileState {
    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    protected DeleteFileFinished(SimpleDeleteFileConversation conversation) {
        super(conversation);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForDeleteFileResponse response) {
        monitor.warning("Received IdentifyPillarsForPutFileResponse from '" + response.getPillarID() 
                + "' after the DeleteFile operation has ended.");
    }
    
    @Override
    public void onMessage(DeleteFileProgressResponse response) {
        monitor.warning("Received PutFileProgressResponse from '" + response.getPillarID() 
                + "' after the DeleteFile operation has ended.");
    }
    
    @Override
    public void onMessage(DeleteFileFinalResponse response) {
        monitor.warning("Received PutFileFinalResponse from '" + response.getPillarID() 
                + "' after the DeleteFile operation has ended.");
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
