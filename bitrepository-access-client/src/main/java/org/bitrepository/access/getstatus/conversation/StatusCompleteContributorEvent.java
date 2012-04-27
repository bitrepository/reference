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
package org.bitrepository.access.getstatus.conversation;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.client.eventhandler.ContributorEvent;

/**
 * The event for a complete GetStatus operation with a contributor.
 */
public class StatusCompleteContributorEvent extends ContributorEvent {

    /** The status for the contributor.*/
    private final ResultingStatus status;
    
    /**
     * Constructor.
     * @param info The information about the event.
     * @param contributorID The ID of the contributor.
     * @param status The status for the contributor.
     * @param conversationID The ID for the conversation.
     */
    public StatusCompleteContributorEvent(String info, String contributorID, ResultingStatus status, 
            String conversationID) {
        super(OperationEventType.COMPONENT_COMPLETE, info, contributorID, conversationID);
        this.status = status;
    }
    
    /**
     * @return The status of for the contributor.
     */
    public ResultingStatus getStatus() {
        return status;
    }
    
    @Override
    public String additionalInfo() {
        return super.additionalInfo() + ", resulting status: " + status;
    }
}
