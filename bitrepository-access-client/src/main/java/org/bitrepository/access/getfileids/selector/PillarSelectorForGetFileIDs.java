/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: PillarSelectorForGetChecksums.java 372 2011-10-27 15:15:20Z mss $
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
package org.bitrepository.access.getfileids.selector;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Class for selecting pillars for the GetFileIDs operation.
 */
public class PillarSelectorForGetFileIDs {
    private final PillarsResponseStatus responseStatus;
    private final List<SelectedPillarInfo> selectedPillars = new LinkedList<SelectedPillarInfo>(); 

    /**
     * Constructor.
     * @param pillars The IDs of the pillars to be selected.
     */
    public PillarSelectorForGetFileIDs(Collection<String> pillarsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(pillarsWhichShouldRespond, "pillarsWhichShouldRespond");
        responseStatus = new PillarsResponseStatus(pillarsWhichShouldRespond);
    }

    /**
     * Method for processing a IdentifyPillarsForGetFileIDsResponse. Checks whether the response is from the wanted
     * expected pillar.
     * @param response The response identifying a pillar for the GetFileIDs operation.
     */
    public void processResponse(IdentifyPillarsForGetFileIDsResponse response) 
            throws UnexpectedResponseException, NegativeResponseException {
        responseStatus.responseReceived(response.getPillarID());
        validateResponse(response.getIdentifyResponseInfo());
        if (!IdentifyResponseCodePositiveType.IDENTIFICATION_POSITIVE.value().equals(
                new BigInteger(response.getIdentifyResponseInfo().getIdentifyResponseCode()))) {
            throw new NegativeResponseException(response.getPillarID() + " sent negative response " + 
                    response.getIdentifyResponseInfo().getIdentifyResponseText(), 
                    ErrorcodeGeneralType.valueOf(response.getIdentifyResponseInfo().getIdentifyResponseCode()));
        }
        selectedPillars.add(new SelectedPillarInfo(response.getPillarID(), response.getReplyTo()));
    }

    /**
     * Method for validating the response.
     * @param irInfo The IdentifyResponseInfo to validate.
     */
    private void validateResponse(IdentifyResponseInfo irInfo) throws UnexpectedResponseException {
        String errorMessage = null;

        if(irInfo == null) {
            errorMessage = "Response code was null";
        }

        String responseCode = irInfo.getIdentifyResponseCode();
        if(responseCode == null) {
            errorMessage = "Response code was null";
        }

        IdentifyResponseCodePositiveType.IDENTIFICATION_POSITIVE.value().equals(
                new BigInteger(responseCode));
        if (errorMessage != null) throw new UnexpectedResponseException(
                "Invalid IdentifyResponse from response.getPillarID(), " + errorMessage);
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
    public String[] getOutstandingPillars() {
        return responseStatus.getOutstandPillars();
    }

    /**
     * @return The selected pillars.
     */
    public List<SelectedPillarInfo> getSelectedPillars() {
        return selectedPillars;
    }
}
