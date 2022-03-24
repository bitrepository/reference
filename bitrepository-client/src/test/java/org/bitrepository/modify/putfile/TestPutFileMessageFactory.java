/*
 * #%L
 * Bitrepository Modifying Client
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
package org.bitrepository.modify.putfile;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

import java.math.BigInteger;

/**
 * Messages creation factory for the PutFile tests.
 */
public class TestPutFileMessageFactory extends ClientTestMessageFactory {

    public TestPutFileMessageFactory(String clientID) {
        super(clientID);
    }

    /**
     * Retrieves a generic Identify message for the Put operation.
     * @return The IdentifyPillarsForPutFileRequest for the test.
     */
    public IdentifyPillarsForPutFileRequest createIdentifyPillarsForPutFileRequest(String from) {
        IdentifyPillarsForPutFileRequest identifyPillarsForPutFileRequest = new IdentifyPillarsForPutFileRequest();
        initializeMessageDetails(identifyPillarsForPutFileRequest);
        identifyPillarsForPutFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForPutFileRequest.setAuditTrailInformation(null);
        identifyPillarsForPutFileRequest.setFrom(from);
        return identifyPillarsForPutFileRequest;
    }

    /**
     * Retrieves a Identify message for the Put operation with specified CorrelationID, ReplyTo, and To.
     * @param correlationID The Correlation ID for the message.
     * @param replyTo The ReplyTo for the message.
     * @param toTopic The To for the message.
     * @return The requested IdentifyPillarsForPutFileRequest.
     */
    public IdentifyPillarsForPutFileRequest createIdentifyPillarsForPutFileRequest(String correlationID, 
            String replyTo, String toTopic, String fileID, long fileSize, String auditTrailInformation, String from) {
        IdentifyPillarsForPutFileRequest identifyPillarsForPutFileRequest = createIdentifyPillarsForPutFileRequest(from);
        identifyPillarsForPutFileRequest.setCorrelationID(correlationID);
        identifyPillarsForPutFileRequest.setReplyTo(replyTo);
        identifyPillarsForPutFileRequest.setDestination(toTopic);
        identifyPillarsForPutFileRequest.setFileID(fileID);
        identifyPillarsForPutFileRequest.setFileSize(BigInteger.valueOf(fileSize));
        identifyPillarsForPutFileRequest.setAuditTrailInformation(auditTrailInformation);
        return identifyPillarsForPutFileRequest;
    }

    /**
     * Creates a IdentifyPillarsForPutFileResponse based on a request and some constants.
     * @param receivedIdentifyRequestMessage The request to base the response on.
     * @param pillarID The id of the pillar, which responds.
     * @param pillarDestinationId The destination for this pillar.
     * @return The requested IdentifyPillarsForPutFileResponse.
     */
    public IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
            IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage,
            String pillarID, String pillarDestinationId) {
        IdentifyPillarsForPutFileResponse ipfpfResponse = new IdentifyPillarsForPutFileResponse();
        initializeMessageDetails(ipfpfResponse);
        ipfpfResponse.setDestination(receivedIdentifyRequestMessage.getReplyTo());
        ipfpfResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        ipfpfResponse.setPillarID(pillarID);
        ipfpfResponse.setReplyTo(pillarDestinationId);
        ipfpfResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        ipfpfResponse.setResponseInfo(createPositiveIdentificationResponseInfo());
        ipfpfResponse.setFrom(pillarID);

        ipfpfResponse.setPillarChecksumSpec(null);

        return ipfpfResponse;
    }
    
    /**
     * Method for creating a generic PutFilRequest.
     * @return The requested PutFileRequest.
     */
    public PutFileRequest createPutFileRequest(String from) {
        PutFileRequest putFileRequest = new PutFileRequest();
        initializeMessageDetails(putFileRequest);
        putFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        putFileRequest.setFileID(FILE_ID_DEFAULT);
        putFileRequest.setFrom(from);
        return putFileRequest;
    }

    /**
     * Method to create specific PutFileRequest.
     * @param pillarID The id of the pillar to request for Put.
     * @param toTopic The destination to send the message.
     * @param replyTo The where responses should be received.
     * @param correlationId The id of the message.
     * @param fileAddress The address where the file to be put can be retrieved from the pillar.
     * @param filesize The size of the file.
     * @return The requested PutFileRequest.
     */
    public PutFileRequest createPutFileRequest(String pillarID, String toTopic, String replyTo, String correlationId,
            String fileAddress, BigInteger filesize, String fileID, String auditTrailInformation, String from) {
        PutFileRequest putFileRequest = createPutFileRequest(from);
        putFileRequest.setPillarID(pillarID);
        putFileRequest.setDestination(toTopic);
        putFileRequest.setReplyTo(replyTo);
        putFileRequest.setCorrelationID(correlationId);
        putFileRequest.setFileAddress(fileAddress);
        putFileRequest.setFileSize(filesize);
        putFileRequest.setFileID(fileID);
        putFileRequest.setAuditTrailInformation(auditTrailInformation);
        
        putFileRequest.setChecksumDataForNewFile(null);
//        putFileRequest.setChecksumSpecs(null);
        
        return putFileRequest;
    }
    
    /**
     * Method to create a ProgressResponse to the put operation based on a PutFileRequest.
     * @param request The PutFileRequest to base the final response on.
     * @param pillarID The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested PutFileProgressResponse.
     */
    public PutFileProgressResponse createPutFileProgressResponse(PutFileRequest request, 
            String pillarID, String pillarDestinationId) {
        PutFileProgressResponse progressResponse = new PutFileProgressResponse();
        initializeMessageDetails(progressResponse);
        progressResponse.setDestination(request.getReplyTo());
        progressResponse.setCorrelationID(request.getCorrelationID());
        progressResponse.setReplyTo(pillarDestinationId);
        progressResponse.setPillarID(pillarID);
        progressResponse.setFileAddress(request.getFileAddress());
        progressResponse.setFileID(request.getFileID());
        progressResponse.setPillarChecksumSpec(null);
        progressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        progressResponse.setFrom(pillarID);
        
        return progressResponse;
    }
    
    /**
     * Method to create a FinalResponse to the put operation based on a PutFileRequest.
     * 
     * @param request The PutFileRequest to base the final response on.
     * @param pillarID The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested PutFileFinalResponse.
     */
    public PutFileFinalResponse createPutFileFinalResponse(PutFileRequest request,
            String pillarID, String pillarDestinationId) {
        PutFileFinalResponse finalResponse = new PutFileFinalResponse();
        initializeMessageDetails(finalResponse);
        finalResponse.setDestination(request.getReplyTo());
        finalResponse.setCorrelationID(request.getCorrelationID());
        finalResponse.setReplyTo(pillarDestinationId);
        finalResponse.setPillarID(pillarID);
        finalResponse.setFileAddress(request.getFileAddress());
        finalResponse.setFileID(request.getFileID());
        finalResponse.setPillarChecksumSpec(null);
        finalResponse.setResponseInfo(createCompleteResponseInfo());
        finalResponse.setFrom(pillarID);
        
        return finalResponse;
    }
}
