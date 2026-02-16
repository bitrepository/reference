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

package org.bitrepository.pillar.integration.func.getstatus;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.bitrepository.pillar.messagefactories.GetStatusMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

public class IdentifyContributorsForGetStatusIT extends PillarFunctionTest {
    protected GetStatusMessageFactory msgFactory;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new GetStatusMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void normalGetStatusTest() {
        addDescription("Tests the GetStatus functionality of a pillar for the successful scenario.");

        addStep("Send a IdentifyContributorsForGetStatusRequest.",
                "The pillar should send a IDENTIFICATION_POSITIVE response.");
        IdentifyContributorsForGetStatusRequest identifyRequest =
                msgFactory.createIdentifyContributorsForGetStatusRequest();
        messageBus.sendMessage(identifyRequest);

        IdentifyContributorsForGetStatusResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyContributorsForGetStatusResponse.class);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID(), "Received " +
                "unexpected 'CollectionID' in response.");
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID(), "Received " +
                "unexpected 'CorrelationID' in response.");
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom(), "Received unexpected 'PillarID' in response.");
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE, receivedIdentifyResponse.getResponseInfo().getResponseCode(), "Received" +
                " unexpected 'ResponseCode' in response.");
        Assertions.assertEquals(identifyRequest.getReplyTo(), receivedIdentifyResponse.getDestination(), "Received unexpected " +
                "'To' in response.");
    }
}
