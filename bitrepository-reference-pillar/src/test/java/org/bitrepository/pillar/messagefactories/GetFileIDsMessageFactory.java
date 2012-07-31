/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id: PillarTestMessageFactory.java 659 2011-12-22 15:56:07Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PillarTestMessageFactory.java $
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
package org.bitrepository.pillar.messagefactories;

import java.util.UUID;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

public class GetFileIDsMessageFactory extends ClientTestMessageFactory {

    final Settings settings;
    
    public GetFileIDsMessageFactory(Settings pSettings) {
        super(pSettings.getCollectionID());
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForGetFileIDsRequest createIdentifyPillarsForGetFileIDsRequest(String auditTrail,
            FileIDs fileId, String from, String replyTo) {
        IdentifyPillarsForGetFileIDsRequest res = new IdentifyPillarsForGetFileIDsRequest();
        initializeMessageDetails(res);
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileIDs(fileId);
        res.setFrom(from);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        
        return res;
    }

    public IdentifyPillarsForGetFileIDsResponse createIdentifyPillarsForGetFileIDsResponse(
            String correlationId, FileIDs fileId, String pillarId, String replyTo, 
            ResponseInfo responseInfo, TimeMeasureTYPE timeToDeliver, String toTopic) {
        IdentifyPillarsForGetFileIDsResponse res = new IdentifyPillarsForGetFileIDsResponse();
        initializeMessageDetails(res);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setFrom(pillarId);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        
        return res;
    }
    
    public GetFileIDsRequest createGetFileIDsRequest(String auditTrail, String correlationId, FileIDs fileId, 
            String from, String pillarId, String replyTo, String url, String toTopic) {
        GetFileIDsRequest res = new GetFileIDsRequest();
        initializeMessageDetails(res);
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setFrom(from);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResultAddress(url);
        res.setTo(toTopic);
        
        return res;
    }

    public GetFileIDsProgressResponse createGetFileIDsProgressResponse(String correlationId, FileIDs fileId, 
            String pillarId, String replyTo, ResponseInfo prInfo, String url, String toTopic) {
        GetFileIDsProgressResponse res = new GetFileIDsProgressResponse();
        initializeMessageDetails(res);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setFrom(pillarId);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(prInfo);
        res.setResultAddress(url);
        res.setTo(toTopic);
        
        return res;
    }

    public GetFileIDsFinalResponse createGetFileIDsFinalResponse(String correlationId, FileIDs fileId, 
            String pillarId, String replyTo, ResponseInfo frInfo, ResultingFileIDs results, String toTopic) {
        GetFileIDsFinalResponse res = new GetFileIDsFinalResponse();
        initializeMessageDetails(res);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setFrom(pillarId);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(frInfo);
        res.setResultingFileIDs(results);
        res.setTo(toTopic);
        
        return res;
    }
    
    /**
     * Method for generating new correlation IDs.
     * @return A unique correlation id.
     */
    public String getNewCorrelationID() {
        return UUID.randomUUID().toString();
    }
}
