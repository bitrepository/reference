/*
 * #%L
 * Bitrepository Integration
 * 
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
package org.bitrepository.pillar;

import java.math.BigInteger;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.TestMessageFactory;

public class PillarTestMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public PillarTestMessageFactory(Settings pSettings) {
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForPutFileRequest createIdentifyPillarsForPutFileRequest(String replyTo) {
        IdentifyPillarsForPutFileRequest res = new IdentifyPillarsForPutFileRequest();
        res.setAuditTrailInformation(null);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setMinVersion(VERSION_DEFAULT);
        res.setVersion(VERSION_DEFAULT);
        res.setTo(settings.getCollectionDestination());
        res.setReplyTo(replyTo);
        
        return res;
    }

    public IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
            String correlationId, String replyTo, String pillarId, 
            TimeMeasureTYPE timeToDeliver, String toTopic, ResponseInfo responseInfo) {
        IdentifyPillarsForPutFileResponse res = new IdentifyPillarsForPutFileResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(null);
        res.setVersion(VERSION_DEFAULT);
        res.setResponseInfo(responseInfo);
         
        res.setCorrelationID(correlationId);
        res.setReplyTo(replyTo);
        res.setPillarID(pillarId);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        
        return res;
    }
    
    public PutFileRequest createPutFileRequest(String correlationId, String url, String fileId, Long fileSize,
            String pillarId, String replyTo, String toTopic) {
        PutFileRequest res = new PutFileRequest();
        res.setAuditTrailInformation(null);
        
        ChecksumDataForFileTYPE checksumDataForFile = new ChecksumDataForFileTYPE();
        ChecksumSpecTYPE csDeliveredSpec = new ChecksumSpecTYPE();
        csDeliveredSpec.setChecksumType(ChecksumType.MD5);
        checksumDataForFile.setChecksumSpec(csDeliveredSpec);       
        checksumDataForFile.setChecksumValue(new String("940a51b250e7aa82d8e8ea31217ff267").getBytes());
        checksumDataForFile.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumDataForNewFile(checksumDataForFile);

        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        
        ChecksumSpecTYPE csReturnSpec = new ChecksumSpecTYPE();
        csReturnSpec.setChecksumType(ChecksumType.SHA1);
        res.setChecksumRequestForNewFile(csReturnSpec);
        
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
            String pillarId, ChecksumSpecTYPE checksumSpec, ResponseInfo prInfo, String replyTo, 
            String toTopic) {
        PutFileProgressResponse res = new PutFileProgressResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setPillarChecksumSpec(checksumSpec);
        res.setResponseInfo(prInfo);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public PutFileFinalResponse createPutFileFinalResponse(ChecksumDataForFileTYPE checksumNewFile, 
            String correlationId, String url, String fileId, ResponseInfo frInfo, String pillarId, 
            ChecksumSpecTYPE checksumSpec, String replyTo, String toTopic) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setChecksumDataForNewFile(checksumNewFile);
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setResponseInfo(frInfo);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setPillarChecksumSpec(checksumSpec);
        res.setReplyTo(replyTo);
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
