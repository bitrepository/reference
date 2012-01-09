/*
 * #%L
 * Bitrepository Access Client
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
package org.bitrepository.access.audittrails;

import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.protocol.TestMessageFactory;

/**
 * Constructs the GetFile specific messages.
 * 
 * ToDo based on example messages.
 */
public class TestGetAuditTrailsMessageFactory extends TestMessageFactory {
    protected final String collectionID;

    public TestGetAuditTrailsMessageFactory(String slaID) {
        super();
        this.collectionID = slaID;
    }

    public IdentifyContributorsForGetAuditTrailsRequest createIdentifyContributorsForGetAuditTrailsRequest() {
        IdentifyContributorsForGetAuditTrailsRequest identifyContributorsForGetAuditTrailsRequest = 
                new IdentifyContributorsForGetAuditTrailsRequest();
        identifyContributorsForGetAuditTrailsRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyContributorsForGetAuditTrailsRequest.setMinVersion(VERSION_DEFAULT);
        identifyContributorsForGetAuditTrailsRequest.setCollectionID(collectionID);
        identifyContributorsForGetAuditTrailsRequest.setVersion(VERSION_DEFAULT);
        return identifyContributorsForGetAuditTrailsRequest;
    }

    public IdentifyContributorsForGetAuditTrailsResponse createIdentifyContributorsForGetAuditTrailsResponse(
            IdentifyContributorsForGetAuditTrailsRequest receivedIdentifyRequestMessage, String contributor1,
            String pillar1DestinationId) {
        // TODO Auto-generated method stub
        return null;
    }
}

