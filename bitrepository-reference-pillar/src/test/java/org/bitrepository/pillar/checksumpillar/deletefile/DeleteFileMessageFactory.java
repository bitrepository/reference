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
package org.bitrepository.pillar.checksumpillar.deletefile;

import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.TestMessageFactory;

public class DeleteFileMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public DeleteFileMessageFactory(Settings pSettings) {
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForDeleteFileRequest createIdentifyPillarsForDeleteFileRequest( 
            String auditTrail, String replyTo, String fileId) {
        IdentifyPillarsForDeleteFileRequest res = new IdentifyPillarsForDeleteFileRequest();
        
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyPillarsForDeleteFileResponse createIdentifyPillarsForDeleteFileResponse(
            ChecksumSpecTYPE pillarCsSpec, String correlationId, String fileId, String replyTo, String pillarId, 
            TimeMeasureTYPE timeToDeliver, String toTopic, ResponseInfo responseInfo) {
        IdentifyPillarsForDeleteFileResponse res = new IdentifyPillarsForDeleteFileResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(pillarCsSpec);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }
    
    public DeleteFileRequest createGetDeleteFile(String auditTrail, ChecksumDataForFileTYPE csData,
            ChecksumSpecTYPE csSpec, String correlationId, String fileId, String pillarId, String replyTo, String url, 
            String toTopic) {
        DeleteFileRequest res = new DeleteFileRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setChecksumDataForExistingFile(csData);
        res.setChecksumRequestForExistingFile(csSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public DeleteFileProgressResponse createGetDeleteFileResponse(String correlationId, String fileId, String pillarId,
            String replyTo, ResponseInfo prInfo, String url, String toTopic) {
        DeleteFileProgressResponse res = new DeleteFileProgressResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(prInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public DeleteFileFinalResponse createDeleteFileFinalResponse(ChecksumDataForFileTYPE csData, String correlationId, 
            String fileId, String pillarId, String replyTo, ResponseInfo frInfo, ResultingChecksums results, 
            String toTopic) {
        DeleteFileFinalResponse res = new DeleteFileFinalResponse();
        res.setChecksumDataForExistingFile(csData);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileID(fileId);
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
    public String getNewCorrelationID() {
        return UUID.randomUUID().toString();
    }
}
