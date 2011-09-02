/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: SpecificPillarSelectorForGetFile.java 240 2011-07-28 07:55:25Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getfile/selectors/SpecificPillarSelectorForGetFile.java $
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
package org.bitrepository.access.getchecksums.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.protocol.pillarselector.AbstractSinglePillarSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for selecting pillars for the GetChecksums operation.
 */
public class PillarSelectorForGetChecksums {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The id for the pillar to select.*/
    private List<String> outstandingPillarsToSelect = new ArrayList<String>();
    /** The map between the IDs of the selected pillars and their destination. */
    private Map<String, String> selectedPillars = new HashMap<String, String>();
    
    /**
     * Constructor.
     * @param pillars The IDs of the pillars to be selected.
     */
    public PillarSelectorForGetChecksums(String ... pillars) {
        for(String p : pillars) {
            outstandingPillarsToSelect.add(p);
        }
    }
    
    /**
     * Method for processing a IdentifyPillarsForGetChecksumsResponse. Checks whether the response is from the wanted
     * expected pillar.
     * @param response The response identifying a pillar for the GetChecksums operation.
     */
    public void processResponse(IdentifyPillarsForGetChecksumsResponse response) {
        log.info("Processing response for '" + response.getPillarID() + "'.");
        if(outstandingPillarsToSelect.contains(response.getPillarID())) {
            
            // TODO validate the content of the response (e.g. the IdentifyResponseInfo)
            
            selectedPillars.put(response.getPillarID(), response.getReplyTo());
            outstandingPillarsToSelect.remove(response.getPillarID());
        } else {
            // handle case when the response has be received twice.
            if(selectedPillars.containsKey(response.getPillarID())) {
                String destination = selectedPillars.get(response.getPillarID());
                if(destination.equals(response.getReplyTo())) {
                    log.debug("Received another response from pillar '" + response.getPillarID() 
                            + "' with identical destination.");
                } else {
                    log.warn("Received responses from pillar '" + response.getPillarID() 
                            + "' with diverging destinations: '" + destination + "' and '" + response.getReplyTo() 
                            + "'. Using the first received: '" + destination + "'");               
                }
            } else {
                log.debug("Ignoring identification response from '" + response.getPillarID() + "'");
            }
        }
    }
    
    public boolean isFinished() {
        return outstandingPillarsToSelect.isEmpty();
    }
    
    /**
     * Method for identifying the pillars, which needs to be identified for this operation to be finished.
     * @return An array of the IDs of the pillars which have not yet responded.
     */
    public Collection<String> getOutstandingPillars() {
        return outstandingPillarsToSelect;
    }

    /**
     * Method for retrieving the destinations of the selected pillars.
     * @return A mapping between the IDs of the pillars and their destinations.
     */
    public Map<String, String> getPillarDestination() {
        return selectedPillars;
    }
}
