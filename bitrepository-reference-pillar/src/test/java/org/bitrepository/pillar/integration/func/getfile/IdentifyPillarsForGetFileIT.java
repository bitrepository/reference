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
package org.bitrepository.pillar.integration.func.getfile;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class IdentifyPillarsForGetFileIT extends PillarFunctionTest {
    protected GetFileMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void goodCaseIdentificationIT() {
        addDescription("Tests the general IdentifyPillarsForGetFile functionality of the pillar for the successful scenario.");
        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                "", DEFAULT_FILE_ID, getPillarID(), settingsForTestClient.getReceiverDestinationID());
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
        assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID());
        assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileID(), DEFAULT_FILE_ID);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        assertEquals(receivedIdentifyResponse.getDestination(), identifyRequest.getReplyTo());
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void nonExistingFileIdentificationIT() {
        addDescription("Tests the  IdentifyPillarsForGetFile functionality of the pillar for a IdentificationForGetFile " +
                "for a non existing file.");

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                "", NON_DEFAULT_FILE_ID, getPillarID(), settingsForTestClient.getReceiverDestinationID());
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.FILE_NOT_FOUND_FAILURE);
    }
}
