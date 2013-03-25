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

public abstract class DefaultPillarIdentificationTest extends DefaultPillarMessagingTest {

    @Test( groups = {"pillar-integration-test"})
    public void irrelevantCollection() {
        addDescription("Verifies identification works correctly for a collection not defined for the pillar");
        addStep("Sending a putFile identification with a irrelevant collectionID. eg. the " +
                " pillar is not part of the collection",
                "The pillar under test should not make a response");
        MessageRequest request = createRequest();
        request.setCollectionID(irrelevantCollectionId);
        messageBus.sendMessage(request);
        assertNoResponseIsReceived();
    }

    protected void assertPositivResponseIsReceived() {
        MessageResponse receivedResponse = receiveResponse();
        Assert.assertEquals(receivedResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
    }
}
