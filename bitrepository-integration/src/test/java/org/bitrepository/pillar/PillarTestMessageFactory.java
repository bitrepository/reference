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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs.ChecksumSpecsItems;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile.ChecksumDataItems;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.protocol.TestMessageFactory;

public class PillarTestMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public PillarTestMessageFactory(Settings pSettings) {
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForPutFileRequest createIdentifyPillarsForPutFileRequest(String replyTo) {
        IdentifyPillarsForPutFileRequest res = new IdentifyPillarsForPutFileRequest();
        res.setAuditTrailInformation(null);
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setMinVersion(VERSION_DEFAULT);
        res.setVersion(VERSION_DEFAULT);
        res.setTo(settings.getProtocol().getCollectionDestination());
        res.setReplyTo(replyTo);
        
        return res;
    }

    public IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
            String correlationId, String replyTo, String pillarId, 
            TimeMeasureTYPE timeToDeliver, String toTopic) {
        IdentifyPillarsForPutFileResponse res = new IdentifyPillarsForPutFileResponse();
        res.setAuditTrailInformation(null);
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(null);
        res.setVersion(VERSION_DEFAULT);
         
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
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        ChecksumsDataForNewFile csdataForNewFile = new ChecksumsDataForNewFile();
        {
            csdataForNewFile.setFileID(fileId);
            ChecksumDataItems cdi = new ChecksumDataItems();
            csdataForNewFile.setChecksumDataItems(cdi);
            csdataForNewFile.setNoOfItems(BigInteger.valueOf(0L));
        }
        res.setChecksumsDataForNewFile(csdataForNewFile);
        ChecksumSpecs csSpecs = new ChecksumSpecs();
        {
            // Add a single MD5 checksum check with no salt. 
            ChecksumSpecsItems csi = new ChecksumSpecsItems();
            ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
            {
                csSpec.setChecksumType("MD5");
            }
            csi.getChecksumSpecsItem().add(csSpec);
            csSpecs.setChecksumSpecsItems(csi);
            csSpecs.setNoOfItems(BigInteger.valueOf((long) csi.getChecksumSpecsItem().size()));
        }
        res.setChecksumSpecs(csSpecs);
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
            String pillarId, ChecksumSpecTYPE checksumSpec, ProgressResponseInfo prInfo, String replyTo, 
            String toTopic) {
        PutFileProgressResponse res = new PutFileProgressResponse();
        res.setAuditTrailInformation(null);
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setPillarChecksumSpec(checksumSpec);
        res.setProgressResponseInfo(prInfo);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public PutFileFinalResponse createPutFileFinalResponse(ChecksumsDataForNewFile checksumNewFile, 
            String correlationId, String url, String fileId, FinalResponseInfo frInfo, String pillarId, 
            ChecksumSpecTYPE checksumSpec, String replyTo, String toTopic) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        res.setAuditTrailInformation(null);
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        res.setChecksumsDataForNewFile(checksumNewFile);
        res.setCorrelationID(correlationId);
        res.setFileAddress(url);
        res.setFileID(fileId);
        res.setFinalResponseInfo(frInfo);
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
