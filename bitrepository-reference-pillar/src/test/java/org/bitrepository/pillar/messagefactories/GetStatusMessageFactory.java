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

import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

import java.util.UUID;

public class GetStatusMessageFactory extends ClientTestMessageFactory {

    final Settings settings;
    
    public GetStatusMessageFactory(Settings pSettings) {
        super(pSettings.getCollectionID());
        this.settings = pSettings;
    }
    
    public IdentifyContributorsForGetStatusRequest createIdentifyContributorsForGetStatusRequest( 
            String auditTrail, String from, String replyTo) {
        IdentifyContributorsForGetStatusRequest res = new IdentifyContributorsForGetStatusRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setCorrelationID(getNewCorrelationID());
        res.setFrom(from);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(settings.getCollectionDestination());
        res.setVersion(VERSION_DEFAULT);
        
        return res;
    }

    public IdentifyContributorsForGetStatusResponse createIdentifyContributorsForGetStatusResponse(
            String contributorId, String correlationId, String replyTo, ResponseInfo responseInfo, 
            TimeMeasureTYPE timeToDeliver, String toTopic) {
        IdentifyContributorsForGetStatusResponse res = new IdentifyContributorsForGetStatusResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFrom(contributorId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTimeToDeliver(timeToDeliver);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }
    
    public GetStatusRequest createGetStatusRequest(String auditTrail, String contributorId, String correlationId, 
            String from, String replyTo, String toTopic) {
        GetStatusRequest res = new GetStatusRequest();
        res.setAuditTrailInformation(auditTrail);
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFrom(from);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public GetStatusProgressResponse createGetStatusProgressResponse(String contributorId, String correlationId, 
            String replyTo, ResponseInfo responseInfo, String toTopic) {
        GetStatusProgressResponse res = new GetStatusProgressResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFrom(contributorId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setTo(toTopic);
        res.setVersion(VERSION_DEFAULT);

        return res;
    }

    public GetStatusFinalResponse createGetStatusFinalResponse(String contributorId, String correlationId, 
            String replyTo, ResponseInfo responseInfo, ResultingStatus status, String toTopic) {
        GetStatusFinalResponse res = new GetStatusFinalResponse();
        res.setCollectionID(settings.getCollectionID());
        res.setContributor(contributorId);
        res.setCorrelationID(correlationId);
        res.setFrom(contributorId);
        res.setMinVersion(VERSION_DEFAULT);
        res.setReplyTo(replyTo);
        res.setResponseInfo(responseInfo);
        res.setResultingStatus(status);
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
