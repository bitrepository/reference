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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static java.lang.System.err;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bitrepository.bitrepositoryelements.ChecksumType.HMAC_MD5;
import static org.bitrepository.bitrepositoryelements.ResponseCode.OPERATION_ACCEPTED_PROGRESS;
import static org.bitrepository.bitrepositoryelements.ResponseCode.OPERATION_COMPLETED;
import static org.bitrepository.common.utils.Base16Utils.encodeBase16;
import static org.bitrepository.common.utils.TestFileHelper.getDefaultFileChecksum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeleteFileRequestIT extends DefaultPillarOperationTest {
    protected DeleteFileMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        pillarDestination = lookupDeleteFileDestination();
        msgFactory = new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
        clientProvider.getPutClient().putFile(
                collectionID, defaultFileUrl, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
        clientProvider.getPutClient().putFile(
                nonDefaultCollectionId, defaultFileUrl, testSpecificFileID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void normalDeleteFileTest() {
        addDescription("Tests a normal DeleteFile sequence");
        addStep("Send a DeleteFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                        "OPERATION_COMPLETED final response");
        DeleteFileRequest deleteRequest = (DeleteFileRequest) createRequest();
        deleteRequest.setFileID(testSpecificFileID);
        messageBus.sendMessage(deleteRequest);

        DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class, getOperationTimeout(),
                SECONDS);
        assertNotNull(progressResponse);
        assertEquals(deleteRequest.getCorrelationID(), progressResponse.getCorrelationID());
        assertEquals(getPillarID(), progressResponse.getFrom());
        assertEquals(getPillarID(), progressResponse.getPillarID());
        assertEquals(OPERATION_ACCEPTED_PROGRESS, progressResponse.getResponseInfo().getResponseCode());

        DeleteFileFinalResponse finalResponse = (DeleteFileFinalResponse) receiveResponse();
        assertNotNull(finalResponse);
        assertEquals(OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        assertEquals(deleteRequest.getCorrelationID(), finalResponse.getCorrelationID());
        assertEquals(getPillarID(), finalResponse.getFrom());
        assertEquals(getPillarID(), finalResponse.getPillarID());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    public void requestNewChecksumDeleteFileTest() {
        addDescription("Tests a normal DeleteFile sequence");
        addStep("Send a DeleteFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                        "OPERATION_COMPLETED final response");

        ChecksumSpecTYPE requestedChecksumSpec = new ChecksumSpecTYPE();
        requestedChecksumSpec.setChecksumType(HMAC_MD5);
        try {
            requestedChecksumSpec.setChecksumSalt(encodeBase16("abab"));
        } catch (DecoderException e) {
            err.println(e.getMessage());
        }

        DeleteFileRequest deleteRequest = msgFactory.createDeleteFileRequest(
                getDefaultFileChecksum(), requestedChecksumSpec, testSpecificFileID);
        messageBus.sendMessage(deleteRequest);

        DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class, getOperationTimeout(),
                SECONDS);
        assertNotNull(progressResponse);
        assertEquals(deleteRequest.getCorrelationID(), progressResponse.getCorrelationID());
        assertEquals(getPillarID(), progressResponse.getFrom());
        assertEquals(getPillarID(), progressResponse.getPillarID());
        assertEquals(OPERATION_ACCEPTED_PROGRESS, progressResponse.getResponseInfo().getResponseCode());

        DeleteFileFinalResponse finalResponse = (DeleteFileFinalResponse) receiveResponse();
        assertNotNull(finalResponse);
        assertEquals(OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        assertEquals(deleteRequest.getCorrelationID(), finalResponse.getCorrelationID());
        assertEquals(getPillarID(), finalResponse.getFrom());
        assertNotNull(finalResponse.getChecksumDataForExistingFile());
        assertEquals(requestedChecksumSpec, finalResponse.getChecksumDataForExistingFile().getChecksumSpec());
        assertEquals(getPillarID(), finalResponse.getPillarID());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createDeleteFileRequest(TestFileHelper.getDefaultFileChecksum(), null, defaultFileId);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(DeleteFileFinalResponse.class, getOperationTimeout(),
                TimeUnit.SECONDS);
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
