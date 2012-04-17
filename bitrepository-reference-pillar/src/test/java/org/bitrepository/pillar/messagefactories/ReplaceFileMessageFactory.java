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
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.TestMessageFactory;

public class ReplaceFileMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public ReplaceFileMessageFactory(Settings pSettings) {
        super(pSettings.getCollectionID());
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForReplaceFileRequest createIdentifyPillarsForReplaceFileRequest( 
            String auditTrail, String fileId, long fileSize, String from, String replyTo) {
        IdentifyPillarsForReplaceFileRequest res = new IdentifyPillarsForReplaceFileRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileID(fileId);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setFrom(from);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyPillarsForReplaceFileResponse createIdentifyPillarsForReplaceFileResponse(
            String correlationId, String fileId, ChecksumSpecTYPE csType, String pillarId, String replyTo,  
            ResponseInfo responseInfo, TimeMeasureTYPE timeToDeliver, String toTopic) {
        IdentifyPillarsForReplaceFileResponse res = new IdentifyPillarsForReplaceFileResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setFrom(pillarId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(csType);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }
    
    public ReplaceFileRequest createReplaceFileRequest(String auditTrail, ChecksumDataForFileTYPE existingChecksumData, 
            ChecksumDataForFileTYPE newChecksumData, ChecksumSpecTYPE csExistingRequest, 
            ChecksumSpecTYPE csNewRequest, String correlationId, String url, String fileId, long fileSize, 
            String from, String pillarId, String replyTo, String toTopic) {
        ReplaceFileRequest res = new ReplaceFileRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setChecksumDataForExistingFile(existingChecksumData);
        res.setChecksumDataForNewFile(newChecksumData);
        res.setChecksumRequestForExistingFile(csExistingRequest);
        res.setChecksumRequestForNewFile(csNewRequest);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setFrom(from);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public ReplaceFileProgressResponse createReplaceFileProgressResponse(String correlationId, String url,
            String fileId, ChecksumSpecTYPE pillarCsSpec, String pillarId, String replyTo, ResponseInfo responseInfo, 
            String toTopic) {
        ReplaceFileProgressResponse res = new ReplaceFileProgressResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFrom(pillarId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(pillarCsSpec);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public ReplaceFileFinalResponse createReplaceFileFinalResponse(ChecksumDataForFileTYPE csDataExisting, 
            ChecksumDataForFileTYPE csDataNew, String correlationId, String url, String fileId, 
            ChecksumSpecTYPE pillarCsSpec, String pillarId, String replyTo, ResponseInfo responseInfo, String toTopic) {
        ReplaceFileFinalResponse res = new ReplaceFileFinalResponse();
        res.setChecksumDataForExistingFile(csDataExisting);
        res.setChecksumDataForNewFile(csDataNew);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFrom(pillarId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(pillarCsSpec);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
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
