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

import java.math.BigInteger;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

public class GetFileMessageFactory extends ClientTestMessageFactory {

    final Settings settings;
    
    public GetFileMessageFactory(Settings pSettings) {
        super(pSettings.getCollectionID());
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest(String auditTrail, 
            String fileId, String from, String replyTo) {
        IdentifyPillarsForGetFileRequest res = new IdentifyPillarsForGetFileRequest();
        initializeMessageDetails(res);
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileID(fileId);
        res.setFrom(from);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        
        return res;
    }

    public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            String correlationId, String fileId, String pillarId, String replyTo, 
            ResponseInfo responseInfo, TimeMeasureTYPE timeToDeliver, String toTopic) {
        IdentifyPillarsForGetFileResponse res = new IdentifyPillarsForGetFileResponse();
        initializeMessageDetails(res);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setFrom(pillarId);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        
        return res;
    }
    
    public GetFileRequest createGetFileRequest(String auditTrail, String correlationId, String url, String fileId, 
            FilePart filePart, String from, String pillarId, String replyTo, String toTopic) {
        GetFileRequest res = new GetFileRequest();
        initializeMessageDetails(res);
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFilePart(filePart);
        res.setFrom(from);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        
        return res;
    }

    public GetFileProgressResponse createGetFileProgressResponse(ChecksumDataForFileTYPE csData, String correlationId, 
            String url, String fileId, FilePart filePart, String pillarId, long fileSize, ResponseInfo prInfo, 
            String replyTo, String toTopic) {
        GetFileProgressResponse res = new GetFileProgressResponse();
        initializeMessageDetails(res);
        res.setChecksumDataForExistingFile(csData);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFilePart(filePart);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setFrom(pillarId);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(prInfo);
        res.setTo(toTopic);
        
        return res;
    }

    public GetFileFinalResponse createGetFileFinalResponse(String correlationId, String url, String fileId, 
            FilePart filePart, String pillarId, String replyTo, ResponseInfo rInfo, String toTopic) {
        GetFileFinalResponse res = new GetFileFinalResponse();
        initializeMessageDetails(res);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFilePart(filePart);
        res.setFrom(pillarId);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(rInfo);
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
