/*
 * #%L
 * bitrepository-access-client
 * *
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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.PillarContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the GetFile operation.
 */
public class GetFileRequestHandler extends ChecksumPillarMessageHandler<GetFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public GetFileRequestHandler(PillarContext context, ChecksumStore refCache) {
        super(context,  refCache);
    }
    
    /**
     * Performs the GetFile operation.
     * @param message The GetFileRequest message to handle.
     */
    @Override
    public void handleMessage(GetFileRequest message) {
        ArgumentValidator.checkNotNull(message, "GetFileRequest message");

        try {
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            ri.setResponseText("The ChecksumPillar '" 
                    + getSettings().getReferenceSettings().getPillarSettings().getPillarID() + "' cannot handle a "
                    + "request for the actual file, since it only contains the checksum of the file.");
            
            sendFailedResponse(message, ri);
        } catch (RuntimeException e) {
            log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FAILURE);
            fri.setResponseText("Error: " + e.getMessage());
            sendFailedResponse(message, fri);
        }
    }
    
    /**
     * Method for sending a response telling that the operation has failed.
     * @param message The message requesting the operation.
     * @param frInfo The information about what went wrong.
     */
    protected void sendFailedResponse(GetFileRequest message, ResponseInfo frInfo) {
        log.info("Sending bad GetFileFinalResponse: " + frInfo);
        GetFileFinalResponse fResponse = createGetFileFinalResponse(message);
        fResponse.setResponseInfo(frInfo);
        getMessageBus().sendMessage(fResponse);
    }

    /**
     * Creates a GetFileFinalResponse based on a GetFileRequest. Missing the 
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - FinalResponseInfo
     * 
     * @param msg The GetFileRequest to base the final response on.
     * @return The GetFileFinalResponse based on the request.
     */
    private GetFileFinalResponse createGetFileFinalResponse(GetFileRequest msg) {
        GetFileFinalResponse res = new GetFileFinalResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setFilePart(msg.getFilePart());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setTo(msg.getReplyTo());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(getSettings().getCollectionID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());

        return res;
    }
}
