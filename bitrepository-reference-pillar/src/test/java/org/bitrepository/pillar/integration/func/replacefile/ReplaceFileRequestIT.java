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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class ReplaceFileRequestIT extends DefaultPillarOperationTest {
    protected ReplaceFileMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        pillarDestination = lookupReplaceFileDestination();
        msgFactory = new ReplaceFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
        clientProvider.getPutClient().putFile(
                collectionID, DEFAULT_FILE_URL, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
        clientProvider.getPutClient().putFile(
                nonDefaultCollectionId, DEFAULT_FILE_URL, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void normalReplaceFileTest() {
        addDescription("Tests a normal ReplaceFile sequence");
        addStep("Send a ReplaceFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                "OPERATION_COMPLETED final response");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                TestFileHelper.getDefaultFileChecksum(), TestFileHelper.getDefaultFileChecksum(),
                null, null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(replaceRequest);

        ReplaceFileProgressResponse progressResponse = clientReceiver.waitForMessage(ReplaceFileProgressResponse.class, 
                getOperationTimeout(), TimeUnit.SECONDS);
        Assertions.assertNotNull(progressResponse);
        Assertions.assertEquals(progressResponse.getCorrelationID(), replaceRequest.getCorrelationID());
        Assertions.assertEquals(progressResponse.getFrom(), getPillarID());
        Assertions.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assertions.assertEquals(ResponseCode.OPERATION_ACCEPTED_PROGRESS,
                progressResponse.getResponseInfo().getResponseCode());

        ReplaceFileFinalResponse finalResponse = (ReplaceFileFinalResponse) receiveResponse();
        Assertions.assertNotNull(finalResponse);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(finalResponse.getCorrelationID(), replaceRequest.getCorrelationID());
        Assertions.assertEquals(finalResponse.getFrom(), getPillarID());
        Assertions.assertNull(finalResponse.getChecksumDataForExistingFile());
        Assertions.assertNull(finalResponse.getChecksumDataForNewFile());
        Assertions.assertEquals(finalResponse.getPillarID(), getPillarID());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createReplaceFileRequest(TestFileHelper.getDefaultFileChecksum(),
                TestFileHelper.getDefaultFileChecksum(), null, null,
                DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, DEFAULT_FILE_SIZE);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(ReplaceFileFinalResponse.class, getOperationTimeout(),
                TimeUnit.SECONDS);
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
