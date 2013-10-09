/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.integration.func.putfile;

import java.lang.reflect.Method;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class PutFileRequestIT extends DefaultPillarOperationTest {
    protected PutFileMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        pillarDestination = lookupPutFileDestination();
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void normalPutFileTest() {
        addDescription("Tests a normal PutFile sequence");
        addStep("Send a putFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should send a final response with the following elements: <ol>"  +
                        "<li>'CollectionID' element corresponding to the supplied value</li>" +
                        "<li>'CorrelationID' element corresponding to the supplied value</li>" +
                        "<li>'From' element corresponding to the pillars component ID</li>" +
                        "<li>'To' element should be set to the value of the 'From' elements in the request</li>" +
                        "<li>'Destination' element should be set to the value of 'ReplyTo' from the request</li>" +
                        "<li>'ChecksumDataForExistingFile' element should be null</li>" +
                        "<li>'ChecksumDataForNewFile' element should be null</li>" +
                        "<li>'PillarID' element corresponding to the pillars component ID</li>" +
                        "<li>'FileID' element corresponding to the supplied fileID</li>" +
                        "<li>'FileAddress' element corresponding to the supplied FileAddress</li>"  +
                        "<li>'ResponseInfo.ResponseCode' element should be OPERATION_COMPLETED</li>" +
                        "</ol>");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID,
                DEFAULT_FILE_SIZE);
        messageBus.sendMessage(putRequest);

        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        assertNotNull(finalResponse);
        assertEquals(finalResponse.getCorrelationID(), putRequest.getCorrelationID());
        assertEquals(finalResponse.getCollectionID(), putRequest.getCollectionID());
        assertEquals(finalResponse.getFrom(), getPillarID());
        assertEquals(finalResponse.getTo(), putRequest.getFrom());
        assertEquals(finalResponse.getDestination(), putRequest.getReplyTo());
        assertNull(finalResponse.getChecksumDataForExistingFile());
        assertNull(finalResponse.getChecksumDataForNewFile());
        assertEquals(finalResponse.getFileID(), putRequest.getFileID());
        assertEquals(finalResponse.getFileAddress(), putRequest.getFileAddress());
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void putFileWithMD5ReturnChecksumTest() {
        addDescription("Tests that the pillar is able to return the default type checksum in the final response");
        addStep("Send a putFile request to " + testConfiguration.getPillarUnderTestID() + " with the ",
                "The pillar should send a final response with the ChecksumRequestForNewFile elemets containing the MD5 " +
                        "checksum for the supplied file.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID,
                DEFAULT_FILE_SIZE);
        putRequest.setChecksumRequestForNewFile(ChecksumUtils.getDefault(settingsForTestClient));
        messageBus.sendMessage(putRequest);

        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        assertEquals(TestFileHelper.getDefaultFileChecksum().getChecksumValue(),
                finalResponse.getChecksumDataForNewFile().getChecksumValue(),
                "Return MD5 checksum was not equals to checksum for default file.");
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST,
            PillarTestGroups.OPERATION_ACCEPTED_PROGRESS})
    public void putFileOperationAcceptedProgressTest() {
        addDescription("Tests a that a pillar sends progress response after receiving a putFile request.");

        addStep("Send a putFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a  progress response with the following elements: <ol>" +
                        "<li>'CollectionID' element corresponding to the value in the request.</li>" +
                        "<li>'CorrelationID' element corresponding to the value in the request.</li>" +
                        "<li>'From' element corresponding to the pillars component ID</li>" +
                        "<li>'To' element should be set to the value of the 'From' elements in the request</li>" +
                        "<li>'Destination' element should be set to the value of 'ReplyTo' from the request</li>" +
                        "<li>'PillarID' element corresponding to the pillars component ID</li>" +
                        "<li>'FileID' element corresponding to the supplied fileID</li>" +
                        "<li>'FileAddress' element corresponding to the supplied FileAddress</li>"  +
                        "<li>'ResponseInfo.ResponseCode' element should be OPERATION_ACCEPTED_PROGRESS</li>" +
                        "</ol>");
        PutFileRequest putRequest = (PutFileRequest)createRequest();
        messageBus.sendMessage(putRequest);

        PutFileProgressResponse progressResponse = clientReceiver.waitForMessage(PutFileProgressResponse.class);
        assertNotNull(progressResponse);
        assertEquals(progressResponse.getCorrelationID(), putRequest.getCorrelationID());
        assertEquals(progressResponse.getCollectionID(), putRequest.getCollectionID());
        assertEquals(progressResponse.getFrom(), getPillarID());
        assertEquals(progressResponse.getTo(), putRequest.getFrom());
        assertEquals(progressResponse.getDestination(), putRequest.getReplyTo());
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertEquals(progressResponse.getFileID(), putRequest.getFileID());
        assertEquals(progressResponse.getFileAddress(), putRequest.getFileAddress());
        assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createPutFileRequest(TestFileHelper.getDefaultFileChecksum(), null,
                DEFAULT_DOWNLOAD_FILE_ADDRESS, NON_DEFAULT_FILE_ID, DEFAULT_FILE_SIZE);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(PutFileFinalResponse.class);
    }

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(PutFileFinalResponse.class);
    }

    public String lookupPutFileDestination() {
        PutFileMessageFactory pillarLookupmMsgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        IdentifyPillarsForPutFileRequest identifyRequest = pillarLookupmMsgFactory.createIdentifyPillarsForPutFileRequest(
                TestFileHelper.DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class).getReplyTo();
    }
}
