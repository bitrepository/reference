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
package org.bitrepository.access.getstatus.selector;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Class for selecting contributors for the GetStatus operation.
 */
public class ContributorSelectorForGetStatus {
    /** Used for tracking who has answered. */
    private final PillarsResponseStatus responseStatus;
    /** The contributors which have been selected for a status request. */
    private final List<SelectedPillarInfo> selectedContributors = new LinkedList<SelectedPillarInfo>(); 
    
    /**
     * Constructor.
     * @param contributorsWhichShouldRespond The IDs of the contributors to be selected.
     */
    public ContributorSelectorForGetStatus(Collection<String> contributorsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(contributorsWhichShouldRespond, "contributorsWhichShouldRespond");
        responseStatus = new PillarsResponseStatus(contributorsWhichShouldRespond);
    }
    
    /**
     * Method for processing a IdentifyContributorsForGetStatusResponse. Checks whether the response is from a 
     * expected contributor.
     * @param response The response identifying a contributor for the GetStatus operation.
     */
    public void processResponse(IdentifyContributorsForGetStatusResponse response) 
            throws UnexpectedResponseException, NegativeResponseException {
        responseStatus.responseReceived(response.getContributor());
        validateResponse(response.getResponseInfo());
        if (!ResponseCode.IDENTIFICATION_POSITIVE.value().equals(
                response.getResponseInfo().getResponseCode().value())) {
            throw new NegativeResponseException(response.getContributor() + " sent negative response " + 
                    response.getResponseInfo().getResponseText(), 
                    response.getResponseInfo().getResponseCode());
        }
        selectedContributors.add(new SelectedPillarInfo(response.getContributor(), response.getReplyTo()));
    }
    
    /**
     * Method for validating the response.
     * @param irInfo The ResponseInfo to validate.
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
     * @return Whether any contributors are outstanding.
     */
    public boolean isFinished() {
        return responseStatus.haveAllPillarResponded();
    }
    
    /**
     * Method for identifying the contributors, which needs to be identified for this operation to be finished.
     * @return An array of the IDs of the contributors which have not yet responded.
     */
    public List<String> getOutstandingContributors() {
        return Arrays.asList(responseStatus.getOutstandPillars());
    }
    
    /**
     * @return The selected contributors.
     */
    public List<SelectedPillarInfo> getSelectedContributors() {
        return selectedContributors;
    }
}
