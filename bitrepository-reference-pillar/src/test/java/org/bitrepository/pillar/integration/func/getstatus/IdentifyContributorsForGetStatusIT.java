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

import java.lang.reflect.Method;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.bitrepository.pillar.messagefactories.GetStatusMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdentifyContributorsForGetStatusIT extends PillarFunctionTest {
    protected GetStatusMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        msgFactory = new GetStatusMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
    }

    @Test( groups = {"fullPillarTest", "checksumPillarTest"})
    public void normalGetStatusTest() {
        addDescription("Tests the GetStatus functionality of a pillar for the successful scenario.");

        addStep("Send a IdentifyContributorsForGetStatusRequest.",
                "The pillar should send a IDENTIFICATION_POSITIVE response.");
        IdentifyContributorsForGetStatusRequest identifyRequest =
                msgFactory.createIdentifyContributorsForGetStatusRequest();
        messageBus.sendMessage(identifyRequest);

        IdentifyContributorsForGetStatusResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyContributorsForGetStatusResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID());
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(receivedIdentifyResponse.getDestination(), identifyRequest.getReplyTo());
    }
}
