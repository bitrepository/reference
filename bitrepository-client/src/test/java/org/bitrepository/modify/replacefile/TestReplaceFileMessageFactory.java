/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id: TestReplaceFileMessageFactory.java 648 2011-12-19 14:55:11Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/test/java/org/bitrepository/modify/putfile/TestReplaceFileMessageFactory.java $
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
package org.bitrepository.modify.replacefile;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

import java.math.BigInteger;

/**
 * Messages creation factory for the ReplaceFile tests.
 */
public class TestReplaceFileMessageFactory extends ClientTestMessageFactory {

    /**
     * @param clientID The id for the collection, where the factory belong.
     */
    public TestReplaceFileMessageFactory(String clientID) {
        super(clientID);
    }

    /**
     * Retrieves a generic Identify message for the Replace operation.
     * @return The IdentifyPillarsForReplaceFileRequest for the test.
     */
    public IdentifyPillarsForReplaceFileRequest createIdentifyPillarsForReplaceFileRequest(String from) {
        IdentifyPillarsForReplaceFileRequest identifyPillarsForReplaceFileRequest = new IdentifyPillarsForReplaceFileRequest();
        initializeMessageDetails(identifyPillarsForReplaceFileRequest);
        identifyPillarsForReplaceFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForReplaceFileRequest.setAuditTrailInformation(null);
        identifyPillarsForReplaceFileRequest.setFrom(from);
        return identifyPillarsForReplaceFileRequest;
    }

    /**
     * Retrieves a Identify message for the Replace operation with specified CorrelationID, ReplyTo, and To.
     * @param correlationID The Correlation ID for the message.
     * @param replyTo The ReplyTo for the message.
     * @param toTopic The To for the message.
     * @return The requested IdentifyPillarsForReplaceFileRequest.
     */
    public IdentifyPillarsForReplaceFileRequest createIdentifyPillarsForReplaceFileRequest(String correlationID, 
            String replyTo, String toTopic, String fileID, String auditTrailInformation, String from) {
        IdentifyPillarsForReplaceFileRequest identifyPillarsForReplaceFileRequest = 
                createIdentifyPillarsForReplaceFileRequest(from);
        identifyPillarsForReplaceFileRequest.setCorrelationID(correlationID);
        identifyPillarsForReplaceFileRequest.setReplyTo(replyTo);
        identifyPillarsForReplaceFileRequest.setDestination(toTopic);
        identifyPillarsForReplaceFileRequest.setFileID(fileID);
        identifyPillarsForReplaceFileRequest.setAuditTrailInformation(auditTrailInformation);
        return identifyPillarsForReplaceFileRequest;
    }

    /**
     * Creates a IdentifyPillarsForReplaceFileResponse based on a request and some constants.
     * @param receivedIdentifyRequestMessage The request to base the response on.
     * @param pillarID The id of the pillar, which responds.
     * @param pillarDestinationId The destination for this pillar.
     * @return The requested IdentifyPillarsForReplaceFileResponse.
     */
    public IdentifyPillarsForReplaceFileResponse createIdentifyPillarsForReplaceFileResponse(
            IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage,
            String pillarID, String pillarDestinationId) {
        IdentifyPillarsForReplaceFileResponse ipfrfResponse = new IdentifyPillarsForReplaceFileResponse();
        initializeMessageDetails(ipfrfResponse);
        ipfrfResponse.setDestination(receivedIdentifyRequestMessage.getReplyTo());
        ipfrfResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        ipfrfResponse.setPillarID(pillarID);
        ipfrfResponse.setReplyTo(pillarDestinationId);
        ipfrfResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        ipfrfResponse.setResponseInfo(createPositiveIdentificationResponseInfo());
        ipfrfResponse.setFileID(receivedIdentifyRequestMessage.getFileID());
        ipfrfResponse.setFrom(pillarID);

        ipfrfResponse.setPillarChecksumSpec(null);

        return ipfrfResponse;
    }
    
    /**
     * Method for creating a generic ReplaceFileRequest.
     * @return The requested ReplaceFileRequest.
     */
    public ReplaceFileRequest createReplaceFileRequest(String from) {
        ReplaceFileRequest replaceFileRequest = new ReplaceFileRequest();
        initializeMessageDetails(replaceFileRequest);
        replaceFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        replaceFileRequest.setFileID(FILE_ID_DEFAULT);
        replaceFileRequest.setFrom(from);
        return replaceFileRequest;
    }

    /**
     * Method to create specific ReplaceFileRequest.
     * @param pillarID The id of the pillar to request for Replace.
     * @param toTopic The destination to send the message.
     * @param replyTo The where responses should be received.
     * @param correlationId The id of the message.
     * @param fileAddress The address where the file to be put can be retrieved from the pillar.
     * @param filesize The size of the file.
     * @return The requested ReplaceFileRequest.
     */
    public ReplaceFileRequest createReplaceFileRequest(String pillarID, String toTopic, String replyTo, String correlationId,
            String fileAddress, BigInteger filesize, String fileID, String auditTrailInformation, String from,
            ChecksumDataForFileTYPE oldChecksum, ChecksumDataForFileTYPE newChecksum, ChecksumSpecTYPE checksumRequested) {
        ReplaceFileRequest replaceFileRequest = createReplaceFileRequest(from);
        replaceFileRequest.setPillarID(pillarID);
        replaceFileRequest.setDestination(toTopic);
        replaceFileRequest.setReplyTo(replyTo);
        replaceFileRequest.setCorrelationID(correlationId);
        replaceFileRequest.setFileAddress(fileAddress);
        replaceFileRequest.setFileSize(filesize);
        replaceFileRequest.setFileID(fileID);
        replaceFileRequest.setAuditTrailInformation(auditTrailInformation);
        replaceFileRequest.setChecksumDataForExistingFile(oldChecksum);
        replaceFileRequest.setChecksumDataForNewFile(newChecksum);
        replaceFileRequest.setChecksumRequestForNewFile(checksumRequested);
        replaceFileRequest.setChecksumRequestForExistingFile(checksumRequested);
        
        return replaceFileRequest;
    }
    
    /**
     * Method to create a ProgressResponse to the put operation based on a ReplaceFileRequest.
     * @param request The ReplaceFileRequest to base the final response on.
     * @param pillarID The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested ReplaceFileProgressResponse.
     */
    public ReplaceFileProgressResponse createReplaceFileProgressResponse(ReplaceFileRequest request, 
            String pillarID, String pillarDestinationId) {
        ReplaceFileProgressResponse progressResponse = new ReplaceFileProgressResponse();
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
     * Method to create a FinalResponse to the put operation based on a ReplaceFileRequest.
     * 
     * @param request The ReplaceFileRequest to base the final response on.
     * @param pillarID The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested ReplaceFileFinalResponse.
     */
    public ReplaceFileFinalResponse createReplaceFileFinalResponse(ReplaceFileRequest request,
            String pillarID, String pillarDestinationId, ChecksumDataForFileTYPE checksumData) {
        ReplaceFileFinalResponse finalResponse = new ReplaceFileFinalResponse();
        initializeMessageDetails(finalResponse);
        finalResponse.setChecksumDataForNewFile(checksumData);
        finalResponse.setCorrelationID(request.getCorrelationID());
        finalResponse.setFileAddress(request.getFileAddress());
        finalResponse.setFileID(request.getFileID());
        finalResponse.setPillarChecksumSpec(null);
        finalResponse.setPillarID(pillarID);
        finalResponse.setReplyTo(pillarDestinationId);
        finalResponse.setResponseInfo(createCompleteResponseInfo());
        finalResponse.setDestination(request.getReplyTo());
        finalResponse.setFrom(pillarID);

        return finalResponse;
    }
}
