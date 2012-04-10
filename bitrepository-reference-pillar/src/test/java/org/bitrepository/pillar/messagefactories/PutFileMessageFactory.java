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
package org.bitrepository.pillar.messagefactories;

import java.math.BigInteger;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.TestMessageFactory;

public class PutFileMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public PutFileMessageFactory(Settings pSettings) {
        super(pSettings.getCollectionID());
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForPutFileRequest createIdentifyPillarsForPutFileRequest(String auditTrail, 
            String fileId, long fileSize, String replyTo) {
        IdentifyPillarsForPutFileRequest res = new IdentifyPillarsForPutFileRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileID(fileId);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
            String correlationId, ChecksumSpecTYPE pillarCsType, String pillarId, String replyTo,  
            ResponseInfo responseInfo, TimeMeasureTYPE timeToDeliver, String toTopic) {
        IdentifyPillarsForPutFileResponse res = new IdentifyPillarsForPutFileResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(pillarCsType);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }
    
    public PutFileRequest createPutFileRequest(String auditTrail, ChecksumDataForFileTYPE checksumDataForFile,
            ChecksumSpecTYPE csReturnSpec, String correlationId, String url, String fileId, Long fileSize,
            String pillarId, String replyTo, String toTopic) {
        PutFileRequest res = new PutFileRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setChecksumDataForNewFile(checksumDataForFile);
        res.setChecksumRequestForNewFile(csReturnSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public PutFileProgressResponse createPutFileProgressResponse(String correlationId, String url, String fileId, 
            String pillarId, ChecksumSpecTYPE checksumSpec, String replyTo, ResponseInfo prInfo, String toTopic) {
        PutFileProgressResponse res = new PutFileProgressResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setPillarChecksumSpec(checksumSpec);
        res.setReplyTo(replyTo);
        res.setResponseInfo(prInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public PutFileFinalResponse createPutFileFinalResponse(ChecksumDataForFileTYPE checksumNewFile, 
            String correlationId, String url, String fileId, ChecksumSpecTYPE checksumSpec, String pillarId, 
            String replyTo, ResponseInfo frInfo, String toTopic) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        res.setChecksumDataForNewFile(checksumNewFile);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(checksumSpec);
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
