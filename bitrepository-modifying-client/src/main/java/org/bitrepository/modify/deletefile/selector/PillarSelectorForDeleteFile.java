/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: PillarSelectorForGetChecksums.java 624 2011-12-08 15:35:06Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getchecksums/selector/PillarSelectorForGetChecksums.java $
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
package org.bitrepository.modify.deletefile.selector;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Class for selecting pillars for the DeleteFile operation.
 */
public class PillarSelectorForDeleteFile {
    /** Used for tracking who has answered. */
    private final PillarsResponseStatus responseStatus;
    /** The pillars which have been selected for a checksums request. */
    private final List<SelectedPillarInfo> selectedPillars = new LinkedList<SelectedPillarInfo>(); 
    
    /**
     * Constructor.
     * @param pillarsWhichShouldRespond The IDs of the pillars to be selected.
     */
    public PillarSelectorForDeleteFile(Collection<String> pillarsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(pillarsWhichShouldRespond, "pillarsWhichShouldRespond");
        responseStatus = new PillarsResponseStatus(pillarsWhichShouldRespond);
    }
    
    /**
     * Method for processing a IdentifyPillarsForDeleteFileResponse. Checks whether the response is from the wanted
     * expected pillar.
     * @param response The response identifying a pillar for the DeleteFile operation.
     */
    public void processResponse(IdentifyPillarsForDeleteFileResponse response) 
            throws UnexpectedResponseException, NegativeResponseException {
        responseStatus.responseReceived(response.getPillarID());
        validateResponse(response.getResponseInfo());
        if (!ResponseCode.IDENTIFICATION_POSITIVE.value().equals(
                response.getResponseInfo().getResponseCode().value())) {
            throw new NegativeResponseException(response.getPillarID() + " sent negative response " + 
                    response.getResponseInfo().getResponseText(), 
                    response.getResponseInfo().getResponseCode());
        }
        selectedPillars.add(new SelectedPillarInfo(response.getPillarID(), response.getReplyTo()));
    }
    
    /**
     * Method for validating the response.
     * @param irInfo The IdentifyResponseInfo to validate.
     */
    private void validateResponse(ResponseInfo irInfo) throws UnexpectedResponseException {
        if(irInfo == null) {
            throw new UnexpectedResponseException("Response info was null");
        }
        
        ResponseCode responseCode = irInfo.getResponseCode();
        if(responseCode == null) {
            throw new UnexpectedResponseException("Response code was null, with text: " + irInfo.getResponseText());
        }
        
        if (responseCode != ResponseCode.IDENTIFICATION_POSITIVE) {
            throw new UnexpectedResponseException("Invalid IdentifyResponse. Expected '" 
                    + ResponseCode.IDENTIFICATION_POSITIVE + "' but received: '" + responseCode 
                    + "', with text '" + irInfo.getResponseText() + "'");
        }
    }
    
    /**
     * Tells whether the selection is finished.
     * @return Whether any pillars are outstanding.
     */
    public boolean isFinished() {
        return responseStatus.haveAllPillarResponded();
    }
    
    /**
     * Method for identifying the pillars, which needs to be identified for this operation to be finished.
     * @return An array of the IDs of the pillars which have not yet responded.
     */
    public List<String> getOutstandingPillars() {
        return Arrays.asList(responseStatus.getOutstandPillars());
    }
    
    /**
     * @return The selected pillars.
     */
    public List<SelectedPillarInfo> getSelectedPillars() {
        return selectedPillars;
    }
}
