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
package org.bitrepository.pillar.integration.func.deletefile;

import java.lang.reflect.Method;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteFileRequestIT extends DefaultPillarOperationTest {
    protected DeleteFileMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        pillarDestination = lookupDeleteFileDestination();
        msgFactory = new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
        clientProvider.getPutClient().putFile(
                collectionID, DEFAULT_FILE_URL, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
        clientProvider.getPutClient().putFile(
                nonDefaultCollectionId, DEFAULT_FILE_URL, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void normalDeleteFileTest() {
        addDescription("Tests a normal DeleteFile sequence");
        addStep("Send a DeleteFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                "OPERATION_COMPLETED final response");
        DeleteFileRequest deleteRequest = (DeleteFileRequest) createRequest();
        deleteRequest.setFileID(testSpecificFileID);
        messageBus.sendMessage(deleteRequest);

        DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), deleteRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);

        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getCorrelationID(), deleteRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
    }
    
    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void requestNewChecksumDeleteFileTest() {
        addDescription("Tests a normal DeleteFile sequence");
        addStep("Send a DeleteFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                "OPERATION_COMPLETED final response");
        
        ChecksumSpecTYPE requestedChecksumSpec = new ChecksumSpecTYPE();
        requestedChecksumSpec.setChecksumType(ChecksumType.HMAC_MD5);
        requestedChecksumSpec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
        
        DeleteFileRequest deleteRequest = msgFactory.createDeleteFileRequest(
                TestFileHelper.getDefaultFileChecksum(), requestedChecksumSpec, testSpecificFileID);
        messageBus.sendMessage(deleteRequest);

        DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), deleteRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);

        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getCorrelationID(), deleteRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertNotNull(finalResponse.getChecksumDataForExistingFile());
        Assert.assertEquals(finalResponse.getChecksumDataForExistingFile().getChecksumSpec(), 
                requestedChecksumSpec);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createDeleteFileRequest(TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_FILE_ID);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
    }

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(DeleteFileFinalResponse.class);
    }

    public String lookupDeleteFileDestination() {
        DeleteFileMessageFactory deleteLookupMessageFactory =
                new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        IdentifyPillarsForDeleteFileRequest identifyRequest = deleteLookupMessageFactory.createIdentifyPillarsForDeleteFileRequest(
                TestFileHelper.DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForDeleteFileResponse.class).getReplyTo();
    }
}
