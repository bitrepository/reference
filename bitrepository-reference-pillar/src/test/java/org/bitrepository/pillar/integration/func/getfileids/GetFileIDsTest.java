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

import java.lang.reflect.Method;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class GetFileIDsTest extends DefaultPillarOperationTest {
    protected GetFileIDsMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        pillarDestination = lookupPillarDestination();
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestination);
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        clearReceivers();
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void pillarGetFileIDsTestSuccessCase() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the pillar for the successful scenario.");

        addStep("Create and send a GetFileIDsRequest to the pillar.",
                "A GetFileIDsProgressResponse should be sent to the client with correct attributes follow by " +
                "a GetFileIDsFinalResponse.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                FileIDsUtils.getAllFileIDs(), null);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "A GetFileIDs progress response should be sent to the client with correct attributes.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        assertNotNull(progressResponse);
        assertEquals(progressResponse.getCorrelationID(), getFileIDsRequest.getCorrelationID());
        assertEquals(progressResponse.getFileIDs(), getFileIDsRequest.getFileIDs());
        assertEquals(progressResponse.getFrom(), getPillarID());
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertEquals(progressResponse.getReplyTo(), pillarDestination);
        assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The GetFileIDs response should be sent by the pillar.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertNotNull(finalResponse);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getCorrelationID(), getFileIDsRequest.getCorrelationID());
        assertEquals(finalResponse.getFileIDs(), FileIDsUtils.getAllFileIDs());
        assertEquals(finalResponse.getFrom(), getPillarID());
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getReplyTo(), pillarDestination);
        assertNull(finalResponse.getResultingFileIDs().getResultAddress());
        assertTrue(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size() >= 2, 
                "Should be at least 2 files, but found: " + finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void pillarGetFileIDsTestFailedNoSuchFileInOperation() throws Exception {
        addDescription("Tests that the pillar is able to handle requests for a non-existing file correctly during " +
                       "the operation phase.");
        FileIDs fileids = FileIDsUtils.createFileIDs(NON_DEFAULT_FILE_ID);

        addStep("Send a GetFileIDs request for a non-existing file.",
                "A FILE_NOT_FOUND_FAILURE response should be generated.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void pillarGetFileIDsSpecificFileIDRequest() throws Exception {
        addDescription("Tests that the pillar is able to handle requests for a non-existing file correctly during " +
                       "the operation phase.");
        FileIDs fileids = FileIDsUtils.createFileIDs(DEFAULT_FILE_ID);

        addStep("Create and send a GetFileIDsRequest to the pillar.",
                "A GetFileIDsProgressResponse should be sent to the client with correct attributes follow by " +
                "a GetFileIDsFinalResponse.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request.",
                "A OPERATION_COMPLETE final response only containing the requested file-id.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(), 1);
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID(), DEFAULT_FILE_ID);
        assertFalse(finalResponse.isSetPartialResult() && finalResponse.isPartialResult());
    }
    
    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void pillarGetFileIDsTestBadDeliveryURL() throws Exception {
        addDescription("Test the case when the delivery URL is unaccessible.");
        String badURL = "http://localhost:61616/¾";
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                FileIDsUtils.getAllFileIDs(), badURL);
        messageBus.sendMessage(getFileIDsRequest);

        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(),
                ResponseCode.FILE_TRANSFER_FAILURE);
    }

    @Test( groups = {
            PillarTestGroups.FULL_PILLAR_TEST,
            PillarTestGroups.CHECKSUM_PILLAR_TEST,
            PillarTestGroups.RESULT_UPLOAD})
    public void pillarGetFileIDsTestDeliveryThroughUpload() throws Exception {
        addDescription("Test the case when the results should be delivered through the message .");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                FileIDsUtils.getAllFileIDs(), DEFAULT_UPLOAD_FILE_ADDRESS);
        messageBus.sendMessage(getFileIDsRequest);

        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getResultingFileIDs().getResultAddress(), DEFAULT_UPLOAD_FILE_ADDRESS);
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createGetFileIDsRequest(FileIDsUtils.getAllFileIDs(), null);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
    }

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(GetFileIDsFinalResponse.class);
    }

    public String lookupPillarDestination() {
        messageBus.sendMessage(msgFactory.createIdentifyPillarsForGetFileIDsRequest(null));
        return clientReceiver.waitForMessage(IdentifyPillarsForGetFileIDsResponse.class).getReplyTo();
    }
}
