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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.bitrepository.pillar.integration.func.Assert.assertEquals;
import static org.bitrepository.pillar.integration.func.Assert.assertNull;

public class IdentifyPillarsForPutFileIT extends DefaultPillarIdentificationTest {
    protected PutFileMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for putFile identification");
        addStep("Sending a putFile identification request.",
            "The pillar under test should make a response with the following elements: <ol>" +
                    "<li>'CollectionID' element corresponding to the supplied value</li>" +
                    "<li>'CorrelationID' element corresponding to the supplied value</li>" +
                    "<li>'From' element corresponding to the pillars component ID</li>" +
                    "<li>'To' element should be set to the value of the 'From' elements in the request</li>" +
                    "<li>'Destination' element should be set to the value of 'ReplyTo' from the request</li>" +
                    "<li>'ChecksumDataForExistingFile' element should be null</li>"  +
                    "<li>'PillarChecksumSpec' element should be null</li>" +
                    "<li>'PillarID' element corresponding to the pillars component ID</li>"  +
                    "<li>'ResponseInfo.ResponseCode' element should be IDENTIFICATION_POSITIVE</li>" +
                    "</ol>");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                NON_DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID(),
                "Received unexpected CollectionID");
        assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID(),
                "Received unexpected CorrelationID");
        assertEquals(receivedIdentifyResponse.getFrom(), getPillarID(),
                "Received unexpected PillarID");
        assertEquals(receivedIdentifyResponse.getTo(), identifyRequest.getFrom(),
                "Received unexpected 'To' element.");
        assertNull(receivedIdentifyResponse.getChecksumDataForExistingFile(),
                "Received unexpected ChecksumDataForExistingFile");
        assertNull(receivedIdentifyResponse.getPillarChecksumSpec(),
                "Received unexpected PillarChecksumSpec");
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID(),
                "Unexpected 'From' element in the received response:\n" + receivedIdentifyResponse + "\n");
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE,
                "Received unexpected ResponseCode");
        assertEquals(receivedIdentifyResponse.getDestination(), identifyRequest.getReplyTo(),
                "Received unexpected ReplyTo");
    }

    @Test( groups = {PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void identificationTestForChecksumPillar() {
        addDescription("Verifies the normal behaviour for putFile identification for a checksum pillar");
        addStep("Sending a putFile identification.",
                "The pillar under test should make a response with the correct elements. The only different from a " +
                "full pillar is that the checksum pillar will respond with the default checksum spec.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                NON_DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID());
        assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        assertNull(receivedIdentifyResponse.getChecksumDataForExistingFile());
        assertEquals(receivedIdentifyResponse.getPillarChecksumSpec().getChecksumType(),
                ChecksumUtils.getDefault(settingsForCUT).getChecksumType());
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        assertEquals(receivedIdentifyResponse.getDestination(), identifyRequest.getReplyTo());
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void fileExistsTest() {
        addDescription("Verifies the exists of a file with the same ID is handled correctly. " +
                "This means that a checksum for the existing file is returned, enabling the client to continue with " +
                "the put operation for the pillars not yet containing the file. The client can easily " +
                "implement idempotent behaviour based on this response." );
        addStep("Sending a putFile identification for a file already in the pillar.",
                "The pillar under test should send a DUPLICATE_FILE_FAILURE response with the (default type) checksum " +
                        "of the existing file.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.DUPLICATE_FILE_FAILURE);
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForPutFileRequest(
                NON_DEFAULT_FILE_ID, 0L);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForPutFileResponse.class);
    }
}
