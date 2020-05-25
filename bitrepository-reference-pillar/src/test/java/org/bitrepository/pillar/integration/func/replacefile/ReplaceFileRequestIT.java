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
package org.bitrepository.pillar.integration.func.replacefile;

import java.lang.reflect.Method;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.ReplaceFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReplaceFileRequestIT extends DefaultPillarOperationTest {
    protected ReplaceFileMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        pillarDestination = lookupReplaceFileDestination();
        msgFactory = new ReplaceFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
        clientProvider.getPutClient().putFile(
                collectionID, DEFAULT_FILE_URL, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
        clientProvider.getPutClient().putFile(
                nonDefaultCollectionId, DEFAULT_FILE_URL, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void normalReplaceFileTest() {
        addDescription("Tests a normal ReplaceFile sequence");
        addStep("Send a ReplaceFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                "OPERATION_COMPLETED final response");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                TestFileHelper.getDefaultFileChecksum(), TestFileHelper.getDefaultFileChecksum(),
                null, null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(replaceRequest);

        ReplaceFileProgressResponse progressResponse = clientReceiver.waitForMessage(ReplaceFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), replaceRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getCorrelationID(), replaceRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertNull(finalResponse.getChecksumDataForExistingFile());
        Assert.assertNull(finalResponse.getChecksumDataForNewFile());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createReplaceFileRequest(TestFileHelper.getDefaultFileChecksum(),
                TestFileHelper.getDefaultFileChecksum(), null, null,
                DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, DEFAULT_FILE_SIZE);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
    }

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(ReplaceFileFinalResponse.class);
    }

    public String lookupReplaceFileDestination() {
        ReplaceFileMessageFactory replaceLookupMessageFactory =
                new ReplaceFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        IdentifyPillarsForReplaceFileRequest identifyRequest = replaceLookupMessageFactory.createIdentifyPillarsForReplaceFileRequest(
                TestFileHelper.DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForReplaceFileResponse.class).getReplyTo();
    }
}
