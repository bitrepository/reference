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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdentifyPillarsForDeleteFileIT extends DefaultPillarIdentificationTest {
    protected DeleteFileMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);

    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for deleteFile identification");
        addStep("Sending a deleteFile identification.",
            "The pillar under test should make a response with the correct elements.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = (IdentifyPillarsForDeleteFileRequest) createRequest(); 
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID());
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertNull(receivedIdentifyResponse.getPillarChecksumSpec());
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(receivedIdentifyResponse.getDestination(), identifyRequest.getReplyTo());
    }

    @Test( groups = {PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void identificationTestForChecksumPillar() {
        addDescription("Verifies the normal behaviour for deleteFile identification for a checksum pillar");
        addStep("Sending a deleteFile identification.",
                "The pillar under test should make a response with the correct elements. The only different from a " +
                "full pillar is that the checksum pillar will respond with the default checksum spec.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = (IdentifyPillarsForDeleteFileRequest) createRequest();
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID());
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getPillarChecksumSpec().getChecksumType(),
                ChecksumUtils.getDefault(settingsForCUT).getChecksumType());
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(receivedIdentifyResponse.getDestination(), identifyRequest.getReplyTo());
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void fileDoesNotExistsTest() {
        addDescription("Verifies that a request for a non-existing file is handled correctly");
        addStep("Sending a deleteFile identification for a file not in the pillar.",
                "The pillar under test should send a FILE_NOT_FOUND_FAILURE response.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                NON_DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.FILE_NOT_FOUND_FAILURE);
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForDeleteFileRequest(DEFAULT_FILE_ID);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForDeleteFileResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForDeleteFileResponse.class);
    }
}
