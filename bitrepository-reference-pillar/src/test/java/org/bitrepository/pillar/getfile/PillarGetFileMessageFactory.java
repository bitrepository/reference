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
package org.bitrepository.pillar.getfile;

import java.math.BigInteger;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.TestMessageFactory;

public class PillarGetFileMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public PillarGetFileMessageFactory(Settings pSettings) {
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest(String replyTo, String fileId) {
        IdentifyPillarsForGetFileRequest res = new IdentifyPillarsForGetFileRequest();
        res.setAuditTrailInformation(null);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            String correlationId, String fileId, String replyTo, String pillarId, 
            TimeMeasureTYPE timeToDeliver, String toTopic, ResponseInfo responseInfo) {
        IdentifyPillarsForGetFileResponse res = new IdentifyPillarsForGetFileResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }
    
    public GetFileRequest createGetFileRequest(String correlationId, String url, String fileId, String pillarId, 
            String replyTo, String toTopic) {
        GetFileRequest res = new GetFileRequest();
        res.setAuditTrailInformation(null);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFilePart(null);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public GetFileProgressResponse createGetFileProgressResponse(String correlationId, String url, String fileId, 
            String pillarId, long fileSize, ResponseInfo prInfo, String replyTo, 
            String toTopic) {
        GetFileProgressResponse res = new GetFileProgressResponse();
        res.setChecksumDataForExistingFile(null);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFilePart(null);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(prInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public GetFileFinalResponse createGetFileFinalResponse(String correlationId, String url, String fileId, ResponseInfo frInfo, String pillarId, 
            String replyTo, String toTopic) {
        GetFileFinalResponse res = new GetFileFinalResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFilePart(null);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(frInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }
    
    /**
     * Method for generating new correlation IDs.
     * @return A unique correlation id.
     */
    private String getNewCorrelationID() {
        return UUID.randomUUID().toString();
    }
}
