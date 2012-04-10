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
import org.bitrepository.protocol.TestMessageFactory;

/**
 * Messages creation factory for the DeleteFile tests.
 */
public class TestDeleteFileMessageFactory extends TestMessageFactory {
    /** The collection id for the factory.*/
    private final String collectionId;
    
    /**
     * Constructor.
     * @param bitrepositoryCollectionId The id for the collection, where the factory belong.
     */
    public TestDeleteFileMessageFactory(String bitrepositoryCollectionId) {
        super(bitrepositoryCollectionId);
        this.collectionId = bitrepositoryCollectionId;
    }

    /**
     * Retrieves a generic Identify message for the Delete operation.
     * @return The IdentifyPillarsForDeleteFileRequest for the test.
     */
    public IdentifyPillarsForDeleteFileRequest createIdentifyPillarsForDeleteFileRequest() {
        IdentifyPillarsForDeleteFileRequest identifyPillarsForDeleteFileRequest = new IdentifyPillarsForDeleteFileRequest();
        identifyPillarsForDeleteFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForDeleteFileRequest.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForDeleteFileRequest.setCollectionID(collectionId);
        identifyPillarsForDeleteFileRequest.setVersion(VERSION_DEFAULT);
        identifyPillarsForDeleteFileRequest.setAuditTrailInformation(null);
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
            String replyTo, String toTopic, String fileId) {
        IdentifyPillarsForDeleteFileRequest identifyPillarsForDeleteFileRequest = createIdentifyPillarsForDeleteFileRequest();
        identifyPillarsForDeleteFileRequest.setCorrelationID(correlationID);
        identifyPillarsForDeleteFileRequest.setReplyTo(replyTo);
        identifyPillarsForDeleteFileRequest.setTo(toTopic);
        identifyPillarsForDeleteFileRequest.setFileID(fileId);
        return identifyPillarsForDeleteFileRequest;
    }

    /**
     * Creates a IdentifyPillarsForDeleteFileResponse based on a request and some constants.
     * @param receivedIdentifyRequestMessage The request to base the response on.
     * @param pillarId The id of the pillar, which responds.
     * @param pillarDestinationId The destination for this pillar.
     * @return The requested IdentifyPillarsForDeleteFileResponse.
     */
    public IdentifyPillarsForDeleteFileResponse createIdentifyPillarsForDeleteFileResponse(
            IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage,
            String pillarId, String pillarDestinationId, String fileId) {
        IdentifyPillarsForDeleteFileResponse identifyResponse = new IdentifyPillarsForDeleteFileResponse();
        identifyResponse.setTo(receivedIdentifyRequestMessage.getReplyTo());
        identifyResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyResponse.setCollectionID(collectionId);
        identifyResponse.setPillarID(pillarId);
        identifyResponse.setReplyTo(pillarDestinationId);
        identifyResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyResponse.setMinVersion(VERSION_DEFAULT);
        identifyResponse.setVersion(VERSION_DEFAULT);

        identifyResponse.setPillarChecksumSpec(null);
        identifyResponse.setFileID(fileId);
        identifyResponse.setResponseInfo(IDENTIFY_INFO_DEFAULT);

        return identifyResponse;
    }

    /**
     * Method to create specific DeleteFileRequest.
     * @param pillarId The id of the pillar to request for Delete.
     * @param toTopic The destination to send the message.
     * @param replyTo The where responses should be received.
     * @param correlationId The id of the message.
     * @param fileAddress The address where the file to be delete can be retrieved from the pillar.
     * @param filesize The size of the file.
     * @return The requested DeleteFileRequest.
     */
    public DeleteFileRequest createDeleteFileRequest(String pillarId, String toTopic, String replyTo, 
            String correlationId, String fileId, ChecksumDataForFileTYPE checksumData, ChecksumSpecTYPE checksumType) {
        DeleteFileRequest deleteFileRequest = new DeleteFileRequest();
        deleteFileRequest.setMinVersion(VERSION_DEFAULT);
        deleteFileRequest.setVersion(VERSION_DEFAULT);
        deleteFileRequest.setCollectionID(collectionId);

        deleteFileRequest.setPillarID(pillarId);
        deleteFileRequest.setTo(toTopic);
        deleteFileRequest.setReplyTo(replyTo);
        deleteFileRequest.setCorrelationID(correlationId);
        deleteFileRequest.setFileID(fileId);
        deleteFileRequest.setChecksumDataForExistingFile(checksumData);
        deleteFileRequest.setChecksumRequestForExistingFile(checksumType);
        
        deleteFileRequest.setAuditTrailInformation(null);
        
        return deleteFileRequest;
    }
    
    /**
     * Method to create a ProgressResponse to the delete operation based on a DeleteFileRequest.
     * @param request The DeleteFileRequest to base the final response on.
     * @param pillarId The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested DeleteFileProgressResponse.
     */
    public DeleteFileProgressResponse createDeleteFileProgressResponse(DeleteFileRequest request, 
            String pillarId, String pillarDestinationId) {
        DeleteFileProgressResponse progressResponse = new DeleteFileProgressResponse();
        progressResponse.setTo(request.getReplyTo());
        progressResponse.setCorrelationID(request.getCorrelationID());
        progressResponse.setCollectionID(collectionId);
        progressResponse.setReplyTo(pillarDestinationId);
        progressResponse.setPillarID(pillarId);
        progressResponse.setFileID(request.getFileID());
        progressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        progressResponse.setVersion(VERSION_DEFAULT);
        progressResponse.setMinVersion(VERSION_DEFAULT);
        
        return progressResponse;
    }
    
    /**
     * Method to create a FinalResponse to the delete operation based on a DeleteFileRequest.
     * 
     * @param request The DeleteFileRequest to base the final response on.
     * @param pillarId The id of the pillar to respond.
     * @param pillarDestinationId The destination for the responding pillar.
     * @return The requested DeleteFileFinalResponse.
     */
    public DeleteFileFinalResponse createDeleteFileFinalResponse(DeleteFileRequest request,
            String pillarId, String pillarDestinationId, String fileId) {
        DeleteFileFinalResponse finalResponse = new DeleteFileFinalResponse();
        finalResponse.setTo(request.getReplyTo());
        finalResponse.setCorrelationID(request.getCorrelationID());
        finalResponse.setCollectionID(collectionId);
        finalResponse.setReplyTo(pillarDestinationId);
        finalResponse.setPillarID(pillarId);
        finalResponse.setFileID(fileId);
        finalResponse.setResponseInfo(FINAL_INFO_DEFAULT);
        finalResponse.setVersion(VERSION_DEFAULT);
        finalResponse.setMinVersion(VERSION_DEFAULT);
        
        return finalResponse;
    }
}
