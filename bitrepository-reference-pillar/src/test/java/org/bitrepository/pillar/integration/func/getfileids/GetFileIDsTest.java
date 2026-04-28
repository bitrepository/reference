/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.pillar.integration.func.getfileids;

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

@ExtendWith(SuiteInfoParameterResolver.class)
class GetFileIDsTest extends DefaultPillarOperationTest {
    protected GetFileIDsMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeEach
    void initialiseReferenceTest() throws Exception {
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        pillarDestination = lookupPillarDestination();
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestination);
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        clearReceivers();
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    void pillarGetFileIDsTestSuccessCase() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the pillar for the successful scenario.");

        addStep("Create and send a GetFileIDsRequest to the pillar.",
                "A GetFileIDsProgressResponse should be sent to the client with correct attributes follow by " +
                        "a GetFileIDsFinalResponse.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                FileIDsUtils.getAllFileIDs(), null);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "A GetFileIDs progress response should be sent to the client with correct attributes.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class,
                getOperationTimeout(), TimeUnit.SECONDS);
        Assertions.assertNotNull(progressResponse);
        Assertions.assertEquals(getFileIDsRequest.getCorrelationID(), progressResponse.getCorrelationID());
        Assertions.assertEquals(getFileIDsRequest.getFileIDs(), progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getFrom());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertEquals(pillarDestination, progressResponse.getReplyTo());
        Assertions.assertEquals(ResponseCode.OPERATION_ACCEPTED_PROGRESS,
                progressResponse.getResponseInfo().getResponseCode());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The GetFileIDs response should be sent by the pillar.");
        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) receiveResponse();
        Assertions.assertNotNull(finalResponse);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getFileIDsRequest.getCorrelationID(), finalResponse.getCorrelationID());
        Assertions.assertEquals(FileIDsUtils.getAllFileIDs(), finalResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), finalResponse.getFrom());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(pillarDestination, finalResponse.getReplyTo());
        Assertions.assertNull(finalResponse.getResultingFileIDs().getResultAddress());
        Assertions.assertTrue(
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem()
                        .size() >= 2,
                "Should be at least 2 files, but found: " +
                        finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem()
                                .size());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    void pillarGetFileIDsTestFailedNoSuchFileInOperation() throws Exception {
        addDescription("Tests that the pillar is able to handle requests for a non-existing file correctly during " +
                "the operation phase.");
        FileIDs fileids = FileIDsUtils.createFileIDs(nonDefaultFileId);

        addStep("Send a GetFileIDs request for a non-existing file.",
                "A FILE_NOT_FOUND_FAILURE response should be generated.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);
        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) receiveResponse();
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE, finalResponse.getResponseInfo().getResponseCode());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    void pillarGetFileIDsSpecificFileIDRequest() throws Exception {
        addDescription("Tests that the pillar is able to handle requests for a non-existing file correctly during " +
                "the operation phase.");
        FileIDs fileids = FileIDsUtils.createFileIDs(defaultFileId);

        addStep("Create and send a GetFileIDsRequest to the pillar.",
                "A GetFileIDsProgressResponse should be sent to the client with correct attributes follow by " +
                        "a GetFileIDsFinalResponse.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the FinalResponse for the GetFileIDs request.",
                "A OPERATION_COMPLETE final response only containing the requested file-id.");
        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) receiveResponse();
        Assertions.assertEquals(1,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(defaultFileId,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0)
                        .getFileID());
        Assertions.assertFalse(finalResponse.isSetPartialResult() && finalResponse.isPartialResult());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    void pillarGetFileIDsTestBadDeliveryURL() throws Exception {
        addDescription("Test the case when the delivery URL is unaccessible.");
        String badURL = "http://localhost:61616/¾";
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                FileIDsUtils.getAllFileIDs(), badURL);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the FinalResponse for the GetFileIDs request.",
                "A FILE_TRANSFER_FAILURE final response is expected.");
        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) receiveResponse();
        Assertions.assertEquals(ResponseCode.FILE_TRANSFER_FAILURE, finalResponse.getResponseInfo().getResponseCode());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    @Tag(PillarTestGroups.RESULT_UPLOAD)
    void pillarGetFileIDsTestDeliveryThroughUpload() throws Exception {
        addDescription("Test the case when the results should be delivered through the message .");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                FileIDsUtils.getAllFileIDs(), defaultUploadFileAddress);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the FinalResponse for the GetFileIDs request.",
                "A OPERATION_COMPLETE final response is expected containing the result provided address.");
        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) receiveResponse();
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(defaultUploadFileAddress, finalResponse.getResultingFileIDs().getResultAddress());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createGetFileIDsRequest(FileIDsUtils.getAllFileIDs(), null);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(GetFileIDsFinalResponse.class, getOperationTimeout(),
                TimeUnit.SECONDS);
    }

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(GetFileIDsFinalResponse.class);
    }

    public String lookupPillarDestination() {
        messageBus.sendMessage(msgFactory.createIdentifyPillarsForGetFileIDsRequest(null));
        return clientReceiver.waitForMessage(IdentifyPillarsForGetFileIDsResponse.class).getReplyTo();
    }
}
