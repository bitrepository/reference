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
package org.bitrepository.pillar.referencepillar.getchecksums;

import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.TestMessageFactory;

public class PillarGetChecksumsMessageFactory extends TestMessageFactory {

    final Settings settings;
    
    public PillarGetChecksumsMessageFactory(Settings pSettings) {
        this.settings = pSettings;
    }
    
    public IdentifyPillarsForGetChecksumsRequest createIdentifyPillarsForGetChecksumsRequest( 
            ChecksumSpecTYPE csSpec, String replyTo, FileIDs fileId) {
        IdentifyPillarsForGetChecksumsRequest res = new IdentifyPillarsForGetChecksumsRequest();
        res.setAuditTrailInformation(null);
        res.setChecksumRequestForExistingFile(csSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFileIDs(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyPillarsForGetChecksumsResponse createIdentifyPillarsForGetChecksumsResponse(
            ChecksumSpecTYPE csSpec, String correlationId, FileIDs fileId, String replyTo, String pillarId, 
            TimeMeasureTYPE timeToDeliver, String toTopic, ResponseInfo responseInfo) {
        IdentifyPillarsForGetChecksumsResponse res = new IdentifyPillarsForGetChecksumsResponse();
        res.setChecksumRequestForExistingFile(csSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarChecksumSpec(null);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }
    
    public GetChecksumsRequest createGetChecksumsRequest(ChecksumSpecTYPE csSpec, String correlationId, FileIDs fileId,
            String pillarId, String replyTo, String url, String toTopic) {
        GetChecksumsRequest res = new GetChecksumsRequest();
        res.setAuditTrailInformation(null);
        res.setChecksumRequestForExistingFile(csSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResultAddress(url);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public GetChecksumsProgressResponse createGetChecksumsProgressResponse(ChecksumSpecTYPE csSpec, 
            String correlationId, FileIDs fileId, String pillarId, String replyTo, ResponseInfo prInfo, String url, 
            String toTopic) {
        GetChecksumsProgressResponse res = new GetChecksumsProgressResponse();
        res.setChecksumRequestForExistingFile(csSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setFileIDs(fileId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(prInfo);
        res.setResultAddress(url);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public GetChecksumsFinalResponse createGetChecksumsFinalResponse(ChecksumSpecTYPE csSpec, String correlationId, 
            FileIDs fileId, String pillarId, String replyTo, ResponseInfo frInfo, ResultingChecksums results, 
            String toTopic) {
        GetChecksumsFinalResponse res = new GetChecksumsFinalResponse();
        res.setChecksumRequestForExistingFile(csSpec);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(correlationId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setPillarID(pillarId);
        res.setReplyTo(replyTo);
        res.setResponseInfo(frInfo);
        res.setResultingChecksums(results);
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
