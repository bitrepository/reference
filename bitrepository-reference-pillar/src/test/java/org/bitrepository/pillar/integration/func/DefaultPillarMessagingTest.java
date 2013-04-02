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

package org.bitrepository.pillar.integration.func;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.testng.annotations.Test;

/**
 * Contains the tests for exploringa pillars handling of general messaging. The concrete class needs to
 * implement the abstract methods and add any operation specific tests. The test will not work for Alarm and status
 * messaging, as it is assumed that operations are collection scope.
 */
public abstract class DefaultPillarMessagingTest extends PillarFunctionTest {

    @Test( groups = {"pillar-integration-test"})
    public void missingCollectionIDTest() {
        addDescription("Verifies the a missing collectionID in the IdentifyRequest is rejected");
        addStep("Sending a IdentifyRequest without a collectionID.",
                "The pillar should send a REQUEST_NOT_UNDERSTOOD_FAILURE Response.");
        MessageRequest request = createRequest();
        request.setCollectionID(null);
        messageBus.sendMessage(request);

        MessageResponse receivedResponse = receiveResponse();
        Assert.assertEquals(receivedResponse.getResponseInfo().getResponseCode(),
                ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
    }

    @Test( groups = {"pillar-integration-test"})
    public void otherCollectionTest() {
        addDescription("Verifies identification works correctly for a second collection defined for pillar");
        addStep("Sending a identify request with a non-default collectionID (not the first collection) where " +
                "the pillar is part of",
                "The pillar under test should make a positive response");
        MessageRequest request = createRequest();
        request.setCollectionID(nonDefaultCollectionId);
        messageBus.sendMessage(request);
        assertPositivResponseIsReceived();
    }

    protected abstract MessageRequest createRequest();
    protected abstract MessageResponse receiveResponse();
    protected abstract void assertPositivResponseIsReceived();
    protected abstract void assertNoResponseIsReceived();
}
