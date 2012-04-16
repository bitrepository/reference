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
package org.bitrepository.access.getstatus;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.TestMessageFactory;

public class TestGetStatusMessageFactory extends TestMessageFactory {
    public TestGetStatusMessageFactory(String collectionID) {
        super(collectionID);
    }
    
    public IdentifyContributorsForGetStatusResponse createIdentifyContributorsForGetStatusResponse(
            IdentifyContributorsForGetStatusRequest request, String from) {
        IdentifyContributorsForGetStatusResponse response = createIdentifyContributorsForGetStatusResponse();
        response.setCorrelationID(request.getCorrelationID());
        response.setTo(request.getReplyTo());
        response.setReplyTo("ME");
        response.setFrom(from);
        response.setResponseInfo(IDENTIFY_INFO_DEFAULT);
        response.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        
        return response;
    }
    
    public IdentifyContributorsForGetStatusResponse createIdentifyContributorsForGetStatusResponse() {
        IdentifyContributorsForGetStatusResponse response = new IdentifyContributorsForGetStatusResponse();
        setMessageDetails(response);
                
        return response;
    }
    
    public GetStatusProgressResponse createGetStatusProgressResponse() {
        GetStatusProgressResponse response = new GetStatusProgressResponse();
        setMessageDetails(response);
        
        return response;
    }
    
    public GetStatusFinalResponse createGetStatusFinalResponse(GetStatusRequest request, String from) {
        GetStatusFinalResponse response = createGetStatusFinalResponse();
        response.setCorrelationID(request.getCorrelationID());
        response.setTo(request.getReplyTo());
        response.setReplyTo("ME");
        response.setFrom(from);
        response.setContributor(request.getContributor());
        response.setResponseInfo(FINAL_INFO_DEFAULT);
        ResultingStatus status = new ResultingStatus();
        status.setStatusTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
        StatusInfo info = new StatusInfo();
        info.setStatusCode(StatusCode.OK);
        info.setStatusText("All is good");
        status.setStatusInfo(info);
        response.setResultingStatus(status);
        return response;
    }
    
    public GetStatusFinalResponse createGetStatusFinalResponse() {
        GetStatusFinalResponse response = new GetStatusFinalResponse();
        setMessageDetails(response);
        
        return response;
    }
}
