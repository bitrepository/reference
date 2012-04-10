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
/*
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
package org.bitrepository.access.getaudittrails;

import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.protocol.TestMessageFactory;

/**
 * Constructs the GetFile specific messages.
 * 
 * ToDo based on example messages.
 */
public class TestGetAuditTrailsMessageFactory extends TestMessageFactory {

    public TestGetAuditTrailsMessageFactory(String collectionID) {
        super(collectionID);
    }

    public IdentifyContributorsForGetAuditTrailsResponse createIdentifyContributorsForGetAuditTrailsResponse(
            IdentifyContributorsForGetAuditTrailsRequest request, String componentID, String replyTo) {
        IdentifyContributorsForGetAuditTrailsResponse response = new IdentifyContributorsForGetAuditTrailsResponse();
        setResponseDetails(response, request, componentID,  replyTo);
        response.setContributor(componentID);
        response.setResponseInfo(IDENTIFY_INFO_DEFAULT);
        response.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);

        return response;
    }

    public GetAuditTrailsProgressResponse createGetAuditTrailsProgressResponse(
            GetAuditTrailsRequest request, String componentID, String replyTo) {
        GetAuditTrailsProgressResponse response = new GetAuditTrailsProgressResponse();
        setResponseDetails(response, request, componentID,  replyTo);
        response.setFileIDs(request.getFileIDs());
        response.setResponseInfo(PROGRESS_INFO_DEFAULT);
        return response;
    }

    public GetAuditTrailsFinalResponse createGetAuditTrailsFinalResponse(
            GetAuditTrailsRequest request, String componentID, String replyTo, ResultingAuditTrails result) {
        GetAuditTrailsFinalResponse response = new GetAuditTrailsFinalResponse();
        setResponseDetails(response, request, componentID,  replyTo);
        response.setFileIDs(request.getFileIDs());
        response.setContributor(componentID);
        response.setResponseInfo(FINAL_INFO_DEFAULT);
        response.setResultingAuditTrails(result);
        return response;
    }
}

