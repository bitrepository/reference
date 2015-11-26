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
package org.bitrepository.modify.deletefile;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 * Messages creation factory for the DeleteFile tests.
 */
public class TestDeleteFileMessageFactory extends ClientTestMessageFactory {
    /**
     * Constructor.
     * @param bitrepositoryCollectionId The id for the collection, where the factory belong.
     */
    public TestDeleteFileMessageFactory(String bitrepositoryCollectionId) {
        super(bitrepositoryCollectionId);
    }

    /**
     * Retrieves a generic Identify message for the Delete operation.
     * @return The IdentifyPillarsForDeleteFileRequest for the test.
     */
    public IdentifyPillarsForDeleteFileRequest createIdentifyPillarsForDeleteFileRequest(String from) {
        IdentifyPillarsForDeleteFileRequest identifyPillarsForDeleteFileRequest = new IdentifyPillarsForDeleteFileRequest();
        initializeMessageDetails(identifyPillarsForDeleteFileRequest);
        identifyPillarsForDeleteFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForDeleteFileRequest.setAuditTrailInformation(null);
        identifyPillarsForDeleteFileRequest.setFrom(from);
        return identifyPillarsForDeleteFileRequest;
    }

    /**
     * Retrieves a Identify message for the Delete operation with specified CorrelationID, ReplyTo, and To.
     * @param correlationID The Correlation ID for the message.
     * @param replyTo The ReplyTo for the message.
     * @param toTopic The To for the message.
     * @return The requested IdentifyPillarsForDeleteFileRequest.
     */
    public IdentifyPillarsForDeleteFileRequest createIdentifyPillarsForDeleteFileRequest(String correlationID, 
            String replyTo, String toTopic, String fileID, String from) {
        IdentifyPillarsForDeleteFileRequest identifyPillarsForDeleteFileRequest = 
                createIdentifyPillarsForDeleteFileRequest(from);
        identifyPillarsForDeleteFileRequest.setCorrelationID(correlationID);
        identifyPillarsForDeleteFileRequest.setReplyTo(replyTo);
        identifyPillarsForDeleteFileRequest.setDestination(toTopic);
        identifyPillarsForDeleteFileRequest.setFileID(fileID);
        return identifyPillarsForDeleteFileRequest;
    }

    /**
     * Creates a IdentifyPillarsForDeleteFileResponse based on a request and some constants.
     * @param receivedIdentifyRequestMessage The request to base the response on.
     * @param pillarID The id of the pillar, which responds.
     * @param pillarDestinationId The destination for this pillar.
     * @return The requested IdentifyPillarsForDeleteFileResponse.
     */
    public IdentifyPillarsForDeleteFileResponse createIdentifyPillarsForDeleteFileResponse(
            IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage,
            String pillarID, String pillarDestinationId, String fileID) {
        IdentifyPillarsForDeleteFileResponse identifyResponse = new IdentifyPillarsForDeleteFileResponse();
        initializeMessageDetails(identifyResponse);
        identifyResponse.setDestination(receivedIdentifyRequestMessage.getReplyTo());
        identifyResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyResponse.setPillarID(pillarID);
        identifyResponse.setReplyTo(pillarDestinationId);
        identifyResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyResponse.setFrom(pillarID);
        identifyResponse.setPillarChecksumSpec(null);
        identifyResponse.setFileID(fileID);
        identifyResponse.setResponseInfo(createPositiveIdentificationResponseInfo());

        return identifyResponse;
    }

    public DeleteFileRequest createDeleteFileRequest(String pillarID, String toTopic, String replyTo,
            String correlationId, String fileID, ChecksumDataForFileTYPE checksumData,
            ChecksumSpecTYPE checksumType, String from) {
        DeleteFileRequest deleteFileRequest = new DeleteFileRequest();
        initializeMessageDetails(deleteFileRequest);
        deleteFileRequest.setFrom(from);
        deleteFileRequest.setPillarID(pillarID);
        deleteFileRequest.setDestination(toTopic);
        deleteFileRequest.setReplyTo(replyTo);
        deleteFileRequest.setCorrelationID(correlationId);
        deleteFileRequest.setFileID(fileID);
        deleteFileRequest.setChecksumDataForExistingFile(checksumData);
        deleteFileRequest.setChecksumRequestForExistingFile(checksumType);
        
        deleteFileRequest.setAuditTrailInformation(null);
        
        return deleteFileRequest;
    }
    
    /**
     * Method to create a ProgressResponse to the delete operation based on a DeleteFileRequest.
     * @param request The DeleteFileRequest to base the final response on.
     * @param pillarID The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested DeleteFileProgressResponse.
     */
    public DeleteFileProgressResponse createDeleteFileProgressResponse(DeleteFileRequest request, 
            String pillarID, String pillarDestinationId) {
        DeleteFileProgressResponse progressResponse = new DeleteFileProgressResponse();
        initializeMessageDetails(progressResponse);
        progressResponse.setDestination(request.getReplyTo());
        progressResponse.setCorrelationID(request.getCorrelationID());
        progressResponse.setReplyTo(pillarDestinationId);
        progressResponse.setPillarID(pillarID);
        progressResponse.setFileID(request.getFileID());
        progressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        progressResponse.setFrom(pillarID);
        
        return progressResponse;
    }
    
    /**
     * Method to create a FinalResponse to the delete operation based on a DeleteFileRequest.
     * 
     * @param request The DeleteFileRequest to base the final response on.
     * @param pillarID The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested DeleteFileFinalResponse.
     */
    public DeleteFileFinalResponse createDeleteFileFinalResponse(DeleteFileRequest request,
            String pillarID, String pillarDestinationId, String fileID) {
        DeleteFileFinalResponse finalResponse = new DeleteFileFinalResponse();
        initializeMessageDetails(finalResponse);
        finalResponse.setDestination(request.getReplyTo());
        finalResponse.setCorrelationID(request.getCorrelationID());
        finalResponse.setReplyTo(pillarDestinationId);
        finalResponse.setPillarID(pillarID);
        finalResponse.setFileID(fileID);
        finalResponse.setResponseInfo(createCompleteResponseInfo());
        finalResponse.setFrom(pillarID);
        
        return finalResponse;
    }
}
