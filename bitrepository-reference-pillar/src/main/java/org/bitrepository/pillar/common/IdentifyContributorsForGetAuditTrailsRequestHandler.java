/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.common;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.utils.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetStatus operation.
 */
public class IdentifyContributorsForGetAuditTrailsRequestHandler 
        extends PillarMessageHandler<IdentifyContributorsForGetAuditTrailsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     */
    public IdentifyContributorsForGetAuditTrailsRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public void handleMessage(IdentifyContributorsForGetAuditTrailsRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyContributorsForGetAuditTrailsRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            respondSuccessfulIdentification(message);
        } catch (IllegalArgumentException e) {
            log.warn("Illegal content of the message.", e);
            getAlarmDispatcher().handleIllegalArgumentException(e);
            
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            ri.setResponseText(e.getMessage());
            respondFailedIdentification(message, ri);
        } catch (RuntimeException e) {
            log.warn("Runtime failure.", e);
            getAlarmDispatcher().handleRuntimeExceptions(e);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FAILURE);
            ri.setResponseText(e.getMessage());
            respondFailedIdentification(message, ri);
        }
    }

    /**
     * Makes a success response for the identification.
     * @param message The request to base the response upon.
     */
    protected void respondSuccessfulIdentification(IdentifyContributorsForGetAuditTrailsRequest message) {
        IdentifyContributorsForGetStatusResponse response = createResponse(message);
        
        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);
        
        getMessageBus().sendMessage(response);
    }
    
    /**
     * Sends a bad response with the given cause.
     * @param message The identification request to respond to.
     * @param responseInfo The cause of the bad identification (e.g. which file is missing).
     */
    protected void respondFailedIdentification(IdentifyContributorsForGetAuditTrailsRequest message, 
            ResponseInfo responseInfo) {
        IdentifyContributorsForGetStatusResponse response = createResponse(message);
        
        response.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        response.setResponseInfo(responseInfo);
        
        getMessageBus().sendMessage(response);                
    }
    
    /**
     * Creates a IdentifyContributorsForGetStatusResponse based on a 
     * IdentifyContributorsForGetStatusRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - ResponseInfo
     * 
     * @param msg The IdentifyContributorsForGetStatusRequest to base the response on.
     * @return The response to the request.
     */
    protected IdentifyContributorsForGetStatusResponse createResponse(
            IdentifyContributorsForGetAuditTrailsRequest message) {
        IdentifyContributorsForGetStatusResponse res = new IdentifyContributorsForGetStatusResponse();
        
        res.setCollectionID(getSettings().getCollectionID());
        res.setContributor(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCorrelationID(message.getCorrelationID());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setMinVersion(MIN_VERSION);
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        res.setTo(message.getReplyTo());
        res.setVersion(VERSION);
        
        return res;
    }
}
