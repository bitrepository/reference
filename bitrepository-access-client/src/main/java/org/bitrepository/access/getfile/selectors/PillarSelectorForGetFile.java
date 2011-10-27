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
package org.bitrepository.access.getfile.selectors;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Used to select a specific pillar and find the topic for this pillar. The selection is implemented by sending a 
 * <code>IdentifyPillarsForGetFileRequest</code> and processing the responses.
 *
 */
public abstract class PillarSelectorForGetFile {
    protected SelectedPillarForGetFileInfo selectedPillar;
    
    /**
     * Each time this method is called the selector will check the response to see whether the selected pillar should 
     * be changed. 
     * @param response The new response from a pillar.
     * @throws UnexpectedResponseException The selector was unable to process the response. The selector will still be 
     * able to continue, but the supplied response is ignored.
     */
    public final void processResponse(IdentifyPillarsForGetFileResponse response) throws UnexpectedResponseException {
        if (checkPillarResponseForSelection(response)) {
            selectedPillar = new SelectedPillarForGetFileInfo(
                    response.getPillarID(), response.getReplyTo(), response.getTimeToDeliver());
        }
    }
    
    /**
     * Contains the logic to determine whether a pillar should be selected. The concrete logic is found in the 
     * implementing classes.
     * @param response The response to based the decision on.
     * @return <code>true</code> if the pillar should be selected, else <code>false</code>.
     * @throws UnexpectedResponseException 
     */
    protected abstract boolean checkPillarResponseForSelection(IdentifyPillarsForGetFileResponse response) 
    		throws UnexpectedResponseException;
    
    /**
     * Return an array of pillars which haven't responded, but are expected to respond.
     * @return Return an array of pillars which haven't responded.
     */
    public abstract String[] getOutstandingPillars();

    /**
     * Container for information about a pillar which as been identified and are marked as 
     * selected for a GetFile request.
     */
    public class SelectedPillarForGetFileInfo extends SelectedPillarInfo {
        /** @see #getTimeToDeliver() */
        private final TimeMeasureTYPE timeToDeliver;       
        
        /** 
         * Delegates to SelectedPillarInfo construct.
         * @see #getTimeToDeliver() 
         */
        public SelectedPillarForGetFileInfo(String pillarID, String pillarTopic, TimeMeasureTYPE timeToDeliver) {
            super(pillarID, pillarTopic);
            this.timeToDeliver = timeToDeliver;
        }

        /**
         * @return The estimated time to deliver for the selected pillar as specified in the identify response.
         */
        public TimeMeasureTYPE getTimeToDeliver() {
            return timeToDeliver;
        }
    }
    
    public SelectedPillarForGetFileInfo getSelectedPillar() {
        return selectedPillar;
    }

    public abstract boolean isFinished() throws UnableToFinishException;

}
