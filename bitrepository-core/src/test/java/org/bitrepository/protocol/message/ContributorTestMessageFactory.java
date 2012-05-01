/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositorymessages.MessageRequest;

/**
 */
public class ContributorTestMessageFactory extends TestMessageFactory {
    protected final String collectionDestination;
    protected final String contributorID;
    protected final String contributorDestination;
    protected final String clientID;
    protected final String clientDestination;

    public ContributorTestMessageFactory(
            String collectionID, String collectionDestination, String contributorID,
            String contributorDestination, String clientID, String clientDestination) {
        super(collectionID);
        this.contributorID = contributorID;
        this.contributorDestination = contributorDestination;
        this.collectionDestination = collectionDestination;
        this.clientID = clientID;
        this.clientDestination = clientDestination;
    }

    protected void initializeRequestDetails(MessageRequest request, String correlationID) {
        initializeMessageDetails(request);
        request.setCorrelationID(correlationID);
        request.setTo(collectionDestination);
        request.setCorrelationID(CORRELATION_ID_DEFAULT);
        request.setFrom(clientID);
        request.setReplyTo(clientDestination);
    }

    protected void initializeIdentifyRequestDetails(MessageRequest identifyRequest) {
        initializeMessageDetails(identifyRequest);
        identifyRequest.setTo(collectionDestination);
        identifyRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyRequest.setFrom(clientID);
        identifyRequest.setReplyTo(clientDestination);
    }
}
