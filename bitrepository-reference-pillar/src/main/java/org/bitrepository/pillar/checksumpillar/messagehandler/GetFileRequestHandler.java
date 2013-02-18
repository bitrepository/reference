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

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Class for performing the GetFile operation.
 */
public class GetFileRequestHandler extends ChecksumPillarMessageHandler<GetFileRequest> {
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public GetFileRequestHandler(MessageHandlerContext context, ChecksumStore refCache) {
        super(context,  refCache);
    }
    
    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public void processRequest(GetFileRequest message) throws RequestHandlerException {
        validatePillarId(message.getPillarID());

        getAuditManager().addAuditEvent(message.getCollectionID(), message.getFileID(), message.getFrom(), 
                "Failed getting file.", message.getAuditTrailInformation(), FileAction.FAILURE);

        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
        ri.setResponseText("The Checksum pillar is unable to deliver actual files.");
        throw new IllegalOperationException(ri);
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileRequest message) {
        return createFinalResponse(message);
    }

    /**
     * Creates a GetFileFinalResponse based on a GetFileRequest. Missing the 
     * following fields:
     * <br/> - FinalResponseInfo
     * 
     * @param request The GetFileRequest to base the final response on.
     * @return The GetFileFinalResponse based on the request.
     */
    private GetFileFinalResponse createFinalResponse(GetFileRequest request) {
        GetFileFinalResponse res = new GetFileFinalResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setFilePart(request.getFilePart());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());

        return res;
    }
}
